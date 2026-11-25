package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.BuildConfig
import com.example.R
import com.example.integration.SigmaInstallStatus
import com.example.ui.PriyStudyUiState
import com.example.ui.components.AdSection
import com.example.ui.components.PriyStudyBrandMark
import com.example.ui.components.StatusPill
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.MonoCodeTextStyle
import com.example.ui.theme.NavyBackground

@Composable
fun HomeScreen(
  uiState: PriyStudyUiState,
  onOpenSigmaStudyClick: () -> Unit,
  onOpenOfficialWebsiteClick: () -> Unit,
  onRefreshStatusClick: () -> Unit,
  onOpenSettingsClick: () -> Unit,
  onSnackbarDismissed: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val snackbarHostState = remember { SnackbarHostState() }

  LaunchedEffect(uiState.snackbarMessage) {
    val msg = uiState.snackbarMessage
    if (!msg.isNullOrBlank()) {
      snackbarHostState.showSnackbar(msg)
      onSnackbarDismissed()
    }
  }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    containerColor = MaterialTheme.colorScheme.background,
    contentWindowInsets = WindowInsets.safeDrawing,
    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
  ) { innerPadding ->
    Box(
      modifier = Modifier.fillMaxSize().padding(innerPadding),
      contentAlignment = Alignment.TopCenter,
    ) {
      LazyColumn(
        modifier =
          Modifier.fillMaxSize()
            .widthIn(max = 600.dp)
            .testTag("home_screen_list"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
      ) {
        item(key = "header") {
          HomeTopBar(onOpenSettingsClick = onOpenSettingsClick)
        }

        item(key = "welcome_hero") {
          WelcomeHeroCard()
        }

        item(key = "sigma_access_card") {
          SigmaStudyAccessCard(
            installStatus = uiState.installStatus,
            configuredPackage = uiState.config.sigmaPackageName,
            hasOfficialWebsite = uiState.config.hasValidOfficialWebsite,
            onOpenSigmaStudyClick = onOpenSigmaStudyClick,
            onOpenOfficialWebsiteClick = onOpenOfficialWebsiteClick,
            onRefreshStatusClick = onRefreshStatusClick,
            onConfigureClick = onOpenSettingsClick,
          )
        }

        item(key = "ad_section") {
          AdSection()
        }

        item(key = "about_version_section") {
          AboutVersionCard()
        }
      }
    }
  }
}

@Composable
private fun HomeTopBar(onOpenSettingsClick: () -> Unit) {
  Row(
    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    PriyStudyBrandMark()

    Surface(
      shape = RoundedCornerShape(14.dp),
      color = MaterialTheme.colorScheme.surface,
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
      IconButton(
        onClick = onOpenSettingsClick,
        modifier = Modifier.size(48.dp).testTag("settings_button"),
      ) {
        Icon(
          imageVector = Icons.Rounded.Settings,
          contentDescription = stringResource(R.string.action_open_settings),
          tint = MaterialTheme.colorScheme.onSurface,
        )
      }
    }
  }
}

