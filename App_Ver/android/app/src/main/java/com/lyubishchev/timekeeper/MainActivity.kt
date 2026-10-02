package com.lyubishchev.timekeeper

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.os.LocaleList
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.lyubishchev.timekeeper.i18n.AppLocale
import com.lyubishchev.timekeeper.ui.AppRoot
import com.lyubishchev.timekeeper.ui.SplashGate
import com.lyubishchev.timekeeper.ui.theme.TimekeeperTheme
import java.util.Locale

/**
 * 唯一 Activity，全部内容交给 Compose。
 * The single activity; Compose owns the whole UI.
 *
 * 语言靠重写 getResources 生效：EMUI 在 onCreate 之前就已碰过 Activity 的 resources，
 * applyOverrideConfiguration 在那里调用会直接闪退；而 attachBaseContext 里
 * createConfigurationContext 又会被随后的主题 Resources 覆盖掉，只有接管 getResources 能让
 * stringResource 与 context.getString 一起跟着语言走，同时 LocalContext 仍是 Activity 本身
 * （SAF 启动器一类依赖 Activity 的代码不受影响）。
 */
class MainActivity : ComponentActivity() {

    private var localizedResources: Resources? = null
    private var localizedFor: Locale? = null

    override fun attachBaseContext(newBase: Context) {
        AppLocale.refresh(newBase)
        super.attachBaseContext(newBase)
    }

    override fun getResources(): Resources {
        localizedResources?.takeIf { localizedFor == AppLocale.locale }?.let { return it }
        val themed = super.getResources()
        val config = Configuration(themed.configuration)
        config.setLocales(LocaleList(AppLocale.locale))
        return createConfigurationContext(config).resources.also {
            localizedResources = it
            localizedFor = AppLocale.locale
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            TimekeeperTheme {
                SplashGate { AppRoot() }
            }
        }
    }
}
