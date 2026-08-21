package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.model.FlashcardEntity
import com.example.ui.components.FlipFlashcardView
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    val sampleCard = FlashcardEntity(
      bookId = 1L,
      chapterTitle = "Chương 1",
      front = "Quy tắc 1% mỗi ngày mang lại kết quả gì sau 1 năm?",
      back = "Tiến bộ gấp 37.78 lần.",
      keyConcept = "Lãi kép thói quen",
      difficulty = "Dễ",
      isMastered = false
    )

    composeTestRule.setContent {
      MyApplicationTheme {
        FlipFlashcardView(flashcard = sampleCard)
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
