package com.example.vigil.detection

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.provider.Telephony
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.vigil.MainActivity

object DetectionOverlayService {
    private val mainHandler = Handler(Looper.getMainLooper())

    private var windowManager: WindowManager? = null
    private var overlayView: ComposeView? = null
    private var lifecycleOwner: OverlayLifecycleOwner? = null
    private var uiState by mutableStateOf<DetectionUiState?>(null)
    private var onSettled: (() -> Unit)? = null

    fun show(context: Context, state: DetectionUiState, onSettled: (() -> Unit)? = null) {
        val appContext = context.applicationContext
        mainHandler.post { showOnMainThread(appContext, state, onSettled) }
    }

    fun hide(context: Context) {
        mainHandler.post { removeOverlayAndStop() }
    }

    fun hasUsageAccess(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        @Suppress("DEPRECATION")
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun showOnMainThread(context: Context, state: DetectionUiState, settled: (() -> Unit)?) {
        if (!Settings.canDrawOverlays(context) || !smsAppIsForeground(context)) {
            settled?.invoke()
            return
        }
        onSettled = settled
        uiState = state
        if (windowManager == null) {
            windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        }
        if (overlayView == null) attachOverlay(context)
    }

    private fun smsAppIsForeground(context: Context): Boolean {
        if (!hasUsageAccess(context)) return true

        val usageStats = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val now = System.currentTimeMillis()
        val events = usageStats.queryEvents(now - 3_600_000, now)
        var lastForeground: String? = null
        val event = UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                lastForeground = event.packageName
            }
        }
        return lastForeground == Telephony.Sms.getDefaultSmsPackage(context)
    }

    private fun attachOverlay(context: Context) {
        val owner = OverlayLifecycleOwner().also { lifecycleOwner = it }

        val view = ComposeView(context).apply {
            setViewTreeLifecycleOwner(owner)
            setViewTreeSavedStateRegistryOwner(owner)
            setContent {
                uiState?.let { state ->
                    DetectionIndicator(
                        state = state,
                        onTap = {
                            openAnalysisScreen(context)
                            removeOverlayAndStop()
                        },
                        onDismissed = { removeOverlayAndStop() },
                    )
                }
            }
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = (12 * context.resources.displayMetrics.density).toInt()
            y = (112 * context.resources.displayMetrics.density).toInt()
        }

        windowManager?.addView(view, params)
        owner.moveTo(Lifecycle.State.RESUMED)
        overlayView = view
    }

    private fun openAnalysisScreen(context: Context) {
        val state = uiState
        context.startActivity(
            Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                putExtra(EXTRA_OPEN_ANALYSIS, true)
                putExtra(EXTRA_TOKEN, issueToken())
                putExtra(EXTRA_SEVERITY, state?.severity?.name)
                putExtra(EXTRA_MESSAGE, state?.message)
                putExtra(EXTRA_BODY, state?.body)
                state?.riskScore?.let { putExtra(EXTRA_RISK_SCORE, it) }
            }
        )
    }

    private fun removeOverlayAndStop() {
        overlayView?.let { view ->
            lifecycleOwner?.moveTo(Lifecycle.State.DESTROYED)
            windowManager?.removeView(view)
        }
        overlayView = null
        lifecycleOwner = null
        uiState = null
        val settled = onSettled
        onSettled = null
        settled?.invoke()
    }

    private class OverlayLifecycleOwner : LifecycleOwner, SavedStateRegistryOwner {
        private val lifecycleRegistry = LifecycleRegistry(this)
        private val savedStateRegistryController = SavedStateRegistryController.create(this)

        init {
            savedStateRegistryController.performRestore(null)
        }

        override val lifecycle: Lifecycle get() = lifecycleRegistry
        override val savedStateRegistry: SavedStateRegistry
            get() = savedStateRegistryController.savedStateRegistry

        fun moveTo(state: Lifecycle.State) {
            lifecycleRegistry.currentState = state
        }
    }

    const val EXTRA_SEVERITY = "com.example.vigil.detection.extra.SEVERITY"
    const val EXTRA_MESSAGE = "com.example.vigil.detection.extra.MESSAGE"
    const val EXTRA_RISK_SCORE = "com.example.vigil.detection.extra.RISK_SCORE"
    const val EXTRA_BODY = "com.example.vigil.detection.extra.BODY"

    const val EXTRA_OPEN_ANALYSIS = "com.example.vigil.detection.extra.OPEN_ANALYSIS"
    const val EXTRA_TOKEN = "com.example.vigil.detection.extra.TOKEN"

    @Volatile private var pendingToken: String? = null

    private fun issueToken(): String =
        java.util.UUID.randomUUID().toString().also { pendingToken = it }

    fun consumeToken(candidate: String?): Boolean {
        val expected = pendingToken ?: return false
        pendingToken = null
        return candidate == expected
    }
}
