package com.lyubishchev.timekeeper.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.lyubishchev.timekeeper.R
import com.lyubishchev.timekeeper.domain.AppMeta
import kotlinx.coroutines.delay

/**
 * 启动屏：先亮一下图标与应用名（按当前语言），期间主界面尚未组装，结束后再进入。
 * Splash gate: shows the launcher icon plus the localized app name, then hands over to the app.
 */
@Composable
fun SplashGate(content: @Composable () -> Unit) {
    var onSplash by rememberSaveable { mutableStateOf(true) }
    if (onSplash) {
        SplashScreen { onSplash = false }
    } else {
        content()
    }
}

@Composable
private fun SplashScreen(onFinished: () -> Unit) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Image(
                painter = painterResource(R.drawable.ic_launcher),
                contentDescription = null,
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(22.dp)),
            )
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )
            val days = AppMeta.daysTogether(context)
            Text(
                text = pluralStringResource(R.plurals.splash_days, days, days),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 10.dp),
            )
            Text(
                text = looyeaCredit(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp),
            )
            Spacer(modifier = Modifier.weight(1.35f))
            Text(
                text = stringResource(R.string.splash_local_only),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
    LaunchedEffect(Unit) {
        delay(900)
        onFinished()
    }
}

/** 署名行：LOOYEA 永远是斜体拉丁字，前后那截"出品"跟着界面语言走。 */
@Composable
private fun looyeaCredit(): AnnotatedString {
    val raw = stringResource(R.string.splash_credit)
    val before = raw.substringBefore("%1\$s")
    val after = raw.substringAfter("%1\$s", "")
    return remember(raw) {
        buildAnnotatedString {
            append(before)
            withStyle(SpanStyle(fontStyle = FontStyle.Italic, fontWeight = FontWeight.Medium)) {
                append("LOOYEA")
            }
            append(after)
        }
    }
}
