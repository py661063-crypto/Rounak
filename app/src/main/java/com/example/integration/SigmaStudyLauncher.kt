package com.example.integration

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import com.example.data.IntegrationConfig

sealed interface SigmaInstallStatus {
  data object NotConfigured : SigmaInstallStatus

  data class Installed(
    val packageName: String,
    val appLabel: String,
    val versionName: String?,
  ) : SigmaInstallStatus

  data class InstalledNoLauncher(val packageName: String) : SigmaInstallStatus

  data class NotInstalled(val packageName: String) : SigmaInstallStatus
}

sealed interface LaunchResult {
  data object Success : LaunchResult

  data object NotConfigured : LaunchResult

  data class AppNotInstalled(val packageName: String) : LaunchResult

  data class InvalidUrl(val url: String) : LaunchResult

  data class Error(val message: String) : LaunchResult
}

object SigmaStudyLauncher {

  fun detectInstallStatus(context: Context, packageName: String): SigmaInstallStatus {
    val trimmed = packageName.trim()
    if (!IntegrationConfig.isValidPackageName(trimmed)) {
      return SigmaInstallStatus.NotConfigured
    }

    val pm = context.packageManager
    val packageInfo = getPackageInfoCompat(pm, trimmed)
      ?: return SigmaInstallStatus.NotInstalled(trimmed)

    val launchIntent = pm.getLaunchIntentForPackage(trimmed)
    if (launchIntent == null) {
      return SigmaInstallStatus.InstalledNoLauncher(trimmed)
    }

    val label =
      packageInfo.applicationInfo?.let { appInfo ->
        runCatching { pm.getApplicationLabel(appInfo).toString() }.getOrNull()
      } ?: trimmed

    return SigmaInstallStatus.Installed(
      packageName = trimmed,
      appLabel = label,
      versionName = packageInfo.versionName,
    )
  }

  /**
   * Opens Sigma Study directly to its normal Home Screen using Android's standard
   * package launch intent (or an optional verified deep link if configured).
   *
   * Does not bypass login, activation codes, ads, or access restrictions, and
   * never attaches or shares private authentication tokens.
   */
  fun openSigmaStudyHome(context: Context, config: IntegrationConfig): LaunchResult {
    val pkg = config.sigmaPackageName.trim()
    if (!IntegrationConfig.isValidPackageName(pkg)) {
      return LaunchResult.NotConfigured
    }

    val pm = context.packageManager
    if (getPackageInfoCompat(pm, pkg) == null) {
      return LaunchResult.AppNotInstalled(pkg)
    }

    // Optional configurable deep link for future official API/deep-link support
    val deepLink = config.sigmaDeepLinkUri.trim()
    if (deepLink.isNotEmpty() && IntegrationConfig.isValidDeepLinkUri(deepLink)) {
      val deepLinkIntent =
        Intent(Intent.ACTION_VIEW, Uri.parse(deepLink)).apply {
          setPackage(pkg)
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
      if (deepLinkIntent.resolveActivity(pm) != null) {
        return startIntentSafely(context, deepLinkIntent)
      }
    }

    val launchIntent =
      pm.getLaunchIntentForPackage(pkg)?.apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      } ?: return LaunchResult.Error("No Home Screen launcher activity found for $pkg.")

    return startIntentSafely(context, launchIntent)
  }

  fun openHttpsUrl(context: Context, url: String): LaunchResult {
    val trimmed = url.trim()
    if (!IntegrationConfig.isValidHttpsUrl(trimmed)) {
      return LaunchResult.InvalidUrl(trimmed)
    }

    val intent =
      Intent(Intent.ACTION_VIEW, Uri.parse(trimmed)).apply {
        addCategory(Intent.CATEGORY_BROWSABLE)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
    return startIntentSafely(context, intent)
  }

  private fun startIntentSafely(context: Context, intent: Intent): LaunchResult {
    return try {
      context.startActivity(intent)
      LaunchResult.Success
    } catch (_: ActivityNotFoundException) {
      LaunchResult.Error("No compatible application found to handle this action.")
    } catch (securityException: SecurityException) {
      LaunchResult.Error(
        securityException.localizedMessage ?: "Permission denied while launching target activity."
      )
    } catch (exception: Exception) {
      LaunchResult.Error(exception.localizedMessage ?: "Unexpected error while launching.")
    }
  }

  @Suppress("DEPRECATION")
  private fun getPackageInfoCompat(pm: PackageManager, packageName: String): PackageInfo? {
    return try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        pm.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
      } else {
        pm.getPackageInfo(packageName, 0)
      }
    } catch (_: PackageManager.NameNotFoundException) {
      null
    } catch (_: Exception) {
      null
    }
  }
}
