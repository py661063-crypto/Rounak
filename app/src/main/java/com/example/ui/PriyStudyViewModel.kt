package com.example.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.ConfigRepository
import com.example.data.IntegrationConfig
import com.example.integration.SigmaInstallStatus
import com.example.integration.SigmaStudyLauncher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface RewardedAdLoadState {
  data object Idle : RewardedAdLoadState

  data object Loading : RewardedAdLoadState

  data object Ready : RewardedAdLoadState

  data object Showing : RewardedAdLoadState

  data class Failed(val message: String) : RewardedAdLoadState
}

data class AdSessionState(
  val rewardedAdLoadState: RewardedAdLoadState = RewardedAdLoadState.Idle,
  val isAdCompletionConfirmed: Boolean = false,
  val confirmedSourceSummary: String? = null,
  val dismissedWithoutReward: Boolean = false,
  val externalShortenerOpened: Boolean = false,
)

data class PriyStudyUiState(
  val config: IntegrationConfig = IntegrationConfig(),
  val installStatus: SigmaInstallStatus = SigmaInstallStatus.NotConfigured,
  val adSessionState: AdSessionState = AdSessionState(),
  val snackbarMessage: String? = null,
)

class PriyStudyViewModel(
  private val configRepository: ConfigRepository,
  private val appContext: Context,
) : ViewModel() {

  private val _uiState = MutableStateFlow(PriyStudyUiState())
  val uiState: StateFlow<PriyStudyUiState> = _uiState.asStateFlow()

  init {
    viewModelScope.launch {
      configRepository.configFlow.collect { config ->
        val status = SigmaStudyLauncher.detectInstallStatus(appContext, config.sigmaPackageName)
        _uiState.update { current ->
          current.copy(config = config, installStatus = status)
        }
      }
    }
  }

  fun refreshInstallStatus() {
    val pkg = _uiState.value.config.sigmaPackageName
    val status = SigmaStudyLauncher.detectInstallStatus(appContext, pkg)
    _uiState.update { it.copy(installStatus = status) }
  }

  fun startNewAdGateSession() {
    _uiState.update {
      it.copy(
        adSessionState = AdSessionState(rewardedAdLoadState = RewardedAdLoadState.Loading)
      )
    }
  }

  fun onRewardedAdLoading() {
    _uiState.update { current ->
      current.copy(
        adSessionState =
          current.adSessionState.copy(
            rewardedAdLoadState = RewardedAdLoadState.Loading,
            dismissedWithoutReward = false,
          )
      )
    }
  }

  fun onRewardedAdLoaded() {
    _uiState.update { current ->
      current.copy(
        adSessionState =
          current.adSessionState.copy(rewardedAdLoadState = RewardedAdLoadState.Ready)
      )
    }
  }

  fun onRewardedAdShowing() {
    _uiState.update { current ->
      current.copy(
        adSessionState =
          current.adSessionState.copy(
            rewardedAdLoadState = RewardedAdLoadState.Showing,
            dismissedWithoutReward = false,
          )
      )
    }
  }

  fun onRewardedAdFailed(message: String) {
    _uiState.update { current ->
      current.copy(
        adSessionState =
          current.adSessionState.copy(
            rewardedAdLoadState =
              RewardedAdLoadState.Failed(message.ifBlank { "Ad network unavailable" })
          )
      )
    }
  }

  /**
   * Called strictly by Google Mobile Ads OnUserEarnedRewardListener.
   * Never invoked by synthetic timers or fake events.
   */
  fun onAdMobRewardConfirmed(rewardType: String, amount: Int) {
    _uiState.update { current ->
      current.copy(
        adSessionState =
          current.adSessionState.copy(
            isAdCompletionConfirmed = true,
            confirmedSourceSummary = "AdMob Confirmed ($amount $rewardType)",
            dismissedWithoutReward = false,
          )
      )
    }
  }

  fun onRewardedAdDismissed() {
    _uiState.update { current ->
      val confirmed = current.adSessionState.isAdCompletionConfirmed
      current.copy(
        adSessionState =
          current.adSessionState.copy(
            rewardedAdLoadState = RewardedAdLoadState.Idle,
            dismissedWithoutReward = !confirmed,
          )
      )
    }
  }

  fun onExternalShortenerOpened() {
    _uiState.update { current ->
      current.copy(
        adSessionState = current.adSessionState.copy(externalShortenerOpened = true)
      )
    }
  }

  /**
   * Handles verified callback deep link (priystudy://ad-callback?status=completed)
   * when an external HTTPS URL shortener redirects back to Priy Study.
   */
  fun handleCallbackUri(uri: Uri?) {
    if (uri == null) return
    val isExpectedCallback =
      uri.scheme.equals("priystudy", ignoreCase = true) &&
        uri.host.equals("ad-callback", ignoreCase = true) &&
        uri.getQueryParameter("status").equals("completed", ignoreCase = true)

    if (isExpectedCallback && _uiState.value.adSessionState.externalShortenerOpened) {
      _uiState.update { current ->
        current.copy(
          adSessionState =
            current.adSessionState.copy(
              isAdCompletionConfirmed = true,
              confirmedSourceSummary = "External Partner Callback Confirmed",
              dismissedWithoutReward = false,
            )
        )
      }
    }
  }

  fun saveConfig(config: IntegrationConfig, savedMessage: String) {
    viewModelScope.launch {
      configRepository.saveConfig(config)
      val status = SigmaStudyLauncher.detectInstallStatus(appContext, config.sigmaPackageName)
      _uiState.update {
        it.copy(
          config = config,
          installStatus = status,
          snackbarMessage = savedMessage,
        )
      }
    }
  }

  fun resetConfig(resetMessage: String) {
    viewModelScope.launch {
      configRepository.resetConfig()
      _uiState.update {
        it.copy(
          config = IntegrationConfig(),
          installStatus = SigmaInstallStatus.NotConfigured,
          snackbarMessage = resetMessage,
        )
      }
    }
  }

  fun showMessage(message: String) {
    _uiState.update { it.copy(snackbarMessage = message) }
  }

  fun consumeSnackbar() {
    _uiState.update { it.copy(snackbarMessage = null) }
  }
}

class PriyStudyViewModelFactory(
  private val configRepository: ConfigRepository,
  private val appContext: Context,
) : ViewModelProvider.Factory {
  @Suppress("UNCHECKED_CAST")
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(PriyStudyViewModel::class.java)) {
      return PriyStudyViewModel(configRepository, appContext.applicationContext) as T
    }
    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
  }
}
