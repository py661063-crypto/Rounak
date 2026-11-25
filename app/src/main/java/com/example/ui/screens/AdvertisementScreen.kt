package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.AdSessionState
import com.example.ui.PriyStudyUiState
import com.example.ui.RewardedAdLoadState
import com.example.ui.components.AdSection
import com.example.ui.components.StatusPill
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.MonoCodeTextStyle
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

// Official Google Mobile Ads sample rewarded unit ID. Replace for production release.
private const val TEST_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvertisementScreen(
  uiState: PriyStudyUiState,
  onNavigateBack: () -> Unit,
  onOpenSettings: () -> Unit,
  onRewardedAdLoading: () -> Unit,
  onRewardedAdLoaded: () -> Unit,
  onRewardedAdShowing: () -> Unit,
  onRewardedAdFailed: (String) -> Unit,
  onAdMobRewardConfirmed: (String, Int) -> Unit,
  onRewardedAdDismissed: () -> Unit,
  onOpenExternalShortener: (String) -> Unit,
  onContinueToSigmaStudy: () -> Unit,
  onSnackbarDismissed: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val isInspection = LocalInspectionMode.current
  val snackbarHostState = remember { SnackbarHostState() }
  var reloadTrigger by remember { mutableIntStateOf(0) }
  var loadedRewardedAd by remember { mutableStateOf<RewardedAd?>(null) }

  LaunchedEffect(uiState.snackbarMessage) {
    val msg = uiState.snackbarMessage
    if (!msg.isNullOrBlank()) {
      snackbarHostState.showSnackbar(msg)
      onSnackbarDismissed()
    }
  }

  // Load real Google Mobile Ads RewardedAd (never fakes impressions or rewards)
  LaunchedEffect(reloadTrigger) {
    if (isInspection) return@LaunchedEffect
    loadedRewardedAd = null
    onRewardedAdLoading()
    runCatching {
        RewardedAd.load(
          context,
          TEST_REWARDED_AD_UNIT_ID,
          AdRequest.Builder().build(),
          object : RewardedAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedAd) {
              loadedRewardedAd = ad
              onRewardedAdLoaded()
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
              loadedRewardedAd = null
              onRewardedAdFailed(loadAdError.message.ifBlank { "Ad network unavailable" })
            }
          },
        )
      }
      .onFailure { throwable ->
        loadedRewardedAd = null
        onRewardedAdFailed(throwable.localizedMessage ?: "AdMob SDK unavailable")
      }
  }

  DisposableEffect(Unit) {
    onDispose { loadedRewardedAd = null }
  }

  val adState = uiState.adSessionState

  Scaffold(
    modifier = modifier.fillMaxSize(),
    containerColor = MaterialTheme.colorScheme.background,
    contentWindowInsets = WindowInsets.safeDrawing,
    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = stringResource(R.string.ad_screen_title),
            style = MaterialTheme.typography.titleLarge,
          )
        },
        navigationIcon = {
          IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.size(48.dp).testTag("ad_back_button"),
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
              contentDescription = stringResource(R.string.action_navigate_back),
            )
          }
        },
        colors =
          TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
          ),
      )
    },
  ) { innerPadding ->
    Box(
      modifier = Modifier.fillMaxSize().padding(innerPadding),
      contentAlignment = Alignment.TopCenter,
    ) {
      LazyColumn(
        modifier =
          Modifier.fillMaxSize()
            .widthIn(max = 600.dp)
            .testTag("advertisement_screen_list"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        item(key = "workflow_steps") {
          WorkflowProgressRow(isConfirmed = adState.isAdCompletionConfirmed)
        }

        item(key = "ad_subtitle") {
          Text(
            text = stringResource(R.string.ad_screen_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }

        item(key = "rewarded_verification_card") {
          RewardedAdVerificationCard(
            adSessionState = adState,
            canShowAd = loadedRewardedAd != null && !adState.isAdCompletionConfirmed,
            onWatchAdClick = {
              val activity = context.findActivity()
              val ad = loadedRewardedAd
              if (activity == null || ad == null) {
                onRewardedAdFailed("No host Activity available to present ad")
                return@RewardedAdVerificationCard
              }

              ad.fullScreenContentCallback =
                object : FullScreenContentCallback() {
                  override fun onAdShowedFullScreenContent() {
                    onRewardedAdShowing()
                  }

                  override fun onAdDismissedFullScreenContent() {
                    loadedRewardedAd = null
                    onRewardedAdDismissed()
                  }

                  override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    loadedRewardedAd = null
                    onRewardedAdFailed(adError.message)
                  }
                }

              ad.show(activity) { rewardItem ->
                onAdMobRewardConfirmed(rewardItem.type, rewardItem.amount)
              }
            },
            onRetryLoadClick = { reloadTrigger++ },
          )
        }

        item(key = "banner_ad_section") {
          AdSection()
        }

        item(key = "external_shortener_card") {
          ExternalShortenerCard(
            shortenerUrl = uiState.config.externalShortenerUrl,
            hasValidShortener = uiState.config.hasValidExternalShortener,
            shortenerOpened = adState.externalShortenerOpened,
            isConfirmed = adState.isAdCompletionConfirmed,
            onOpenShortenerClick = {
              onOpenExternalShortener(uiState.config.externalShortenerUrl)
            },
            onConfigureClick = onOpenSettings,
          )
        }

        item(key = "continue_cta") {
          ContinueActionCard(
            isAdCompletionConfirmed = adState.isAdCompletionConfirmed,
            confirmedSummary = adState.confirmedSourceSummary,
            onContinueClick = onContinueToSigmaStudy,
          )
        }
      }
    }
  }
}

@Composable
private fun WorkflowProgressRow(isConfirmed: Boolean) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    StepChip(
      label = stringResource(R.string.ad_workflow_step_1),
      active = true,
      completed = isConfirmed,
      modifier = Modifier.weight(1f),
    )
    StepChip(
      label = stringResource(R.string.ad_workflow_step_2),
      active = isConfirmed,
      completed = isConfirmed,
      modifier = Modifier.weight(1f),
    )
    StepChip(
      label = stringResource(R.string.ad_workflow_step_3),
      active = isConfirmed,
      completed = false,
      modifier = Modifier.weight(1f),
    )
  }
}

