package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.DataStoreConfigRepository
import com.example.integration.LaunchResult
import com.example.integration.SigmaStudyLauncher
import com.example.ui.PriyStudyViewModel
import com.example.ui.PriyStudyViewModelFactory
import com.example.ui.screens.AdvertisementScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.PriyStudyTheme
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val ROUTE_HOME = "home"
private const val ROUTE_ADVERTISEMENT = "advertisement"
private const val ROUTE_SETTINGS = "settings"

class MainActivity : ComponentActivity() {

  private lateinit var viewModel: PriyStudyViewModel

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Edge-to-edge dark navy status/navigation bars
    enableEdgeToEdge(
      statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
      navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
    )

    // Initialize Google Mobile Ads SDK off the main thread
    lifecycleScope.launch(Dispatchers.IO) {
      runCatching { MobileAds.initialize(applicationContext) {} }
    }

    val repository = DataStoreConfigRepository(applicationContext)
    viewModel =
      ViewModelProvider(this, PriyStudyViewModelFactory(repository, applicationContext))[
        PriyStudyViewModel::class.java
      ]

    viewModel.handleCallbackUri(intent?.data)

    setContent {
      PriyStudyTheme {
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        val navController = rememberNavController()
        val lifecycleOwner = LocalLifecycleOwner.current

        // Automatically refresh Sigma Study installation detection when returning to app
        DisposableEffect(lifecycleOwner) {
          val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
              viewModel.refreshInstallStatus()
            }
          }
          lifecycleOwner.lifecycle.addObserver(observer)
          onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
        }

        NavHost(navController = navController, startDestination = ROUTE_HOME) {
          composable(ROUTE_HOME) {
            HomeScreen(
              uiState = uiState,
              onOpenSigmaStudyClick = {
                viewModel.startNewAdGateSession()
                navController.navigate(ROUTE_ADVERTISEMENT)
              },
              onOpenOfficialWebsiteClick = {
                if (!uiState.config.hasValidOfficialWebsite) {
                  viewModel.showMessage(getString(R.string.error_website_not_configured))
                } else {
                  when (
                    val result =
                      SigmaStudyLauncher.openHttpsUrl(
                        this@MainActivity,
                        uiState.config.officialWebsiteUrl,
                      )
                  ) {
                    LaunchResult.Success -> Unit
                    is LaunchResult.InvalidUrl ->
                      viewModel.showMessage(getString(R.string.error_invalid_https_url))
                    is LaunchResult.Error -> viewModel.showMessage(result.message)
                    else -> viewModel.showMessage(getString(R.string.error_no_browser))
                  }
                }
              },
              onRefreshStatusClick = { viewModel.refreshInstallStatus() },
              onOpenSettingsClick = { navController.navigate(ROUTE_SETTINGS) },
              onSnackbarDismissed = viewModel::consumeSnackbar,
            )
          }

          composable(ROUTE_ADVERTISEMENT) {
            AdvertisementScreen(
              uiState = uiState,
              onNavigateBack = { navController.popBackStack() },
              onOpenSettings = { navController.navigate(ROUTE_SETTINGS) },
              onRewardedAdLoading = viewModel::onRewardedAdLoading,
              onRewardedAdLoaded = viewModel::onRewardedAdLoaded,
              onRewardedAdShowing = viewModel::onRewardedAdShowing,
              onRewardedAdFailed = viewModel::onRewardedAdFailed,
              onAdMobRewardConfirmed = viewModel::onAdMobRewardConfirmed,
              onRewardedAdDismissed = viewModel::onRewardedAdDismissed,
              onOpenExternalShortener = { url ->
                when (val result = SigmaStudyLauncher.openHttpsUrl(this@MainActivity, url)) {
                  LaunchResult.Success -> viewModel.onExternalShortenerOpened()
                  is LaunchResult.InvalidUrl ->
                    viewModel.showMessage(getString(R.string.error_invalid_https_url))
                  is LaunchResult.Error -> viewModel.showMessage(result.message)
                  else -> viewModel.showMessage(getString(R.string.error_no_browser))
                }
              },
              onContinueToSigmaStudy = {
                when (
                  val result =
                    SigmaStudyLauncher.openSigmaStudyHome(this@MainActivity, uiState.config)
                ) {
                  LaunchResult.Success -> Unit
                  LaunchResult.NotConfigured ->
                    viewModel.showMessage(getString(R.string.sigma_package_unconfigured_hint))
                  is LaunchResult.AppNotInstalled ->
                    viewModel.showMessage(
                      getString(R.string.sigma_not_installed_message, result.packageName)
                    )
                  is LaunchResult.Error -> viewModel.showMessage(result.message)
                  is LaunchResult.InvalidUrl ->
                    viewModel.showMessage(getString(R.string.error_launch_failed))
                }
              },
              onSnackbarDismissed = viewModel::consumeSnackbar,
            )
          }

          composable(ROUTE_SETTINGS) {
            SettingsScreen(
              initialConfig = uiState.config,
              onNavigateBack = { navController.popBackStack() },
              onSaveConfig = { updatedConfig ->
                viewModel.saveConfig(updatedConfig, getString(R.string.snackbar_config_saved))
                navController.popBackStack()
              },
              onResetConfig = {
                viewModel.resetConfig(getString(R.string.snackbar_config_reset))
              },
            )
          }
        }
      }
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    viewModel.handleCallbackUri(intent.data)
  }
}
