package com.example.vigil.detection

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.provider.Settings
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

/**
 * Hosts the [DetectionIndicator] chip in a system overlay window so it can
 * appear on top of other apps (e.g. Android Messages) while Vigil is in the
 * background.
 *
 * Detection logic lives elsewhere; it drives this service through:
 * - [show] — display (or update) the chip for a detection result
 * - [hide] — remove the chip immediately
 *
 * Requires the SYSTEM_ALERT_WINDOW ("Display over other apps") permission;
 * if not granted the service stops itself without showing anything.
 */
class DetectionOverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private var overlayView: ComposeView? = null
    private var lifecycleOwner: OverlayLifecycleOwner? = null

    private var uiState by mutableStateOf<DetectionUiState?>(null)

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_SHOW -> {
                val state = intent.toDetectionUiState()
                if (state != null && Settings.canDrawOverlays(this)) {
                    uiState = state
                    if (overlayView == null) attachOverlay()
                } else {
                    stopSelf()
                }
            }

            ACTION_HIDE -> removeOverlayAndStop()
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun attachOverlay() {
        val owner = OverlayLifecycleOwner().also { lifecycleOwner = it }

        val view = ComposeView(this).apply {
            setViewTreeLifecycleOwner(owner)
            setViewTreeSavedStateRegistryOwner(owner)
            setContent {
                uiState?.let { state ->
                    DetectionIndicator(
                        state = state,
                        onTap = {
                            openAnalysisScreen()
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
            x = (12 * resources.displayMetrics.density).toInt()
            y = (112 * resources.displayMetrics.density).toInt()
        }

        windowManager.addView(view, params)
        owner.moveTo(Lifecycle.State.RESUMED)
        overlayView = view
    }

    private fun openAnalysisScreen() {
        val state = uiState
        startActivity(
            Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                putExtra(EXTRA_OPEN_ANALYSIS, true)
                putExtra(EXTRA_SEVERITY, state?.severity?.name)
                putExtra(EXTRA_MESSAGE, state?.message)
                state?.riskScore?.let { putExtra(EXTRA_RISK_SCORE, it) }
            }
        )
    }

    private fun removeOverlayAndStop() {
        overlayView?.let { view ->
            lifecycleOwner?.moveTo(Lifecycle.State.DESTROYED)
            windowManager.removeView(view)
        }
        overlayView = null
        lifecycleOwner = null
        uiState = null
        stopSelf()
    }

    override fun onDestroy() {
        overlayView?.let { view ->
            lifecycleOwner?.moveTo(Lifecycle.State.DESTROYED)
            windowManager.removeView(view)
            overlayView = null
            lifecycleOwner = null
        }
        super.onDestroy()
    }

    /**
     * Minimal lifecycle + saved-state owner so a ComposeView can live in a
     * WindowManager-managed window without an Activity.
     */
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

    companion object {
        private const val ACTION_SHOW = "com.example.vigil.detection.action.SHOW"
        private const val ACTION_HIDE = "com.example.vigil.detection.action.HIDE"

        private const val EXTRA_SEVERITY = "com.example.vigil.detection.extra.SEVERITY"
        private const val EXTRA_MESSAGE = "com.example.vigil.detection.extra.MESSAGE"
        private const val EXTRA_RISK_SCORE = "com.example.vigil.detection.extra.RISK_SCORE"

        /** Set on the MainActivity intent when the user taps the chip. */
        const val EXTRA_OPEN_ANALYSIS = "com.example.vigil.detection.extra.OPEN_ANALYSIS"

        /**
         * Show (or update) the detection chip over the current foreground app.
         * Call from detection logic when a suspicious message is found while
         * the user is in another app.
         */
        fun show(context: Context, state: DetectionUiState) {
            context.startService(
                Intent(context, DetectionOverlayService::class.java).apply {
                    action = ACTION_SHOW
                    putExtra(EXTRA_SEVERITY, state.severity.name)
                    putExtra(EXTRA_MESSAGE, state.message)
                    state.riskScore?.let { putExtra(EXTRA_RISK_SCORE, it) }
                }
            )
        }

        /** Hide the chip immediately (e.g. user returned to the Vigil app). */
        fun hide(context: Context) {
            context.startService(
                Intent(context, DetectionOverlayService::class.java).apply {
                    action = ACTION_HIDE
                }
            )
        }

        private fun Intent.toDetectionUiState(): DetectionUiState? {
            val message = getStringExtra(EXTRA_MESSAGE) ?: return null
            val severity = getStringExtra(EXTRA_SEVERITY)
                ?.let { runCatching { Severity.valueOf(it) }.getOrNull() }
                ?: Severity.UNKNOWN
            val riskScore = if (hasExtra(EXTRA_RISK_SCORE)) getIntExtra(EXTRA_RISK_SCORE, 0) else null
            return DetectionUiState(severity = severity, message = message, riskScore = riskScore)
        }
    }
}
