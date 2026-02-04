    package com.pavlovalexey.pavlovAlexeySandbox

    import android.app.Application
    import androidx.appcompat.app.AppCompatDelegate
    import androidx.core.os.LocaleListCompat
    import com.pavlovalexey.pavlovAlexeySandbox.utils.LanguagePrefs
    import com.pavlovalexey.pavlovAlexeySandbox.utils.ToastExt

    class MyApplication : Application() {
        override fun onCreate() {
            super.onCreate()
            LanguagePrefs.getSelectedLanguage(this)?.let { languageCode ->
                AppCompatDelegate.setApplicationLocales(
                    LocaleListCompat.forLanguageTags(languageCode)
                )
            }
            ToastExt.init(this)
        }
    }
