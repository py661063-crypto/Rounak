package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.HelpOutline
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.IntegrationConfig
import com.example.ui.theme.MonoCodeTextStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
  initialConfig: IntegrationConfig,
  onNavigateBack: () -> Unit,
  onSaveConfig: (IntegrationConfig) -> Unit,
  onResetConfig: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var packageName by rememberSaveable(initialConfig) { mutableStateOf(initialConfig.sigmaPackageName) }
  var deepLinkUri by rememberSaveable(initialConfig) { mutableStateOf(initialConfig.sigmaDeepLinkUri) }
  var officialWebsite by rememberSaveable(initialConfig) { mutableStateOf(initialConfig.officialWebsiteUrl) }
  var shortenerUrl by rememberSaveable(initialConfig) { mutableStateOf(initialConfig.externalShortenerUrl) }

  val isPackageValid =
    packageName.isBlank() || IntegrationConfig.isValidPackageName(packageName)
  val isDeepLinkValid =
    deepLinkUri.isBlank() || IntegrationConfig.isValidDeepLinkUri(deepLinkUri)
  val isWebsiteValid =
    officialWebsite.isBlank() || IntegrationConfig.isValidHttpsUrl(officialWebsite)
  val isShortenerValid =
    shortenerUrl.isBlank() || IntegrationConfig.isValidHttpsUrl(shortenerUrl)

  val canSave = isPackageValid && isDeepLinkValid && isWebsiteValid && isShortenerValid

  Scaffold(
    modifier = modifier.fillMaxSize(),
    containerColor = MaterialTheme.colorScheme.background,
    contentWindowInsets = WindowInsets.safeDrawing,
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.titleLarge,
          )
        },
        navigationIcon = {
          IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.size(48.dp).testTag("settings_back_button"),
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
        modifier = Modifier.fillMaxSize().widthIn(max = 600.dp),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        item(key = "settings_subtitle") {
          Text(
            text = stringResource(R.string.settings_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }

        item(key = "settings_form") {
          Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
          ) {
            Column(
              modifier = Modifier.fillMaxWidth().padding(18.dp),
              verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
              OutlinedTextField(
                value = packageName,
                onValueChange = { packageName = it },
                label = { Text(stringResource(R.string.settings_sigma_package_label)) },
                placeholder = { Text(stringResource(R.string.settings_sigma_package_placeholder)) },
                isError = !isPackageValid,
                supportingText =
                  if (!isPackageValid) {
                    { Text(stringResource(R.string.error_invalid_package_name)) }
                  } else null,
                textStyle = MonoCodeTextStyle,
                singleLine = true,
                keyboardOptions =
                  KeyboardOptions(
                    keyboardType = KeyboardType.Ascii,
                    imeAction = ImeAction.Next,
                  ),
                modifier = Modifier.fillMaxWidth().testTag("sigma_package_input"),
              )

              OutlinedTextField(
                value = deepLinkUri,
                onValueChange = { deepLinkUri = it },
                label = { Text(stringResource(R.string.settings_sigma_deeplink_label)) },
                placeholder = { Text(stringResource(R.string.settings_sigma_deeplink_placeholder)) },
                isError = !isDeepLinkValid,
                supportingText =
                  if (!isDeepLinkValid) {
                    { Text(stringResource(R.string.error_invalid_deeplink)) }
                  } else null,
                textStyle = MonoCodeTextStyle,
                singleLine = true,
                keyboardOptions =
                  KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Next,
                  ),
                modifier = Modifier.fillMaxWidth().testTag("sigma_deeplink_input"),
              )

              OutlinedTextField(
                value = officialWebsite,
                onValueChange = { officialWebsite = it },
                label = { Text(stringResource(R.string.settings_official_website_label)) },
                placeholder = {
                  Text(stringResource(R.string.settings_official_website_placeholder))
                },
                isError = !isWebsiteValid,
                supportingText =
                  if (!isWebsiteValid) {
                    { Text(stringResource(R.string.error_invalid_https_url)) }
                  } else null,
                textStyle = MonoCodeTextStyle,
                singleLine = true,
                keyboardOptions =
                  KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Next,
                  ),
                modifier = Modifier.fillMaxWidth().testTag("official_website_input"),
              )

              OutlinedTextField(
                value = shortenerUrl,
                onValueChange = { shortenerUrl = it },
                label = { Text(stringResource(R.string.settings_shortener_url_label)) },
                placeholder = { Text(stringResource(R.string.settings_shortener_url_placeholder)) },
                isError = !isShortenerValid,
                supportingText =
                  if (!isShortenerValid) {
                    { Text(stringResource(R.string.error_invalid_https_url)) }
                  } else null,
                textStyle = MonoCodeTextStyle,
                singleLine = true,
                keyboardOptions =
                  KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Done,
                  ),
                modifier = Modifier.fillMaxWidth().testTag("shortener_url_input"),
              )

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
              ) {
                OutlinedButton(
                  onClick = {
                    packageName = ""
                    deepLinkUri = ""
                    officialWebsite = ""
                    shortenerUrl = ""
                    onResetConfig()
                  },
                  modifier =
                    Modifier.weight(1f)
                      .heightIn(min = 48.dp)
                      .testTag("reset_settings_button"),
                  shape = MaterialTheme.shapes.small,
                ) {
                  Icon(
                    imageVector = Icons.Rounded.RestartAlt,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(text = stringResource(R.string.action_reset_settings))
                }

                Button(
                  onClick = {
                    onSaveConfig(
                      IntegrationConfig(
                        sigmaPackageName = packageName.trim(),
                        sigmaDeepLinkUri = deepLinkUri.trim(),
                        officialWebsiteUrl = officialWebsite.trim(),
                        externalShortenerUrl = shortenerUrl.trim(),
                      )
                    )
                  },
                  enabled = canSave,
                  modifier =
                    Modifier.weight(1.4f)
                      .heightIn(min = 48.dp)
                      .testTag("save_settings_button"),
                  shape = MaterialTheme.shapes.small,
                ) {
                  Icon(
                    imageVector = Icons.Rounded.Save,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(text = stringResource(R.string.action_save_settings))
                }
              }
            }
          }
        }

        item(key = "verification_guide") {
          Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
          ) {
            Column(
              modifier = Modifier.fillMaxWidth().padding(18.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Icon(
                  imageVector = Icons.Rounded.HelpOutline,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.secondary,
                  modifier = Modifier.size(20.dp),
                )
                Text(
                  text = stringResource(R.string.settings_verification_title),
                  style = MaterialTheme.typography.titleSmall,
                  color = MaterialTheme.colorScheme.onSurface,
                )
              }

              Text(
                text = stringResource(R.string.settings_verification_steps),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }
      }
    }
  }
}