@Composable
private fun StepChip(
  label: String,
  active: Boolean,
  completed: Boolean,
  modifier: Modifier = Modifier,
) {
  val bg =
    when {
      completed -> MaterialTheme.colorScheme.tertiaryContainer
      active -> MaterialTheme.colorScheme.primaryContainer
      else -> MaterialTheme.colorScheme.surface
    }
  val fg =
    when {
      completed -> MaterialTheme.colorScheme.onTertiaryContainer
      active -> MaterialTheme.colorScheme.onPrimaryContainer
      else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(10.dp),
    color = bg,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
  ) {
    Box(
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
      contentAlignment = Alignment.Center,
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = fg,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

@Composable
private fun RewardedAdVerificationCard(
  adSessionState: AdSessionState,
  canShowAd: Boolean,
  onWatchAdClick: () -> Unit,
  onRetryLoadClick: () -> Unit,
) {
  Surface(
    modifier = Modifier.fillMaxWidth().testTag("rewarded_ad_card"),
    shape = MaterialTheme.shapes.large,
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 2.dp,
    border =
      BorderStroke(
        width = 1.dp,
        color =
          if (adSessionState.isAdCompletionConfirmed) MaterialTheme.colorScheme.tertiary
          else MaterialTheme.colorScheme.outline,
      ),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Text(
          text = stringResource(R.string.ad_rewarded_card_title),
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.onSurface,
        )

        if (adSessionState.isAdCompletionConfirmed) {
          StatusPill(
            text = "CONFIRMED",
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
          )
        }
      }

      when (val loadState = adSessionState.rewardedAdLoadState) {
        RewardedAdLoadState.Idle -> {
          if (adSessionState.isAdCompletionConfirmed) {
            ConfirmedBanner(summary = adSessionState.confirmedSourceSummary)
          } else if (adSessionState.dismissedWithoutReward) {
            WarningBanner(text = stringResource(R.string.ad_rewarded_dismissed_early))
            RetryAdButton(onRetryLoadClick = onRetryLoadClick)
          }
        }
        RewardedAdLoadState.Loading -> {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            CircularProgressIndicator(
              modifier = Modifier.size(20.dp),
              strokeWidth = 2.dp,
              color = MaterialTheme.colorScheme.primary,
            )
            Text(
              text = stringResource(R.string.ad_rewarded_loading),
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
        RewardedAdLoadState.Ready -> {
          if (adSessionState.isAdCompletionConfirmed) {
            ConfirmedBanner(summary = adSessionState.confirmedSourceSummary)
          } else {
            Text(
              text = stringResource(R.string.ad_rewarded_ready),
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.secondary,
            )
            Button(
              onClick = onWatchAdClick,
              enabled = canShowAd,
              modifier =
                Modifier.fillMaxWidth()
                  .heightIn(min = 50.dp)
                  .testTag("watch_ad_button"),
              shape = MaterialTheme.shapes.small,
            ) {
              Icon(
                imageVector = Icons.Rounded.PlayCircle,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = stringResource(R.string.action_watch_approved_ad),
                style = MaterialTheme.typography.labelLarge,
              )
            }
          }
        }
        RewardedAdLoadState.Showing -> {
          Text(
            text = stringResource(R.string.ad_rewarded_showing),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary,
          )
        }
        is RewardedAdLoadState.Failed -> {
          if (adSessionState.isAdCompletionConfirmed) {
            ConfirmedBanner(summary = adSessionState.confirmedSourceSummary)
          } else {
            WarningBanner(text = stringResource(R.string.ad_rewarded_failed, loadState.message))
            RetryAdButton(onRetryLoadClick = onRetryLoadClick)
          }
        }
      }
    }
  }
}

@Composable
private fun ConfirmedBanner(summary: String?) {
  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = MaterialTheme.shapes.small,
    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Icon(
        imageVector = Icons.Rounded.Verified,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.tertiary,
        modifier = Modifier.size(22.dp),
      )
      Column {
        Text(
          text = stringResource(R.string.ad_rewarded_confirmed),
          style = MaterialTheme.typography.titleSmall,
          color = MaterialTheme.colorScheme.onSurface,
        )
        if (!summary.isNullOrBlank()) {
          Text(
            text = summary,
            style = MonoCodeTextStyle,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
          )
        }
      }
    }
  }
}

@Composable
private fun WarningBanner(text: String) {
  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = MaterialTheme.shapes.small,
    color = AmberContainer.copy(alpha = 0.55f),
    border = BorderStroke(1.dp, AmberWarning.copy(alpha = 0.5f)),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Icon(
        imageVector = Icons.Rounded.ErrorOutline,
        contentDescription = null,
        tint = AmberWarning,
        modifier = Modifier.size(20.dp),
      )
      Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface,
      )
    }
  }
}

