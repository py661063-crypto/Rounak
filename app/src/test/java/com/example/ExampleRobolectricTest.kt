package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToKey
import androidx.test.core.app.ApplicationProvider
import com.example.data.IntegrationConfig
import com.example.integration.SigmaInstallStatus
import com.example.ui.AdSessionState
import com.example.ui.PriyStudyUiState
import com.example.ui.RewardedAdLoadState
import com.example.ui.screens.AdvertisementScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.PriyStudyTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun `app identity uses Priy Study name and com priy study applicationId`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    assertEquals("Priy Study", context.getString(R.string.app_name))
    assertEquals("com.priy.study", BuildConfig.APPLICATION_ID)
  }

  @Test
  fun `home screen displays welcome message, sigma study card, ad section, and about card`() {
    var openSigmaClicked = false

    composeTestRule.setContent {
      PriyStudyTheme {
        HomeScreen(
          uiState =
            PriyStudyUiState(
              config =
                IntegrationConfig(
                  sigmaPackageName = "com.vendor.sigmastudy",
                  officialWebsiteUrl = "https://example.com",
                ),
              installStatus = SigmaInstallStatus.NotInstalled("com.vendor.sigmastudy"),
            ),
          onOpenSigmaStudyClick = { openSigmaClicked = true },
          onOpenOfficialWebsiteClick = {},
          onRefreshStatusClick = {},
          onOpenSettingsClick = {},
          onSnackbarDismissed = {},
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Welcome to Priy Study").assertIsDisplayed()
    composeTestRule.onNodeWithTag("home_screen_list").performScrollToKey("sigma_access_card")
    composeTestRule.onNodeWithTag("sigma_study_card").assertIsDisplayed()
    composeTestRule.onNodeWithTag("open_sigma_study_button").assertIsDisplayed().performClick()
    assertTrue(openSigmaClicked)

    composeTestRule.onNodeWithTag("home_screen_list").performScrollToKey("ad_section")
    composeTestRule.onNodeWithTag("ad_section").assertIsDisplayed()

    composeTestRule.onNodeWithTag("home_screen_list").performScrollToKey("about_version_section")
    composeTestRule.onNodeWithTag("about_version_card").assertIsDisplayed()
  }

  @Test
  fun `continue button stays disabled until ad network confirms completion`() {
    composeTestRule.setContent {
      PriyStudyTheme {
        AdvertisementScreen(
          uiState =
            PriyStudyUiState(
              adSessionState =
                AdSessionState(
                  rewardedAdLoadState = RewardedAdLoadState.Ready,
                  isAdCompletionConfirmed = false,
                )
            ),
          onNavigateBack = {},
          onOpenSettings = {},
          onRewardedAdLoading = {},
          onRewardedAdLoaded = {},
          onRewardedAdShowing = {},
          onRewardedAdFailed = {},
          onAdMobRewardConfirmed = { _, _ -> },
          onRewardedAdDismissed = {},
          onOpenExternalShortener = {},
          onContinueToSigmaStudy = {},
          onSnackbarDismissed = {},
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("advertisement_screen_list").performScrollToKey("continue_cta")
    composeTestRule.onNodeWithTag("continue_to_sigma_button").assertIsNotEnabled()
  }

  @Test
  fun `continue button enables and launches sigma study after confirmed ad completion`() {
    var launchedSigma = false

    composeTestRule.setContent {
      PriyStudyTheme {
        AdvertisementScreen(
          uiState =
            PriyStudyUiState(
              adSessionState =
                AdSessionState(
                  rewardedAdLoadState = RewardedAdLoadState.Idle,
                  isAdCompletionConfirmed = true,
                  confirmedSourceSummary = "AdMob Confirmed (1 Reward)",
                )
            ),
          onNavigateBack = {},
          onOpenSettings = {},
          onRewardedAdLoading = {},
          onRewardedAdLoaded = {},
          onRewardedAdShowing = {},
          onRewardedAdFailed = {},
          onAdMobRewardConfirmed = { _, _ -> },
          onRewardedAdDismissed = {},
          onOpenExternalShortener = {},
          onContinueToSigmaStudy = { launchedSigma = true },
          onSnackbarDismissed = {},
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("advertisement_screen_list").performScrollToKey("continue_cta")
    composeTestRule
      .onNodeWithTag("continue_to_sigma_button")
      .assertIsEnabled()
      .performClick()
    assertTrue(launchedSigma)
  }
}
