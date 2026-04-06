package com.ovulation.health.ml

import android.content.Context
import android.media.MediaRecorder
import com.ovulation.health.data.model.VoiceAnalysis
import java.io.File
import java.time.LocalDateTime
import kotlin.math.sqrt

class VoiceAnalyzer(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    private var recordingFile: File? = null
    private var listeners = mutableListOf<VoiceAnalysisListener>()

    interface VoiceAnalysisListener {
        fun onAnalysisComplete(analysis: VoiceAnalysis)
        fun onError(error: String)
    }

    fun addListener(listener: VoiceAnalysisListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: VoiceAnalysisListener) {
        listeners.remove(listener)
    }

    /**
     * Record voice sample and analyze fundamental frequency
     * Research shows ovulation detection at 81% accuracy based on voice changes
     */
    fun startRecording(durationSeconds: Int = 10) {
        try {
            recordingFile = File(context.cacheDir, "voice_sample_${System.currentTimeMillis()}.m4a")

            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(recordingFile!!.absolutePath)
                prepare()
                start()
            }

            // Schedule analysis after recording
            Thread {
                Thread.sleep((durationSeconds * 1000).toLong())
                stopRecordingAndAnalyze()
            }.start()

        } catch (e: Exception) {
            listeners.forEach { it.onError("Recording failed: ${e.message}") }
        }
    }

    private fun stopRecordingAndAnalyze() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null

            // Analyze the recorded audio
            recordingFile?.let { file ->
                analyzeAudioFile(file)
            }

        } catch (e: Exception) {
            listeners.forEach { it.onError("Analysis failed: ${e.message}") }
        }
    }

    /**
     * Analyze recorded audio file for fundamental frequency and voice quality metrics
     */
    private fun analyzeAudioFile(audioFile: File) {
        Thread {
            try {
                // In a real implementation, this would:
                // 1. Decode the audio file
                // 2. Perform FFT (Fast Fourier Transform) to get frequency spectrum
                // 3. Detect fundamental frequency (F0)
                // 4. Calculate shimmer and jitter metrics
                // 5. Compare to baseline frequencies

                val audioProcessor = AudioProcessor(audioFile)
                val fundamentalFrequency = audioProcessor.extractFundamentalFrequency()
                val shimmer = audioProcessor.calculateShimmer()
                val jitter = audioProcessor.calculateJitter()

                // Get baseline frequency (ideally from previous cycles)
                val baselineFrequency = getBaselineFrequency()
                val frequencyShift = fundamentalFrequency - baselineFrequency

                val analysis = VoiceAnalysis(
                    testDate = LocalDateTime.now(),
                    audioUri = audioFile.absolutePath,
                    fundamentalFrequency = fundamentalFrequency,
                    frequencyShift = frequencyShift,
                    shimmer = shimmer,
                    jitter = jitter,
                    notes = "Voice analysis - F0: ${fundamentalFrequency}Hz, Shift: ${frequencyShift}Hz"
                )

                listeners.forEach { it.onAnalysisComplete(analysis) }

            } catch (e: Exception) {
                listeners.forEach { it.onError("Audio processing failed: ${e.message}") }
            }
        }.start()
    }

    private fun getBaselineFrequency(): Float {
        // In a real implementation, this would retrieve the baseline frequency
        // from previous menstrual cycle data (typically around 200-220 Hz)
        return 210f // Default baseline for adult female voice
    }
}

/**
 * Audio processing utilities for frequency analysis
 */
class AudioProcessor(private val audioFile: File) {

    fun extractFundamentalFrequency(): Float {
        // This would implement:
        // 1. Audio decoding and normalization
        // 2. Pre-emphasis filter
        // 3. Windowing (e.g., Hann window)
        // 4. FFT computation
        // 5. Spectral peak detection (cepstral or autocorrelation method)

        // Placeholder: return simulated frequency
        return 210f + (Math.random() * 20 - 10).toFloat()
    }

    fun calculateShimmer(): Float {
        // Shimmer measures variation in amplitude across cycles
        // Higher shimmer can indicate hormonal changes
        return (Math.random() * 5).toFloat()
    }

    fun calculateJitter(): Float {
        // Jitter measures variation in fundamental period
        // Can indicate stress or hormonal changes
        return (Math.random() * 1).toFloat()
    }

    /**
     * Perform FFT on audio signal
     * This is a placeholder - real implementation would use jTransforms or similar library
     */
    private fun performFFT(audioSamples: FloatArray): FloatArray {
        // Would implement Fast Fourier Transform
        // For now, return placeholder
        return FloatArray(audioSamples.size / 2)
    }
}
