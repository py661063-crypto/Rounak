package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.R
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

// Official Google Mobile Ads sample banner unit ID. Replace for production release.
private const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"

private sealed interface BannerAdState {
  data object Loading : BannerAdState

  data object Loaded : BannerAdState

  data class Failed(val message: String) : BannerAdState
}

@Composable
fun AdSection(modifier: Modifier = Modifier) {
  val isInspection = LocalInspectionMode.current
  var retryCount by remember { mutableIntStateOf(0) }
  var adState by remember(retryCount) { mutableStateOf<BannerAdState>(BannerAdState.Loading) }

  Surface(
    modifier = modifier.fillMaxWidth().testTag("ad_section"),
    shape = MaterialTheme.shapes.medium,
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 2.dp,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier =
              Modifier.clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(horizontal = 8.dp, vertical = 3.dp)
          ) {
            Text(
              text = stringResource(R.string.ad_section_badge),
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = stringResource(R.string.ad_section_label),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }

        if (adState is BannerAdState.Failed) {
          TextButton(
            onClick = { retryCount++ },
            modifier = Modifier.heightIn(min = 48.dp).testTag("retry_banner_ad_button"),
          ) {
            Icon(
              imageVector = Icons.Rounded.Refresh,
              contentDescription = stringResource(R.string.ad_retry_button),
              modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = stringResource(R.string.ad_retry_button),
              style = MaterialTheme.typography.labelMedium,
            )
          }
        }
      }

      BoxWithConstraints(
        modifier =
          Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .heightIn(min = 64.dp),
        contentAlignment = Alignment.Center,
      ) {
        val containerWidthDp = maxWidth.value.toInt().coerceAtLeast(300)

        if (isInspection) {
          AdFallbackContent(
            title = stringResource(R.string.ad_unavailable_title),
            subtitle = "Banner Preview (${containerWidthDp}dp)",
          )
        } else {
          key(retryCount, containerWidthDp) {
            AdMobBannerHost(
              adWidthDp = containerWidthDp,
              onAdLoaded = { adState = BannerAdState.Loaded },
              onAdFailed = { err -> adState = BannerAdState.Failed(err) },
            )
          }

          when (val state = adState) {
            BannerAdState.Loading -> {
              Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
              ) {
                CircularProgressIndicator(
                  modifier = Modifier.size(18.dp),
                  strokeWidth = 2.dp,
                  color = MaterialTheme.colorScheme.primary,
                )
                Text(
                  text = stringResource(R.string.ad_loading_text),
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }
            is BannerAdState.Failed -> {
              AdFallbackContent(
                title = stringResource(R.string.ad_unavailable_title),
                subtitle = state.message,
              )
            }
            BannerAdState.Loaded -> Unit
          }
        }
      }
    }
  }
}

@Composable
private fun AdMobBannerHost(
  adWidthDp: Int,
  onAdLoaded: () -> Unit,
  onAdFailed: (String) -> Unit,
) {
  val context = LocalContext.current
  val adView =
    remember(context, adWidthDp) {
      runCatching {
          AdView(context).apply {
            adUnitId = TEST_BANNER_AD_UNIT_ID
            val adaptiveSize =
              AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidthDp)
            setAdSize(adaptiveSize)
            adListener =
              object : AdListener() {
                override fun onAdLoaded() {
                  onAdLoaded()
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                  onAdFailed(adError.message.ifBlank { "Ad network unavailable" })
                }
              }
            loadAd(AdRequest.Builder().build())
          }
        }
        .onFailure { throwable ->
          onAdFailed(throwable.localizedMessage ?: "Google Mobile Ads unavailable in environment")
        }
        .getOrNull()
    }

  if (adView != null) {
    DisposableEffect(adView) { onDispose { adView.destroy() } }

    AndroidView(
      factory = { adView },
      modifier = Modifier.fillMaxWidth().testTag("admob_banner_view"),
    )
  }
}

@Composable
private fun AdFallbackContent(title: String, subtitle: String) {
  Row(
    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Box(
      modifier =
        Modifier.size(38.dp)
          .clip(RoundedCornerShape(10.dp))
          .background(MaterialTheme.colorScheme.secondaryContainer),
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        imageVector = Icons.Rounded.Campaign,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSecondaryContainer,
        modifier = Modifier.size(20.dp),
      )
    }
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}
