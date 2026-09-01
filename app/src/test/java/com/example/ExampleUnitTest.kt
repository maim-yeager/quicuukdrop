package com.example

import com.example.data.model.StorageUsageInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun storageUsageInfo_calculations_areCorrect() {
    val info = StorageUsageInfo(
      quickDropBytes = 53_700_000L, // 53.7 MB
      totalDeviceBytes = 128L * 1024 * 1024 * 1024, // 128 GB
      freeDeviceBytes = 64L * 1024 * 1024 * 1024,
      receivedFilesCount = 4
    )

    assertTrue(info.quickDropFormatted.contains("MB"))
    assertTrue(info.totalDeviceFormatted.contains("GB"))
    assertTrue(info.usageFraction > 0f && info.usageFraction < 1f)
    assertEquals(4, info.receivedFilesCount)
  }
}

