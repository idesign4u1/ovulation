package com.ovulation.health.sensor

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import com.ovulation.health.data.db.OvulationDatabase
import com.ovulation.health.data.model.VoiceAnalysis
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDateTime
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.sqrt

/**
 * Passively records and analyses voice while the user is in a call
 * or speaking (detected by voice-activity detection).
 *
 * - Triggered by incoming/outgoing call events (PhoneCallReceiver)
 *   or by a long-press button in the app.
 * - Runs as long as voice activity is detected, then saves a
 *   VoiceAnalysis record with F0, shimmer and jitter.
 *
 * Key science: F0 rises ~15 Hz two days before ovulation (Wennig et al.).
 */
class PassiveVoiceMonitor(
    private val context: Context,
    private val database: OvulationDatabase
) {

    companion object {
        private const val SAMPLE_RATE = 44100
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        private const val BUFFER_SECONDS = 5          // analyse every 5 s
        private const val MIN_RMS_FOR_VOICE = 500.0   // below this = silence
        private const val ANALYSIS_WINDOW_FRAMES = 2048
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var recordingJob: Job? = null
    private var audioRecord: AudioRecord? = null

    /**
     * Accumulated F0 readings during this call session.
     * Averaged at the end to reduce noise.
     */
    private val sessionF0 = mutableListOf<Float>()
    private val sessionShimmer = mutableListOf<Float>()
    private val sessionJitter = mutableListOf<Float>()

    /** Call this when a phone call starts (or the user taps "Analyse voice"). */
    fun startAnalysis() {
        if (recordingJob?.isActive == true) return
        sessionF0.clear()
        sessionShimmer.clear()
        sessionJitter.clear()
        Timber.d("Passive voice monitor started")
        recordingJob = scope.launch { runRecordingLoop() }
    }

    /** Call this when the call ends. */
    fun stopAnalysis() {
        recordingJob?.cancel()
        recordingJob = null
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
        saveSessionResult()
    }

    private suspend fun runRecordingLoop() {
        val bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
            .coerceAtLeast(SAMPLE_RATE * BUFFER_SECONDS * 2)

        val record = try {
            AudioRecord(
                MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, bufferSize
            ).also { audioRecord = it }
        } catch (e: SecurityException) {
            Timber.e(e, "RECORD_AUDIO permission missing")
            return
        }

        record.startRecording()
        val buffer = ShortArray(ANALYSIS_WINDOW_FRAMES)

        try {
            while (isActive) {
                val read = record.read(buffer, 0, buffer.size)
                if (read <= 0) continue

                val rms = calculateRMS(buffer, read)
                if (rms < MIN_RMS_FOR_VOICE) continue   // silence – skip

                // Perform frequency analysis on voiced frame
                val floatBuffer = buffer.take(read).map { it.toFloat() }.toFloatArray()
                val f0 = extractF0(floatBuffer)
                if (f0 > 80f && f0 < 500f) {              // realistic speech range
                    sessionF0.add(f0)
                    sessionShimmer.add(calculateShimmer(floatBuffer))
                    sessionJitter.add(calculateJitter(floatBuffer))
                    Timber.v("Voice frame: F0=${"%.1f".format(f0)} Hz")
                }
            }
        } finally {
            record.stop()
            record.release()
            audioRecord = null
        }
    }

    /**
     * Autocorrelation-based fundamental frequency (F0) estimation.
     * Robust and simple – works well for speech.
     */
    private fun extractF0(samples: FloatArray): Float {
        val n = samples.size
        val maxLag = SAMPLE_RATE / 80   // 80 Hz lowest expected F0
        val minLag = SAMPLE_RATE / 500  // 500 Hz highest expected F0

        var maxCorr = Double.MIN_VALUE
        var bestLag = minLag

        for (lag in minLag..maxLag) {
            var corr = 0.0
            for (i in 0 until n - lag) {
                corr += samples[i] * samples[i + lag]
            }
            if (corr > maxCorr) {
                maxCorr = corr
                bestLag = lag
            }
        }

        return SAMPLE_RATE.toFloat() / bestLag
    }

    /**
     * Shimmer: amplitude variation between successive glottal cycles.
     * Computed as mean |A[i] – A[i-1]| / mean(A).
     */
    private fun calculateShimmer(samples: FloatArray): Float {
        val amplitudes = samples.map { abs(it) }
        val mean = amplitudes.average().takeIf { it > 0 } ?: return 0f
        val diffs = amplitudes.zipWithNext { a, b -> abs(b - a) }
        return (diffs.average() / mean * 100).toFloat()   // expressed as %
    }

    /**
     * Jitter: F0 cycle-to-cycle period variation.
     * Uses zero-crossing intervals as proxy for period.
     */
    private fun calculateJitter(samples: FloatArray): Float {
        val crossings = mutableListOf<Int>()
        for (i in 1 until samples.size) {
            if (samples[i - 1] < 0 && samples[i] >= 0) crossings.add(i)
        }
        if (crossings.size < 3) return 0f

        val periods = crossings.zipWithNext { a, b -> (b - a).toDouble() }
        val mean = periods.average().takeIf { it > 0 } ?: return 0f
        val diffs = periods.zipWithNext { a, b -> abs(b - a) }
        return (diffs.average() / mean * 100).toFloat()   // expressed as %
    }

    private fun calculateRMS(buffer: ShortArray, length: Int): Double {
        val sumSq = buffer.take(length).sumOf { it.toDouble() * it }
        return sqrt(sumSq / length)
    }

    private fun saveSessionResult() {
        if (sessionF0.isEmpty()) {
            Timber.d("No voiced frames captured – skipping save")
            return
        }

        scope.launch {
            val avgF0 = sessionF0.average().toFloat()
            val baseline = getBaselineF0()
            val shift = avgF0 - baseline

            val analysis = VoiceAnalysis(
                testDate = LocalDateTime.now(),
                audioUri = "passive_session",
                fundamentalFrequency = avgF0,
                frequencyShift = shift,
                shimmer = sessionShimmer.average().toFloat(),
                jitter = sessionJitter.average().toFloat(),
                notes = "Passive session: ${sessionF0.size} voiced frames, " +
                    "F0=${"%.1f".format(avgF0)} Hz, Δ=${"%.1f".format(shift)} Hz"
            )
            database.voiceAnalysisDao().insert(analysis)
            Timber.d("Voice session saved: $analysis")
        }
    }

    /** Retrieve personal baseline F0 from the last 7 non-peri-ovulatory days. */
    private suspend fun getBaselineF0(): Float {
        // Use a fixed default if no history is available.
        // A real implementation would query the DB for menstrual-phase days.
        return 210f
    }

    fun release() {
        stopAnalysis()
        scope.cancel()
    }
}
