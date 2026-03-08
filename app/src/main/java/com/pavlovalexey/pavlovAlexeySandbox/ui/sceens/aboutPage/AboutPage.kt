package com.pavlovalexey.pavlovAlexeySandbox.ui.sceens.aboutPage

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.core.os.LocaleListCompat
import com.pavlovalexey.pavlovAlexeySandbox.R
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.components.AlexIconButton
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.components.SpacerHeight
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.components.WankerConfirmationDialog
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.dp12
import com.pavlovalexey.pavlovAlexeySandbox.ui.theme.dp16
import com.pavlovalexey.pavlovAlexeySandbox.utils.LanguagePrefs
import java.util.Locale

@Composable
fun AboutPage() {
    val context = LocalContext.current
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var languageMenuExpanded by remember { mutableStateOf(false) }

    val languages = listOf(
        LanguageOption("ru", stringResource(R.string.about_language_ru)),
        LanguageOption("en", stringResource(R.string.about_language_en)),
        LanguageOption("es", stringResource(R.string.about_language_es)),
        LanguageOption("hi", stringResource(R.string.about_language_hi)),
        LanguageOption("fr", stringResource(R.string.about_language_fr)),
        LanguageOption("pt", stringResource(R.string.about_language_pt)),
        LanguageOption("ja", stringResource(R.string.about_language_ja)),
    )
    val initialLanguage = remember { resolveAppLanguage(context) }
    var selectedLanguage by remember { mutableStateOf(initialLanguage) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(dp16),
        verticalArrangement = Arrangement.spacedBy(dp12)
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )
        SpacerHeight()
        Text(
            text = stringResource(R.string.about_app_text),
            style = MaterialTheme.typography.bodyMedium
        )
        SpacerHeight()

//        AlexIconButton(
//            text = stringResource(R.string.about_user_data_button),
//            onClick = {
//                Toast.makeText(
//                    context,
//                    context.getString(R.string.about_user_data_toast),
//                    Toast.LENGTH_LONG
//                ).show()
//            },
//            outlined = true
//        )

        AlexIconButton(
            text = stringResource(R.string.about_privacy_policy_button),
            onClick = { showPrivacyDialog = true },
            outlined = true
        )

        AlexIconButton(
            text = stringResource(R.string.about_support_button),
            onClick = {
                val supportMailIntent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse(
                        "mailto:a9633238@gmail.com?subject=${Uri.encode("Help with PixelWanker app")}"
                    )
                }

                val chooserIntent = Intent.createChooser(
                    supportMailIntent,
                    context.getString(R.string.about_support_chooser_title)
                )

                if (supportMailIntent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(chooserIntent)
                } else {
                    Toast.makeText(
                        context,
                        context.getString(R.string.about_support_no_email_app),
                        Toast.LENGTH_LONG
                    ).show()
                }
            },
            outlined = true
        )
        SpacerHeight()

        Text(
            text = stringResource(R.string.about_language_title),
            style = MaterialTheme.typography.bodyMedium
        )

        Box(modifier = Modifier.fillMaxWidth()) {
            val selectedLabel =
                languages.firstOrNull { it.code == selectedLanguage }?.label
                    ?: selectedLanguage

            AlexIconButton(
                text = selectedLabel,
                onClick = { languageMenuExpanded = true },
                outlined = true
            )

            DropdownMenu(
                expanded = languageMenuExpanded,
                onDismissRequest = { languageMenuExpanded = false }
            ) {
                languages.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.label) },
                        onClick = {
                            languageMenuExpanded = false
                            selectedLanguage = option.code
                            LanguagePrefs.setSelectedLanguage(context, option.code)
                            AppCompatDelegate.setApplicationLocales(
                                LocaleListCompat.forLanguageTags(option.code)
                            )
//                            Toast.makeText(
//                                context,
//                                context.getString(R.string.about_language_change_toast),
//                                Toast.LENGTH_LONG
//                            ).show()
                        }
                    )
                }
            }
        }
        SpacerHeight(60)
    }

    if (showPrivacyDialog) {
        WankerConfirmationDialog(
            onDismiss = { showPrivacyDialog = false },
            dialogText = stringResource(R.string.about_privacy_policy_text),
            onConfirm = { showPrivacyDialog = false },
            confirmText = stringResource(R.string.about_dialog_confirm),
            dismissText = stringResource(R.string.about_dialog_dismiss),
        )
    }
}

private fun resolveAppLanguage(context: Context): String {
    LanguagePrefs.getSelectedLanguage(context)?.let { return it }
    val appLocaleTags = AppCompatDelegate.getApplicationLocales().toLanguageTags()
    if (appLocaleTags.isNotBlank()) {
        val primaryTag = appLocaleTags.split(",").firstOrNull().orEmpty()
        val normalized = Locale.forLanguageTag(primaryTag).language
        if (normalized.isNotBlank()) {
            return normalized
        }
    }
    return Locale.getDefault().language
}

private data class LanguageOption(
    val code: String,
    val label: String,
)
