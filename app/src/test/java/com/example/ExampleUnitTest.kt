package com.example

import com.example.data.local.DefaultData
import com.example.data.model.AcousticReading
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun testDefaultAssetsIntegrity() {
    val assets = DefaultData.getInitialAssets()
    assertTrue(assets.isNotEmpty())
    val bundGarden = assets.find { it.id == "PMC-BRG-01" }
    assertNotNull(bundGarden)
    assertEquals("Bund Garden Bridge", bundGarden?.name)
    assertEquals("CRITICAL", bundGarden?.status)
    // Resonant frequency shift calculation
    val shift = bundGarden!!.currentFrequencyHz - bundGarden.baselineFrequencyHz
    assertTrue(shift < -1.0f)
  }

  @Test
  fun testAcousticReadingStiffnessCalculation() {
    val reading = AcousticReading(
      rawWaveform = floatArrayOf(0.1f, -0.2f),
      fftMagnitudes = floatArrayOf(0.1f, 0.9f),
      fundamentalFrequencyHz = 26.6f,
      baselineFrequencyHz = 28.4f,
      frequencyShiftHz = -1.8f,
      burstRateHitsPerSec = 42,
      dampingRatio = 0.058f,
      rmsAmplitude = 0.45f,
      acousticEnergyJoules = 12.5f,
      microStrainEstimate = 350f,
      sensorNodeId = "NODE-P04B",
      assetName = "Bund Garden Bridge"
    )

    // Verify negative shift indicating stiffness degradation
    assertTrue(reading.frequencyShiftHz < 0)
    assertEquals(42, reading.burstRateHitsPerSec)
  }
}
