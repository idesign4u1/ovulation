package com.ovulation.health.sensor

import android.content.Context
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.view.Surface
import com.ovulation.health.data.db.OvulationDatabase
import com.ovulation.health.data.model.CardiacData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDateTime
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Detects heart rate and HRV via PPG by opening the rear camera
 * with the flash (torch) on.
 *
 * Measurement is triggered every time the user places their finger
 * on the camera (detected via a proximity-like brightness drop on
 * the image, or called explicitly by the overlay view).
 *
 * Duration: ~30 seconds of frames → HR + HRV calculation.
 */
class CameraPPGDetector(
    private val context: Context,
    private val database: OvulationDatabase
) {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null
    private var imageReader: ImageReader? = null

    private val handlerThread = HandlerThread("ppg-camera").also { it.start() }
    private val handler = Handler(handlerThread.looper)

    /** Red-channel brightness values collected during measurement */
    private val ppgSamples = mutableListOf<Double>()
    private var samplingActive = false
    private var measurementStartTime = 0L

    companion object {
        private const val SAMPLE_DURATION_MS = 30_000L   // 30 seconds
        private const val FRAME_WIDTH = 160
        private const val FRAME_HEIGHT = 120
        private const val MIN_SAMPLES_FOR_ANALYSIS = 150  // ~5 Hz × 30 s
    }

    /**
     * Open the rear camera with flash torch and start collecting PPG frames.
     * Called when the user's finger is detected on the camera lens.
     */
    fun startMeasurement() {
        if (samplingActive) return
        Timber.d("PPG measurement started")
        ppgSamples.clear()
        samplingActive = true
        measurementStartTime = System.currentTimeMillis()
        openCamera()
    }

    private fun openCamera() {
        try {
            val rearCamera = findRearCamera() ?: return
            cameraManager.openCamera(rearCamera, cameraStateCallback, handler)
        } catch (e: CameraAccessException) {
            Timber.e(e, "Failed to open camera for PPG")
            samplingActive = false
        } catch (e: SecurityException) {
            Timber.e(e, "Camera permission missing")
            samplingActive = false
        }
    }

    private fun findRearCamera(): String? =
        cameraManager.cameraIdList.firstOrNull { id ->
            cameraManager.getCameraCharacteristics(id)
                .get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
        }

    private val cameraStateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            cameraDevice = camera
            startCapture(camera)
        }

        override fun onDisconnected(camera: CameraDevice) {
            camera.close()
            cameraDevice = null
            Timber.d("Camera disconnected during PPG")
        }

        override fun onError(camera: CameraDevice, error: Int) {
            camera.close()
            cameraDevice = null
            samplingActive = false
            Timber.e("Camera error $error during PPG")
        }
    }

    private fun startCapture(camera: CameraDevice) {
        imageReader = ImageReader.newInstance(
            FRAME_WIDTH, FRAME_HEIGHT, ImageFormat.YUV_420_888, 4
        )
        imageReader!!.setOnImageAvailableListener(imageAvailableListener, handler)

        val surface = imageReader!!.surface
        val dummyTexture = SurfaceTexture(0).also { it.setDefaultBufferSize(1, 1) }
        val dummySurface = Surface(dummyTexture)

        camera.createCaptureSession(
            listOf(surface, dummySurface),
            object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    captureSession = session
                    val requestBuilder = camera.createCaptureRequest(
                        CameraDevice.TEMPLATE_PREVIEW
                    ).apply {
                        addTarget(surface)
                        // Enable torch to illuminate finger
                        set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH)
                        // Disable auto-exposure for stable readings
                        set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF)
                    }
                    session.setRepeatingRequest(requestBuilder.build(), null, handler)
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {
                    Timber.e("Camera capture session configuration failed")
                    samplingActive = false
                }
            },
            handler
        )
    }

    private val imageAvailableListener = ImageReader.OnImageAvailableListener { reader ->
        val image = reader.acquireLatestImage() ?: return@OnImageAvailableListener
        try {
            // Extract mean brightness of red channel (Y-plane in YUV is luminance)
            val yPlane = image.planes[0]
            val buffer = yPlane.buffer
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)
            val meanBrightness = bytes.map { it.toInt() and 0xFF }.average()
            ppgSamples.add(meanBrightness)
        } finally {
            image.close()
        }

        // Stop after SAMPLE_DURATION_MS
        if (System.currentTimeMillis() - measurementStartTime >= SAMPLE_DURATION_MS) {
            stopCapture()
            analyzeAndSave()
        }
    }

    private fun stopCapture() {
        samplingActive = false
        captureSession?.close()
        captureSession = null
        cameraDevice?.close()
        cameraDevice = null
        imageReader?.close()
        imageReader = null
        Timber.d("PPG capture stopped, ${ppgSamples.size} samples collected")
    }

    private fun analyzeAndSave() {
        if (ppgSamples.size < MIN_SAMPLES_FOR_ANALYSIS) {
            Timber.w("Not enough PPG samples (${ppgSamples.size})")
            return
        }

        scope.launch {
            val hr = estimateHeartRate(ppgSamples, sampleRateHz = 5.0)
            val hrv = estimateHRV(ppgSamples)

            Timber.d("PPG result: HR=$hr BPM, HRV=${"%.1f".format(hrv)} ms")

            val cardiacData = CardiacData(
                testDate = LocalDateTime.now(),
                heartRate = hr,
                heartRateVariability = hrv,
                notes = "Passive PPG – ${ppgSamples.size} samples / 30 s"
            )
            database.cardiacDataDao().insert(cardiacData)
            Timber.d("Cardiac data saved")
        }
    }

    /**
     * Peak-based heart rate estimation.
     * 1. Band-pass the signal (remove DC + high-freq noise via simple moving average)
     * 2. Count peaks in 30-second window
     */
    private fun estimateHeartRate(samples: List<Double>, sampleRateHz: Double): Int {
        if (samples.size < 10) return 0

        // Smooth signal (5-point moving average)
        val smoothed = samples.windowed(5, 1) { w -> w.average() }

        // Detect peaks (local maxima)
        val peaks = mutableListOf<Int>()
        for (i in 1 until smoothed.size - 1) {
            if (smoothed[i] > smoothed[i - 1] && smoothed[i] > smoothed[i + 1]) {
                peaks.add(i)
            }
        }

        if (peaks.size < 2) return 0

        // HR from average R-R interval
        val durationSeconds = samples.size / sampleRateHz
        return ((peaks.size.toDouble() / durationSeconds) * 60).toInt().coerceIn(40, 200)
    }

    /**
     * RMSSD-based HRV (Root Mean Square of Successive Differences).
     */
    private fun estimateHRV(samples: List<Double>): Float {
        if (samples.size < 10) return 0f

        val smoothed = samples.windowed(5, 1) { w -> w.average() }
        val differences = smoothed.zipWithNext { a, b -> abs(b - a) }
        val squaredDiffs = differences.map { it * it }
        return sqrt(squaredDiffs.average()).toFloat()
    }

    fun release() {
        scope.cancel()
        stopCapture()
        handlerThread.quitSafely()
    }
}
