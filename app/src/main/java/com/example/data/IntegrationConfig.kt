package com.example.data

import java.net.URI

/**
 * Configurable integration settings for Priy Study.
 *
 * All external identifiers and URLs default to empty/unconfigured rather than
 * inventing third-party package names or API endpoints. Users and developers
 * can configure and verify them in Integration Settings.
 */
data class IntegrationConfig(
  val sigmaPackageName: String = DEFAULT_SIGMA_PACKAGE_NAME,
  val sigmaDeepLinkUri: String = DEFAULT_SIGMA_DEEP_LINK_URI,
  val officialWebsiteUrl: String = DEFAULT_OFFICIAL_WEBSITE_URL,
  val externalShortenerUrl: String = DEFAULT_EXTERNAL_SHORTENER_URL,
) {
  val hasValidSigmaPackage: Boolean
    get() = isValidPackageName(sigmaPackageName)

  val hasValidOfficialWebsite: Boolean
    get() = isValidHttpsUrl(officialWebsiteUrl)

  val hasValidExternalShortener: Boolean
    get() = isValidHttpsUrl(externalShortenerUrl)

  val hasValidDeepLink: Boolean
    get() = isValidDeepLinkUri(sigmaDeepLinkUri)

  companion object {
    // Intentionally left empty: configure your verified Sigma Study package name
    // (for example, from Google Play URL ?id=<package> or `adb shell pm list packages`).
    const val DEFAULT_SIGMA_PACKAGE_NAME = ""
    const val DEFAULT_SIGMA_DEEP_LINK_URI = ""
    const val DEFAULT_OFFICIAL_WEBSITE_URL = ""
    const val DEFAULT_EXTERNAL_SHORTENER_URL = ""

    private val PACKAGE_NAME_REGEX = Regex("^[a-zA-Z][a-zA-Z0-9_]*(\\.[a-zA-Z][a-zA-Z0-9_]*)+$")
    private val UNSAFE_URI_SCHEMES = setOf("javascript", "file", "content", "data", "vbscript")

    fun isValidPackageName(value: String): Boolean {
      val trimmed = value.trim()
      if (trimmed.isEmpty() || trimmed.length > 200) return false
      return PACKAGE_NAME_REGEX.matches(trimmed)
    }

    fun isValidHttpsUrl(value: String): Boolean {
      val trimmed = value.trim()
      if (trimmed.isEmpty() || trimmed.any { it.isWhitespace() }) return false
      return runCatching {
          val uri = URI(trimmed)
          val scheme = uri.scheme?.lowercase()
          val host = uri.host
          scheme == "https" && !host.isNullOrBlank() && host.contains('.')
        }
        .getOrDefault(false)
    }

    fun isValidDeepLinkUri(value: String): Boolean {
      val trimmed = value.trim()
      if (trimmed.isEmpty() || trimmed.any { it.isWhitespace() }) return false
      return runCatching {
          val uri = URI(trimmed)
          val scheme = uri.scheme?.lowercase() ?: return@runCatching false
          scheme.isNotBlank() &&
            scheme !in UNSAFE_URI_SCHEMES &&
            (!uri.host.isNullOrBlank() || !uri.schemeSpecificPart.isNullOrBlank())
        }
        .getOrDefault(false)
    }
  }
}