@Composable
private fun WelcomeHeroCard() {
  Surface(
    modifier = Modifier.fillMaxWidth().testTag("welcome_hero_card"),
    shape = MaterialTheme.shapes.large,
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
  ) {
    Box(modifier = Modifier.fillMaxWidth().heightIn(min = 176.dp)) {
      Image(
        painter = painterResource(id = R.drawable.img_study_hero),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.matchParentSize(),
      )

      Box(
        modifier =
          Modifier.matchParentSize()
            .background(
              Brush.verticalGradient(
                colors =
                  listOf(
                    NavyBackground.copy(alpha = 0.45f),
                    NavyBackground.copy(alpha = 0.92f),
                  )
              )
            )
      )

      Column(
        modifier = Modifier.fillMaxWidth().padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        StatusPill(
          text = "EDUCATIONAL GATEWAY",
          containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
          contentColor = MaterialTheme.colorScheme.secondary,
        )

        Text(
          text = stringResource(R.string.welcome_title),
          style = MaterialTheme.typography.headlineMedium,
          color = Color.White,
        )

        Text(
          text = stringResource(R.string.welcome_subtitle),
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}

@Composable
private fun SigmaStudyAccessCard(
  installStatus: SigmaInstallStatus,
  configuredPackage: String,
  hasOfficialWebsite: Boolean,
  onOpenSigmaStudyClick: () -> Unit,
  onOpenOfficialWebsiteClick: () -> Unit,
  onRefreshStatusClick: () -> Unit,
  onConfigureClick: () -> Unit,
) {
  Surface(
    modifier = Modifier.fillMaxWidth().testTag("sigma_study_card"),
    shape = MaterialTheme.shapes.large,
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 2.dp,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(
          modifier = Modifier.weight(1f),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Box(
            modifier =
              Modifier.size(46.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Rounded.School,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.secondary,
              modifier = Modifier.size(24.dp),
            )
          }

          Column {
            Text(
              text = stringResource(R.string.sigma_card_title),
              style = MaterialTheme.typography.titleLarge,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
              text = stringResource(R.string.sigma_card_subtitle),
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }

        IconButton(
          onClick = onRefreshStatusClick,
          modifier = Modifier.size(48.dp).testTag("refresh_status_button"),
        ) {
          Icon(
            imageVector = Icons.Rounded.Refresh,
            contentDescription = stringResource(R.string.action_refresh_status),
            tint = MaterialTheme.colorScheme.secondary,
          )
        }
      }

      // Live installation status row
      val (statusText, statusBg, statusFg) =
        when (installStatus) {
          is SigmaInstallStatus.Installed ->
            Triple(
              stringResource(R.string.sigma_status_installed),
              MaterialTheme.colorScheme.tertiaryContainer,
              MaterialTheme.colorScheme.onTertiaryContainer,
            )
          is SigmaInstallStatus.InstalledNoLauncher ->
            Triple(
              stringResource(R.string.sigma_status_no_launcher),
              AmberContainer,
              AmberWarning,
            )
          is SigmaInstallStatus.NotInstalled ->
            Triple(
              stringResource(R.string.sigma_status_not_installed),
              MaterialTheme.colorScheme.errorContainer,
              MaterialTheme.colorScheme.onErrorContainer,
            )
          SigmaInstallStatus.NotConfigured ->
            Triple(
              stringResource(R.string.sigma_status_not_configured),
              AmberContainer,
              AmberWarning,
            )
        }

      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
      ) {
        Column(
          modifier = Modifier.fillMaxWidth().padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Text(
              text = stringResource(R.string.sigma_package_label),
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            StatusPill(
              text = statusText,
              containerColor = statusBg,
              contentColor = statusFg,
            )
          }

          if (configuredPackage.isNotBlank()) {
            Text(
              text =
                when (installStatus) {
                  is SigmaInstallStatus.Installed ->
                    "${installStatus.packageName}${installStatus.versionName?.let { " • v$it" } ?: ""}"
                  else -> configuredPackage
                },
              style = MonoCodeTextStyle,
              color = MaterialTheme.colorScheme.onSurface,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          } else {
            Text(
              text = stringResource(R.string.sigma_package_unconfigured_hint),
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }

      // Helpful notice when not installed or not configured
      when (installStatus) {
        is SigmaInstallStatus.NotInstalled -> {
          HelpfulNoticeBanner(
            message = stringResource(R.string.sigma_not_installed_message, installStatus.packageName),
            actionLabel = stringResource(R.string.action_configure_integration),
            onActionClick = onConfigureClick,
          )
        }
        is SigmaInstallStatus.InstalledNoLauncher -> {
          HelpfulNoticeBanner(
            message = stringResource(R.string.sigma_no_launcher_message, installStatus.packageName),
            actionLabel = stringResource(R.string.action_configure_integration),
            onActionClick = onConfigureClick,
          )
        }
        SigmaInstallStatus.NotConfigured -> {
          HelpfulNoticeBanner(
            message = stringResource(R.string.sigma_package_unconfigured_hint),
            actionLabel = stringResource(R.string.action_configure_integration),
            onActionClick = onConfigureClick,
          )
        }
        is SigmaInstallStatus.Installed -> {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Icon(
              imageVector = Icons.Rounded.CheckCircle,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.tertiary,
              modifier = Modifier.size(18.dp),
            )
            Text(
              text = "${installStatus.appLabel} detected on this device.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }

      // Primary Electric Blue CTA
      Button(
        onClick = onOpenSigmaStudyClick,
        modifier =
          Modifier.fillMaxWidth()
            .heightIn(min = 52.dp)
            .testTag("open_sigma_study_button"),
        shape = MaterialTheme.shapes.small,
        colors =
          ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
          ),
      ) {
        Icon(
          imageVector = Icons.Rounded.RocketLaunch,
          contentDescription = null,
          modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
          text = stringResource(R.string.action_open_sigma_study),
          style = MaterialTheme.typography.labelLarge,
        )
      }

      // Optional Official Website button
      OutlinedButton(
        onClick = onOpenOfficialWebsiteClick,
        modifier =
          Modifier.fillMaxWidth()
            .heightIn(min = 48.dp)
            .testTag("official_website_button"),
        shape = MaterialTheme.shapes.small,
        border =
          BorderStroke(
            width = 1.dp,
            color =
              if (hasOfficialWebsite) MaterialTheme.colorScheme.secondary
              else MaterialTheme.colorScheme.outline,
          ),
      ) {
        Icon(
          imageVector = Icons.Rounded.Language,
          contentDescription = null,
          modifier = Modifier.size(18.dp),
          tint = MaterialTheme.colorScheme.secondary,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = stringResource(R.string.action_official_website),
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.width(6.dp))
        Icon(
          imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
          contentDescription = null,
          modifier = Modifier.size(16.dp),
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}

@Composable
private fun HelpfulNoticeBanner(
  message: String,
  actionLabel: String,
  onActionClick: () -> Unit,
) {
  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = MaterialTheme.shapes.small,
    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Icon(
        imageVector = Icons.Rounded.ErrorOutline,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.secondary,
        modifier = Modifier.size(20.dp),
      )
      Text(
        text = message,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.weight(1f),
      )
      TextButton(
        onClick = onActionClick,
        modifier = Modifier.heightIn(min = 48.dp).testTag("configure_integration_button"),
      ) {
        Icon(
          imageVector = Icons.Rounded.Tune,
          contentDescription = null,
          modifier = Modifier.size(16.dp),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = actionLabel, style = MaterialTheme.typography.labelMedium)
      }
    }
  }
}

@Composable
private fun AboutVersionCard() {
  Surface(
    modifier = Modifier.fillMaxWidth().testTag("about_version_card"),
    shape = MaterialTheme.shapes.medium,
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(18.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Icon(
          imageVector = Icons.Rounded.Info,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.secondary,
          modifier = Modifier.size(20.dp),
        )
        Text(
          text = stringResource(R.string.about_section_title),
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.onSurface,
        )
      }

      Text(
        text = stringResource(R.string.about_compliance_body),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )

      Spacer(modifier = Modifier.height(2.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text =
            stringResource(
              R.string.about_app_version_format,
              BuildConfig.VERSION_NAME,
              BuildConfig.VERSION_CODE,
            ),
          style = MonoCodeTextStyle,
          color = MaterialTheme.colorScheme.secondary,
        )
        Text(
          text = BuildConfig.APPLICATION_ID,
          style = MonoCodeTextStyle,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}
