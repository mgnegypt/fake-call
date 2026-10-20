package com.mgneg.fakecall

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.AnticipateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.mgneg.fakecall.ui.intro.IntroScreen
import com.mgneg.fakecall.ui.nav.FakeCallRoot
import com.mgneg.fakecall.ui.theme.FakeCallTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val app = application as FakeCallApp
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)

        splash.setKeepOnScreenCondition { !app.initialized }
        splash.setOnExitAnimationListener { provider ->
            val view = provider.view
            ObjectAnimator.ofFloat(view, View.ALPHA, 1f, 0f).apply {
                duration = 350
                interpolator = AnticipateInterpolator()
                addListener(object : android.animation.AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: android.animation.Animator) {
                        provider.remove()
                    }
                })
                start()
            }
        }

        setContent {
            val dynamicColor by app.settings.dynamicColor.collectAsState(initial = false)
            var showIntro by remember { mutableStateOf<Boolean?>(null) }
            androidx.compose.runtime.LaunchedEffect(Unit) {
                showIntro = !app.settings.introShown.first()
            }
            FakeCallTheme(dynamicColor = dynamicColor) {
                when (showIntro) {
                    null -> Unit // splash still visible
                    true -> IntroScreen(onDone = {
                        showIntro = false
                        app.appScope.launch { app.settings.setIntroShown(true) }
                    })
                    false -> FakeCallRoot(app)
                }
            }
        }
    }
}
