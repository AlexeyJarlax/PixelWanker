    package com.pavlovalexey.pavlovAlexeySandbox

    import android.app.Application
    import com.pavlovalexey.pavlovAlexeySandbox.utils.ToastExt
//    import timber.log.Timber

    class MyApplication : Application() {
        override fun onCreate() {
            super.onCreate()
            ToastExt.init(this)
//            if (BuildConfig.DEBUG) {
//                Timber.plant(Timber.DebugTree())
//            } else {
//                Timber.plant(ReleaseTree())
//            }
        }
    }
