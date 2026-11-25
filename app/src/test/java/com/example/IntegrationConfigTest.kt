package com.example

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.example.data.ConfigRepository
import com.example.data.IntegrationConfig
import com.example.ui.PriyStudyViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class IntegrationConfigTest {

  private val testDispatcher = StandardTestDispatcher()

  private class FakeConfigRepository : ConfigRepository {
    private val state = MutableStateFlow(IntegrationConfig())
    override val configFlow: Flow<IntegrationConfig> = state

    override suspend fun saveConfig(config: IntegrationConfig) {
      state.value = config
    }

    override suspend fun resetConfig() {
      state.value = IntegrationConfig()
    }
  }

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun `package name validation accepts valid Android IDs and rejects malformed values`() {
    assertTrue(IntegrationConfig.isValidPackageName("com.priy.study"))
    assertTrue(IntegrationConfig.isValidPackageName("org.example.sigma_study"))
    assertFalse(IntegrationConfig.isValidPackageName(""))
    assertFalse(IntegrationConfig.isValidPackageName("singleword"))
    assertFalse(IntegrationConfig.isValidPackageName("com.example..bad"))
    assertFalse(IntegrationConfig.isValidPackageName("123.invalid.pkg"))
  }

  @Test
  fun `https validation enforces https scheme and rejects insecure or malformed URLs`() {
    assertTrue(IntegrationConfig.isValidHttpsUrl("https://example.com"))
    assertTrue(IntegrationConfig.isValidHttpsUrl("https://short.example.org/abc"))
    assertFalse(IntegrationConfig.isValidHttpsUrl("http://example.com"))
    assertFalse(IntegrationConfig.isValidHttpsUrl("javascript:alert(1)"))
    assertFalse(IntegrationConfig.isValidHttpsUrl("https://localhost"))
    assertFalse(IntegrationConfig.isValidHttpsUrl("not a url"))
  }

  @Test
  fun `external callback URI confirms ad only after external shortener flow is initiated`() =
    runTest {
      val context = ApplicationProvider.getApplicationContext<Context>()
      val viewModel = PriyStudyViewModel(FakeConfigRepository(), context)
      advanceUntilIdle()

      val callbackUri = Uri.parse("priystudy://ad-callback?status=completed")
      // Ignored if external shortener flow was never initiated
      viewModel.handleCallbackUri(callbackUri)
      assertFalse(viewModel.uiState.value.adSessionState.isAdCompletionConfirmed)

      // Confirmed once external shortener was opened and callback returns status=completed
      viewModel.onExternalShortenerOpened()
      viewModel.handleCallbackUri(callbackUri)
      assertTrue(viewModel.uiState.value.adSessionState.isAdCompletionConfirmed)
    }
}
