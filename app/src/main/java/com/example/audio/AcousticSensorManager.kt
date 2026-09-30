package com.example.audio

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import com.example.data.model.AcousticReading
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

enum class SensorStreamMode {
    EDGE_IOT_SIMULATOR, // Deployed MEMS Piezoelectric Sensor Node on Bridge Pillar
    LIVE_DEVICE_MIC     // Real Phone Microphone Audio Stream
}

enum class SimulatedStressEvent {
    NORMAL_FLOW,
    HEAVY_TRUCK_PASSAGE,
    MICRO_FRACTURE_EVENT,
    POST_TENSION_CABLE_PING
}

class AcousticSensorManager(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val _currentReading = MutableStateFlow(createInitialReading())
    val currentReading: StateFlow<AcousticReading> = _currentReading.asStateFlow()

    private val _streamMode = MutableStateFlow(SensorStreamMode.EDGE_IOT_SIMULATOR)
    val streamMode: StateFlow<SensorStreamMode> = _streamMode.asStateFlow()

    private val _activeStressEvent = MutableStateFlow(SimulatedStressEvent.MICRO_FRACTURE_EVENT)
    val activeStressEvent: StateFlow<SimulatedStressEvent> = _activeStressEvent.asStateFlow()

    private val _isStreaming = MutableStateFlow(true)
    val isStreaming: StateFlow<Boolean> = _isStreaming.asStateFlow()

    private var streamingJob: Job? = null
    private var audioRecord: AudioRecord? = null

    // Simulated asset parameters (defaults to Bund Garden Bridge Pier P-04B)
    var selectedAssetName: String = "Bund Garden Bridge"
    var selectedPillarId: String = "Pier P-04B"
    var baselineFrequencyHz: Float = 28.4f

    init {
        startStream()
    }

    fun setStreamMode(mode: SensorStreamMode) {
        if (_streamMode.value != mode) {
            _streamMode.value = mode
            startStream()
        }
    }

    fun triggerStressEvent(event: SimulatedStressEvent) {
        _activeStressEvent.value = event
    }

    fun toggleStreaming() {
        if (_isStreaming.value) {
            stopStream()
        } else {
            startStream()
        }
    }

    fun startStream() {
        stopStream()
        _isStreaming.value = true

        streamingJob = scope.launch(Dispatchers.Default) {
            when (_streamMode.value) {
                SensorStreamMode.EDGE_IOT_SIMULATOR -> runEdgeSimulationLoop()
                SensorStreamMode.LIVE_DEVICE_MIC -> runLiveMicLoop()
            }
        }
    }

    fun stopStream() {
        _isStreaming.value = false
        streamingJob?.cancel()
        streamingJob = null
        try {
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
        } catch (e: Exception) {
            // ignore
        }
    }

    private suspend fun runEdgeSimulationLoop() {
        var phase = 0.0
        val sampleCount = 64
        val fftBinsCount = 32

        while (scope.isActive && _isStreaming.value) {
            val event = _activeStressEvent.value

            val (fundamentalHz, burstRate, damping, amplitudeScale) = when (event) {
                SimulatedStressEvent.NORMAL_FLOW -> {
                    // Slight variation around baseline 28.4 Hz
                    val freq = baselineFrequencyHz + (sin(phase * 0.1) * 0.05).toFloat()
                    Quadruple(freq, (1..4).random(), 0.018f, 0.25f)
                }
                SimulatedStressEvent.HEAVY_TRUCK_PASSAGE -> {
                    val freq = baselineFrequencyHz - 0.4f + (sin(phase * 0.3) * 0.1).toFloat()
                    Quadruple(freq, (8..15).random(), 0.028f, 0.75f)
                }
                SimulatedStressEvent.MICRO_FRACTURE_EVENT -> {
                    // Distinct fractional hertz downward shift! e.g. -1.8 Hz drop indicates stiffness decay
                    val freq = baselineFrequencyHz - 1.8f + (sin(phase * 0.4) * 0.12).toFloat()
                    Quadruple(freq, (35..48).random(), 0.058f, 0.92f)
                }
                SimulatedStressEvent.POST_TENSION_CABLE_PING -> {
                    val freq = baselineFrequencyHz + 4.2f
                    Quadruple(freq, (18..26).random(), 0.042f, 0.88f)
                }
            }

            // Generate synthetic time-series waveform chunk (-1.0 to 1.0)
            val waveform = FloatArray(sampleCount)
            for (i in 0 until sampleCount) {
                val t = phase + (i * 0.15)
                val primaryWave = sin(t * (fundamentalHz / 10f))
                val harmonicWave = 0.3 * sin(t * (fundamentalHz * 2.1 / 10f))
                val noise = ((Math.random() - 0.5) * 0.15)
                val microFractureAcousticSpike = if (event == SimulatedStressEvent.MICRO_FRACTURE_EVENT && i % 12 == 0) {
                    (Math.random() * 0.4 - 0.2)
                } else 0.0

                waveform[i] = ((primaryWave + harmonicWave + noise + microFractureAcousticSpike) * amplitudeScale)
                    .coerceIn(-1.0, 1.0).toFloat()
            }

            // Generate FFT spectrum bins centered around resonant modal peak
            val fft = FloatArray(fftBinsCount)
            val peakBinIndex = ((fundamentalHz / 60f) * fftBinsCount).toInt().coerceIn(1, fftBinsCount - 2)
            for (i in 0 until fftBinsCount) {
                val dist = Math.abs(i - peakBinIndex)
                val spectralPeak = Math.exp(-dist * dist / 3.0).toFloat() * amplitudeScale
                val floorNoise = (Math.random() * 0.05).toFloat()
                fft[i] = (spectralPeak + floorNoise).coerceIn(0f, 1.0f)
            }

            val deltaF = fundamentalHz - baselineFrequencyHz
            val rms = amplitudeScale * 0.52f
            val acousticEnergy = (rms * rms * burstRate * 0.45f).coerceAtLeast(0.1f)
            val microStrain = (amplitudeScale * 480f).coerceAtLeast(35f)

            val reading = AcousticReading(
                timestamp = System.currentTimeMillis(),
                rawWaveform = waveform,
                fftMagnitudes = fft,
                fundamentalFrequencyHz = fundamentalHz,
                baselineFrequencyHz = baselineFrequencyHz,
                frequencyShiftHz = deltaF,
                burstRateHitsPerSec = burstRate,
                dampingRatio = damping,
                rmsAmplitude = rms,
                acousticEnergyJoules = acousticEnergy,
                microStrainEstimate = microStrain,
                sensorNodeId = "NODE-${selectedPillarId.replace(" ", "")}-PZT",
                assetName = selectedAssetName
            )

            _currentReading.value = reading
            phase += 0.3
            delay(120L) // ~8 fps real-time continuous sensor refresh
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun runLiveMicLoop() {
        val sampleRate = 16000
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

        if (bufferSize <= 0) {
            // Audio hardware unavailable, fallback to edge simulator
            _streamMode.value = SensorStreamMode.EDGE_IOT_SIMULATOR
            runEdgeSimulationLoop()
            return
        }

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                _streamMode.value = SensorStreamMode.EDGE_IOT_SIMULATOR
                runEdgeSimulationLoop()
                return
            }

            audioRecord?.startRecording()
            val audioBuffer = ShortArray(bufferSize)

            while (scope.isActive && _isStreaming.value) {
                val readShorts = audioRecord?.read(audioBuffer, 0, bufferSize) ?: -1
                if (readShorts > 0) {
                    // Compute RMS amplitude
                    var sumSquare = 0.0
                    for (i in 0 until readShorts) {
                        val norm = audioBuffer[i] / 32768.0
                        sumSquare += norm * norm
                    }
                    val rms = sqrt(sumSquare / readShorts).toFloat()

                    // Downsample to 64 waveform points for rendering
                    val sampleCount = 64
                    val waveform = FloatArray(sampleCount)
                    val step = (readShorts / sampleCount).coerceAtLeast(1)
                    for (i in 0 until sampleCount) {
                        val idx = (i * step).coerceAtMost(readShorts - 1)
                        waveform[i] = (audioBuffer[idx] / 32768.0f).coerceIn(-1.0f, 1.0f)
                    }

                    // Estimate peak acoustic frequency via zero-crossing rate & simple DFT
                    var zeroCrossings = 0
                    for (i in 1 until readShorts) {
                        if ((audioBuffer[i] >= 0 && audioBuffer[i - 1] < 0) ||
                            (audioBuffer[i] < 0 && audioBuffer[i - 1] >= 0)
                        ) {
                            zeroCrossings++
                        }
                    }
                    val estimatedFreq = ((zeroCrossings * sampleRate) / (2.0f * readShorts)).coerceIn(12f, 150f)

                    // 32 FFT display bins
                    val fftBins = FloatArray(32)
                    val peakBin = ((estimatedFreq / 150f) * 32).toInt().coerceIn(1, 30)
                    for (b in 0 until 32) {
                        val dist = Math.abs(b - peakBin)
                        fftBins[b] = (rms * 2f * Math.exp(-dist * dist / 2.0)).toFloat().coerceIn(0f, 1f)
                    }

                    val deltaF = estimatedFreq - baselineFrequencyHz
                    val burstRate = (rms * 60f).toInt().coerceIn(0, 50)

                    val reading = AcousticReading(
                        timestamp = System.currentTimeMillis(),
                        rawWaveform = waveform,
                        fftMagnitudes = fftBins,
                        fundamentalFrequencyHz = estimatedFreq,
                        baselineFrequencyHz = baselineFrequencyHz,
                        frequencyShiftHz = deltaF,
                        burstRateHitsPerSec = burstRate,
                        dampingRatio = 0.02f + (rms * 0.04f),
                        rmsAmplitude = rms,
                        acousticEnergyJoules = rms * 2.5f,
                        microStrainEstimate = rms * 600f,
                        sensorNodeId = "PHONE-MIC-TEST",
                        assetName = "$selectedAssetName (Live Acoustic Capture)"
                    )
                    _currentReading.value = reading
                }
                delay(100L)
            }
        } catch (e: Exception) {
            // Permission or mic error, fallback safely
            _streamMode.value = SensorStreamMode.EDGE_IOT_SIMULATOR
            runEdgeSimulationLoop()
        }
    }

    private fun createInitialReading(): AcousticReading {
        val count = 64
        val waveform = FloatArray(count)
        val fft = FloatArray(32)
        for (i in 0 until count) {
            waveform[i] = (sin(i * 0.2) * 0.4).toFloat()
        }
        fft[12] = 0.85f
        fft[11] = 0.45f
        fft[13] = 0.50f

        return AcousticReading(
            timestamp = System.currentTimeMillis(),
            rawWaveform = waveform,
            fftMagnitudes = fft,
            fundamentalFrequencyHz = 26.6f,
            baselineFrequencyHz = 28.4f,
            frequencyShiftHz = -1.8f,
            burstRateHitsPerSec = 42,
            dampingRatio = 0.058f,
            rmsAmplitude = 0.48f,
            acousticEnergyJoules = 14.8f,
            microStrainEstimate = 380f,
            sensorNodeId = "NODE-P04B-PZT",
            assetName = "Bund Garden Bridge"
        )
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