@Composable
private fun RetryAdButton(onRetryLoadClick: () -> Unit) {
  OutlinedButton(
    onClick = onRetryLoadClick,
    modifier =
      Modifier.fillMaxWidth()
        .heightIn(min = 48.dp)
        .testTag("retry_rewarded_ad_button"),
    shape = MaterialTheme.shapes.small,
  ) {
    Icon(
      imageVector = Icons.Rounded.Refresh,
      contentDescription = null,
      modifier = Modifier.size(18.dp),
    )
    Spacer(modifier = Modifier.width(8.dp))
    Text(
      text = stringResource(R.string.action_retry_ad_load),
      style = MaterialTheme.typography.labelLarge,
    )
  }
}

@Composable
private fun ExternalShortenerCard(
  shortenerUrl: String,
  hasValidShortener: Boolean,
  shortenerOpened: Boolean,
  isConfirmed: Boolean,
  onOpenShortenerClick: () -> Unit,
  onConfigureClick: () -> Unit,
) {
  Surface(
    modifier = Modifier.fillMaxWidth().testTag("external_shortener_card"),
    shape = MaterialTheme.shapes.medium,
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Icon(
            imageVector = Icons.Rounded.Link,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(20.dp),
          )
          Text(
            text = stringResource(R.string.shortener_card_title),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
          )
        }

        if (!hasValidShortener) {
          TextButton(
            onClick = onConfigureClick,
            modifier = Modifier.heightIn(min = 48.dp),
          ) {
            Icon(
              imageVector = Icons.Rounded.Tune,
              contentDescription = null,
              modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = stringResource(R.string.action_configure_integration))
          }
        }
      }

      if (hasValidShortener) {
        Text(
          text = stringResource(R.string.shortener_configured_subtitle, shortenerUrl),
          style = MonoCodeTextStyle,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
        )

        if (shortenerOpened && !isConfirmed) {
          Text(
            text = stringResource(R.string.shortener_waiting_callback),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary,
          )
        }

        OutlinedButton(
          onClick = onOpenShortenerClick,
          modifier =
            Modifier.fillMaxWidth()
              .heightIn(min = 48.dp)
              .testTag("open_external_shortener_button"),
          shape = MaterialTheme.shapes.small,
        ) {
          Text(
            text = stringResource(R.string.action_open_shortener_url),
            style = MaterialTheme.typography.labelLarge,
          )
          Spacer(modifier = Modifier.width(8.dp))
          Icon(
            imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
          )
        }
      } else {
        Text(
          text = stringResource(R.string.shortener_unconfigured_subtitle),
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}

@Composable
private fun ContinueActionCard(
  isAdCompletionConfirmed: Boolean,
  confirmedSummary: String?,
  onContinueClick: () -> Unit,
) {
  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = MaterialTheme.shapes.large,
    color =
      if (isAdCompletionConfirmed) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
      else MaterialTheme.colorScheme.surface,
    border =
      BorderStroke(
        width = 1.dp,
        color =
          if (isAdCompletionConfirmed) MaterialTheme.colorScheme.primary
          else MaterialTheme.colorScheme.outline,
      ),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(18.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Box(
          modifier =
            Modifier.size(36.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(
                if (isAdCompletionConfirmed) MaterialTheme.colorScheme.tertiaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
              ),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector =
              if (isAdCompletionConfirmed) Icons.Rounded.CheckCircle else Icons.Rounded.Lock,
            contentDescription = null,
            tint =
              if (isAdCompletionConfirmed) MaterialTheme.colorScheme.tertiary
              else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
          )
        }
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text =
              if (isAdCompletionConfirmed) stringResource(R.string.action_continue_to_sigma)
              else stringResource(R.string.action_complete_ad_first),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
          )
          if (!confirmedSummary.isNullOrBlank()) {
            Text(
              text = confirmedSummary,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.secondary,
            )
          }
        }
      }

      Button(
        onClick = onContinueClick,
        enabled = isAdCompletionConfirmed,
        modifier =
          Modifier.fillMaxWidth()
            .heightIn(min = 52.dp)
            .testTag("continue_to_sigma_button"),
        shape = MaterialTheme.shapes.small,
        colors =
          ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
          ),
      ) {
        Text(
          text =
            if (isAdCompletionConfirmed) stringResource(R.string.action_continue_to_sigma)
            else stringResource(R.string.action_complete_ad_first),
          style = MaterialTheme.typography.labelLarge,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
          imageVector =
            if (isAdCompletionConfirmed) Icons.AutoMirrored.Rounded.ArrowForward
            else Icons.Rounded.Lock,
          contentDescription = null,
          modifier = Modifier.size(18.dp),
        )
      }
    }
  }
}

private tailrec fun Context.findActivity(): Activity? =
  when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
  }
