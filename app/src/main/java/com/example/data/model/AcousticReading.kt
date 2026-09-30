package com.example.data.model

data class AcousticReading(
    val timestamp: Long = System.currentTimeMillis(),
    val rawWaveform: FloatArray, // Normalized waveform samples (-1.0f .. 1.0f)
    val fftMagnitudes: FloatArray, // Frequency bins (0 .. 120 Hz)
    val fundamentalFrequencyHz: Float,
    val baselineFrequencyHz: Float,
    val frequencyShiftHz: Float,
    val burstRateHitsPerSec: Int,
    val dampingRatio: Float,
    val rmsAmplitude: Float,
    val acousticEnergyJoules: Float,
    val microStrainEstimate: Float,
    val sensorNodeId: String,
    val assetName: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AcousticReading
        if (timestamp != other.timestamp) return false
        if (!rawWaveform.contentEquals(other.rawWaveform)) return false
        if (!fftMagnitudes.contentEquals(other.fftMagnitudes)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = timestamp.hashCode()
        result = 31 * result + rawWaveform.contentHashCode()
        result = 31 * result + fftMagnitudes.contentHashCode()
        return result
    }
}

data class GeminiStructuralAnalysisResult(
    val riskScore: Int, // 0 - 100
    val severityLevel: String, // CRITICAL, HIGH, MODERATE, NOMINAL
    val structuralLifespanReductionMonths: Int,
    val crackClassification: String,
    val physicsDiagnosis: String,
    val acousticSignaturesIdentified: List<String>,
    val immediateSafetyAction: String,
    val suggestedWorkOrderTitle: String,
    val requiredNdtTechnique: String,
    val isLiveGeminiCall: Boolean
)
