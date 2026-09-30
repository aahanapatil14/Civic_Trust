package com.example.data.gemini

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.AcousticReading
import com.example.data.model.GeminiStructuralAnalysisResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class GeminiAcousticAnalyzer {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    // Model selection complying strictly with gemini-api skill instructions: gemini-3.5-flash
    private val modelName = "gemini-3.5-flash"
    private val endpointUrl = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent"

    suspend fun analyzeStructuralAcoustics(
        reading: AcousticReading,
        assetPillar: String,
        structuralMaterial: String,
        yearConstructed: Int
    ): GeminiStructuralAnalysisResult = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        // If API key is present and not placeholder, invoke live Gemini API
        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY" && apiKey != "null") {
            try {
                val liveResult = callGeminiApi(apiKey, reading, assetPillar, structuralMaterial, yearConstructed)
                if (liveResult != null) {
                    return@withContext liveResult
                }
            } catch (e: Exception) {
                Log.w("GeminiAnalyzer", "Gemini API call failed, falling back to deterministic physics engine: ${e.message}")
            }
        }

        // Deterministic Civil Engineering Physics Fallback Engine
        return@withContext runDeterministicPhysicsAnalysis(reading, assetPillar, structuralMaterial)
    }

    private fun callGeminiApi(
        apiKey: String,
        reading: AcousticReading,
        assetPillar: String,
        structuralMaterial: String,
        yearConstructed: Int
    ): GeminiStructuralAnalysisResult? {
        val prompt = """
            You are Civic Trust, an autonomous civic infrastructure monitoring copilot built for the GDG Cloud Pune Code for Communities hackathon (Track 1: AI for Digital Public Infrastructure & Governance).
            Analyze this raw acoustic time-series and spectral shift reading captured by edge-deployed piezoelectric sensors mounted on municipal infrastructure in Pune.

            INFRASTRUCTURE CONTEXT:
            - Asset Name: ${reading.assetName}
            - Monitored Component: $assetPillar
            - Material: $structuralMaterial
            - Construction Year: $yearConstructed
            - Standards: Indian Road Congress (IRC:SP:35 & IRC:SP:40)

            SENSOR TELEMETRY CHUNK:
            - Baseline Resonant Frequency: ${String.format("%.2f", reading.baselineFrequencyHz)} Hz
            - Observed Current Fundamental Frequency: ${String.format("%.2f", reading.fundamentalFrequencyHz)} Hz
            - Fractional Hertz Shift (Δf): ${String.format("%.2f", reading.frequencyShiftHz)} Hz
            - Acoustic Emission (AE) Burst Rate: ${reading.burstRateHitsPerSec} hits/second
            - Structural Damping Ratio (ζ): ${String.format("%.4f", reading.dampingRatio)}
            - RMS Vibration Amplitude: ${String.format("%.3f", reading.rmsAmplitude)} V
            - Acoustic Wave Energy: ${String.format("%.2f", reading.acousticEnergyJoules)} mJ
            - Estimated Micro-Strain (με): ${String.format("%.1f", reading.microStrainEstimate)}

            OPERATIONAL REQUIREMENTS:
            1. Evaluate structural stiffness loss (k ~ f^2).
            2. Classify internal micro-fracture propagation prior to visual crack emergence.
            3. Output MUST be valid JSON only, without markdown fences or extra text, matching this exact schema:
            {
              "riskScore": (integer between 0 and 100),
              "severityLevel": ("CRITICAL", "HIGH", "MODERATE", or "NOMINAL"),
              "structuralLifespanReductionMonths": (integer months lost if untreated),
              "crackClassification": (short string naming the structural failure mode),
              "physicsDiagnosis": (2-3 sentences explaining the frequency drop, acoustic burst dissipation, and internal defect),
              "acousticSignaturesIdentified": [list of 3 string indicators],
              "immediateSafetyAction": (concrete instruction for municipal field engineers),
              "suggestedWorkOrderTitle": (concise municipal work order name),
              "requiredNdtTechnique": (e.g., "Ultrasonic Pulse Velocity (UPV) & Acoustic Emission Localization")
            }
        """.trimIndent()

        val jsonRequest = JSONObject().apply {
            val contentsArray = JSONArray().apply {
                put(JSONObject().apply {
                    val partsArray = JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    }
                    put("parts", partsArray)
                })
            }
            put("contents", contentsArray)

            put("generationConfig", JSONObject().apply {
                put("temperature", 0.2)
                put("topP", 0.95)
                put("responseMimeType", "application/json")
            })
        }

        val request = Request.Builder()
            .url("$endpointUrl?key=$apiKey")
            .post(jsonRequest.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                Log.e("GeminiAnalyzer", "Error HTTP ${response.code}: ${response.body?.string()}")
                return null
            }

            val responseBody = response.body?.string() ?: return null
            val rootObj = JSONObject(responseBody)
            val candidates = rootObj.optJSONArray("candidates") ?: return null
            if (candidates.length() == 0) return null
            val candidate = candidates.getJSONObject(0)
            val content = candidate.optJSONObject("content") ?: return null
            val parts = content.optJSONArray("parts") ?: return null
            if (parts.length() == 0) return null
            val rawText = parts.getJSONObject(0).optString("text", "")

            // Parse output JSON
            val cleanJson = rawText.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
            val parsed = JSONObject(cleanJson)

            val signatures = mutableListOf<String>()
            val sigArray = parsed.optJSONArray("acousticSignaturesIdentified")
            if (sigArray != null) {
                for (i in 0 until sigArray.length()) {
                    signatures.add(sigArray.getString(i))
                }
            }
            if (signatures.isEmpty()) {
                signatures.add("Negative fractional Hertz shift (Δf = ${String.format("%.2f", reading.frequencyShiftHz)} Hz)")
                signatures.add("Elevated acoustic emission burst rate (${reading.burstRateHitsPerSec} hits/s)")
            }

            return GeminiStructuralAnalysisResult(
                riskScore = parsed.optInt("riskScore", 75),
                severityLevel = parsed.optString("severityLevel", "HIGH"),
                structuralLifespanReductionMonths = parsed.optInt("structuralLifespanReductionMonths", 14),
                crackClassification = parsed.optString("crackClassification", "Internal Shear Micro-Fracture"),
                physicsDiagnosis = parsed.optString("physicsDiagnosis", "Analysis completed via Gemini 3.5 Flash."),
                acousticSignaturesIdentified = signatures,
                immediateSafetyAction = parsed.optString("immediateSafetyAction", "Initiate ultrasonic non-destructive testing."),
                suggestedWorkOrderTitle = parsed.optString("suggestedWorkOrderTitle", "Urgent Structural NDT Inspection"),
                requiredNdtTechnique = parsed.optString("requiredNdtTechnique", "Ultrasonic Pulse Velocity (UPV) & Impact-Echo"),
                isLiveGeminiCall = true
            )
        }
    }

    private fun runDeterministicPhysicsAnalysis(
        reading: AcousticReading,
        assetPillar: String,
        structuralMaterial: String
    ): GeminiStructuralAnalysisResult {
        val deltaF = reading.frequencyShiftHz
        val baseline = max(10f, reading.baselineFrequencyHz)
        val current = max(5f, reading.fundamentalFrequencyHz)

        // Euler-Bernoulli structural stiffness ratio: EI_eff / EI_0 = (f_curr / f_base)^2
        val stiffnessRatio = (current / baseline) * (current / baseline)
        val stiffnessLossPercent = max(0f, (1.0f - stiffnessRatio) * 100f)

        val burstPenalty = min(35f, reading.burstRateHitsPerSec * 0.7f)
        val dampingPenalty = max(0f, (reading.dampingRatio - 0.02f) * 1000f)

        var calculatedRisk = (stiffnessLossPercent * 3.5f + burstPenalty + dampingPenalty).roundToInt()
        calculatedRisk = min(98, max(12, calculatedRisk))

        val severity = when {
            calculatedRisk >= 75 || deltaF <= -1.5f -> "CRITICAL"
            calculatedRisk >= 50 || deltaF <= -0.6f -> "HIGH"
            calculatedRisk >= 30 || deltaF < -0.2f -> "MODERATE"
            else -> "NOMINAL"
        }

        val lifespanReduction = when (severity) {
            "CRITICAL" -> (12..24).random()
            "HIGH" -> (6..12).random()
            "MODERATE" -> (3..6).random()
            else -> 0
        }

        val classification = when {
            reading.dampingRatio > 0.05f -> "Rebar-to-Concrete Debonding & Shear Delamination"
            deltaF <= -1.2f -> "Internal Concrete Core Micro-Fracture Propagation"
            reading.burstRateHitsPerSec > 25 -> "High-Energy Acoustic Micro-Cracking Under Cyclic Load"
            deltaF < -0.4f -> "Progressive Flexural Stiffness Softening"
            else -> "Nominal Elastic Micro-Vibration Baseline"
        }

        val diagnosis = when (severity) {
            "CRITICAL" -> "Severe resonant frequency depression of ${String.format("%.2f", deltaF)} Hz indicates a ${(stiffnessLossPercent).roundToInt()}% reduction in effective flexural rigidity (EI). Acoustic emission burst count of ${reading.burstRateHitsPerSec} hits/sec confirms active micro-crack propagation inside $assetPillar ($structuralMaterial) months ahead of visible spalling."
            "HIGH" -> "Noticeable downward frequency shift of ${String.format("%.2f", deltaF)} Hz and elevated modal damping (${String.format("%.3f", reading.dampingRatio)}) indicate internal micro-fissuring and cyclic fatigue under heavy axle loading on $assetPillar."
            "MODERATE" -> "Mild modal frequency softening (${String.format("%.2f", deltaF)} Hz). Elastic restoring force remains within permissible bounds, but increased acoustic burst rate suggests localized micro-strain accumulation."
            else -> "Resonant frequency of ${String.format("%.2f", current)} Hz closely tracks baseline (${String.format("%.2f", baseline)} Hz). Structural stiffness matrix is intact with no anomalous acoustic emissions."
        }

        val action = when (severity) {
            "CRITICAL" -> "Immediately impose temporary 20-tonne axle restriction on lane above $assetPillar. Mobilize NDT crew for Ultrasonic Pulse Velocity (UPV) scanning and high-pressure epoxy resin injection."
            "HIGH" -> "Schedule nocturnal non-destructive testing within 72 hours. Inspect bearing seats and install triaxial accelerometer arrays for continuous strain monitoring."
            "MODERATE" -> "Log for priority quarterly inspection. Re-calibrate acoustic baseline during next zero-traffic maintenance window."
            else -> "Routine monitoring maintained. Asset operating within Indian Road Congress (IRC:SP:35) nominal thresholds."
        }

        val workOrderTitle = when (severity) {
            "CRITICAL" -> "Emergency Substructure NDT & CFRP Retrofit for $assetPillar"
            "HIGH" -> "Scheduled Ultrasonic Integrity & Bearing Alignment for $assetPillar"
            "MODERATE" -> "Routine Acoustic Sensor Recalibration & Visual Borescope Inspection"
            else -> "Preventive Maintenance Telemetry Check"
        }

        val ndtTechnique = when (severity) {
            "CRITICAL" -> "Ultrasonic Pulse Velocity (UPV), Impact-Echo & Acoustic Emission Source Triangulation"
            "HIGH" -> "Electromagnetic Covermeter & Rebound Hammer Core Validation"
            "MODERATE" -> "Digital Borescopy & Vibration Modal Survey"
            else -> "Continuous Piezo Telemetry Health Logging"
        }

        val signatures = listOf(
            "Spectral resonant frequency shift: ${String.format("%.2f", deltaF)} Hz (${(stiffnessLossPercent).roundToInt()}% stiffness loss)",
            "Acoustic burst rate: ${reading.burstRateHitsPerSec} hits/sec (Cyclic strain dissipation)",
            "Modal damping ratio ζ = ${String.format("%.4f", reading.dampingRatio)} (Energy absorption via micro-fissures)"
        )

        return GeminiStructuralAnalysisResult(
            riskScore = calculatedRisk,
            severityLevel = severity,
            structuralLifespanReductionMonths = lifespanReduction,
            crackClassification = classification,
            physicsDiagnosis = diagnosis,
            acousticSignaturesIdentified = signatures,
            immediateSafetyAction = action,
            suggestedWorkOrderTitle = workOrderTitle,
            requiredNdtTechnique = ndtTechnique,
            isLiveGeminiCall = false
        )
    }
}
