package com.cbo.sfa_utils.helper

import android.content.Context
import android.content.Intent
import android.content.ActivityNotFoundException
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import disable_battery_optimizations.managers.BatteryOptimizationManager
import disable_battery_optimizations.managers.KillerManager
import disable_battery_optimizations.models.OptimizationVerificationStatus
import disable_battery_optimizations.utils.BatteryOptimizationUtil
import disable_battery_optimizations.utils.LogUtils
import disable_battery_optimizations.utils.PrefKeys
import disable_battery_optimizations.utils.PrefUtils

/**
 * Modernized BatteryOptimizationHelper.
 * Displays interactive text-based guides in the native flow to replace outdated images.
 * 
 * IMPORTANT: Must call initSetup() once during activity initialization for proper
 * ActivityResult handling. If not called, falls back to lifecycle observer method.
 */
object BatteryOptimizationHelper {

    private var batteryLauncher: ActivityResultLauncher<Intent?>? = null
    private var batteryLauncherCallback: BatteryOptimizationUtil.OnOptimizationActionCallback? = null

    fun initSetup(activity: ComponentActivity) {
        try {
            batteryLauncher = activity.registerForActivityResult(StartActivityForResult()) {
                handleVerificationResult(activity)
            }
            LogUtils.i("BatteryOptHelper", "✅ ActivityResult launcher initialized")
        } catch (e: Exception) {
            LogUtils.e("BatteryOptHelper", "Failed to initialize ActivityResult launcher: ${e.message}")
            e.printStackTrace()
        }
    }

    fun clearActivity() {
        batteryLauncher = null
        batteryLauncherCallback = null
        LogUtils.i("BatteryOptHelper", "Activity cleared")
    }

    // --- Check APIs ---

    /**
     * Battery optimization (Doze) is the MOST CRITICAL setting.
     * Must be explicitly disabled for background work.
     * Only skip if VERIFIED (actually disabled) or USER_CONFIRMED (manual device, user visited settings).
     * If NOT_SUPPORTED on pre-Android 6, that's OK (no Doze mode).
     */
    fun isBatteryOptimizationDisabled(context: Context): Boolean {
        val manager = BatteryOptimizationManager.getInstance(context)
        val status = manager.currentDevice?.checkBatteryOptimizationStatus(context)
                ?: return false
        return status == OptimizationVerificationStatus.VERIFIED ||
               status == OptimizationVerificationStatus.USER_CONFIRMED
    }

    /**
     * Auto-start is MEDIUM priority (app survives device reboot).
     * If NOT_SUPPORTED on this device, that's OK (auto-start is default behavior).
     * Only skip if VERIFIED, USER_CONFIRMED, or NOT_SUPPORTED.
     */
    fun isAutoStartEnabled(context: Context): Boolean {
        val manager = BatteryOptimizationManager.getInstance(context)
        val status = manager.currentDevice?.checkAutoStartStatus(context)
                ?: return true
        // If not supported, assume enabled by default (Android default behavior)
        return status == OptimizationVerificationStatus.VERIFIED ||
               status == OptimizationVerificationStatus.USER_CONFIRMED ||
               status == OptimizationVerificationStatus.NOT_SUPPORTED
    }

    /**
     * Background restriction is LOW priority (helps with foreground execution).
     * If NOT_SUPPORTED, that's OK (uses standard Android behavior).
     * Only skip if VERIFIED, USER_CONFIRMED, or NOT_SUPPORTED.
     */
    fun isManBatteryOptimizationDisabled(context: Context): Boolean {
        val manager = BatteryOptimizationManager.getInstance(context)
        val status = manager.currentDevice?.checkBackgroundRestrictionStatus(context)
                ?: return true
        // If not supported, assume unrestricted by default
        return status == OptimizationVerificationStatus.VERIFIED ||
               status == OptimizationVerificationStatus.USER_CONFIRMED ||
               status == OptimizationVerificationStatus.NOT_SUPPORTED
    }

    // --- Action APIs (Displaying Interactive Text Guides) ---

    fun showDisableBatteryOptimization(
        activity: ComponentActivity,
        callback: BatteryOptimizationUtil.OnOptimizationActionCallback
    ) {
        if (isBatteryOptimizationDisabled(activity)) {
            LogUtils.i("BatteryOptHelper", "✅ Battery optimization already disabled, skipping dialog")
            callback.onAccepted()
            return
        }

        // Step 1: Try programmatic approach first (Android 12+)
        if (tryRequestIgnoreBatteryOptimizationsProgrammatically(activity)) {
            LogUtils.i("BatteryOptHelper", "✅ Battery optimization exemption requested programmatically")
            // Set callback for when user returns from system dialog
            batteryLauncherCallback = callback
            // Attach lifecycle observer to verify when activity resumes
            attachLifecycleObserver(activity)
            return
        }

        // Step 2: Programmatic not available, fall back to manual dialog
        LogUtils.i("BatteryOptHelper", "Programmatic approach not available, falling back to manual dialog")
        showBatteryOptimizationDialog(activity, callback)
    }

    private fun tryRequestIgnoreBatteryOptimizationsProgrammatically(activity: ComponentActivity): Boolean {
        try {
            // On Android 12+, try requesting programmatically
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // API 31+
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                intent.data = Uri.parse("package:${activity.packageName}")
                if (intent.resolveActivity(activity.packageManager) != null) {
                    LogUtils.i("BatteryOptHelper", "🚀 Requesting battery optimization exemption (Android 12+)")
                    activity.startActivity(intent)
                    return true
                }
            }
        } catch (e: Exception) {
            LogUtils.e("BatteryOptHelper", "Failed to request battery optimization exemption: ${e.message}")
        }
        return false
    }

    private fun showBatteryOptimizationDialog(
        activity: ComponentActivity,
        callback: BatteryOptimizationUtil.OnOptimizationActionCallback
    ) {
        if (batteryLauncher == null) {
            LogUtils.i("BatteryOptHelper", "initSetup() was not called, using lifecycle observer fallback")
        }

        // Display the native text-based guide dialog before opening settings
        BatteryOptimizationUtil.showBatteryOptimizationDialog(
            activity,
            KillerManager.Actions.ACTION_POWERSAVING,
            "Battery Optimization Guide",
            object : BatteryOptimizationUtil.OnOptimizationActionCallback {
                override fun onAccepted() {
                    batteryLauncherCallback = callback
                    val intent = BatteryOptimizationManager.getInstance(activity).dozeIntent
                            ?: BatteryOptimizationUtil.getAppSettingsIntent(activity)

                    try {
                        if (batteryLauncher != null) {
                            LogUtils.i("BatteryOptHelper", "🚀 Launching doze intent via ActivityResult")
                            batteryLauncher?.launch(intent)
                        } else {
                            LogUtils.i("BatteryOptHelper", "🚀 Launching doze intent via startActivity")
                            activity.startActivity(intent)
                            attachLifecycleObserver(activity)
                        }
                    } catch (e: ActivityNotFoundException) {
                        LogUtils.e("BatteryOptHelper", "Activity not found for doze intent: ${e.message}")
                        callback.onCanceled()
                    }
                }
                override fun onCanceled() = callback.onCanceled()
            }
        )
    }

    fun showEnableAutoStart(
        activity: ComponentActivity,
        callback: BatteryOptimizationUtil.OnOptimizationActionCallback
    ) {
        val manager = BatteryOptimizationManager.getInstance(activity)
        if (manager.autoStartIntent == null) {
            LogUtils.i("BatteryOptHelper", "✅ Auto-start not supported on this device, skipping")
            callback.onAccepted()
            return
        }

        BatteryOptimizationUtil.showBatteryOptimizationDialog(
            activity,
            KillerManager.Actions.ACTION_AUTOSTART,
            "Auto Start Guide",
            object : BatteryOptimizationUtil.OnOptimizationActionCallback {
                override fun onAccepted() {
                    batteryLauncherCallback = callback
                    manager.autoStartIntent?.let {
                        try {
                            if (batteryLauncher != null) {
                                LogUtils.i("BatteryOptHelper", "🚀 Launching auto-start intent via ActivityResult")
                                batteryLauncher?.launch(it)
                            } else {
                                LogUtils.i("BatteryOptHelper", "🚀 Launching auto-start intent via startActivity")
                                activity.startActivity(it)
                                attachLifecycleObserver(activity)
                            }
                        } catch (e: ActivityNotFoundException) {
                            LogUtils.e("BatteryOptHelper", "Activity not found for auto-start intent: ${e.message}")
                            callback.onCanceled()
                        }
                    } ?: run {
                        LogUtils.i("BatteryOptHelper", "Auto-start intent became null unexpectedly")
                        callback.onAccepted()
                    }
                }
                override fun onCanceled() = callback.onCanceled()
            }
        )
    }

    fun showDisableManBatteryOptimization(
        activity: ComponentActivity,
        callback: BatteryOptimizationUtil.OnOptimizationActionCallback
    ) {
        val manager = BatteryOptimizationManager.getInstance(activity)
        if (manager.backgroundRestrictionIntent == null) {
            LogUtils.i("BatteryOptHelper", "✅ Background restriction not supported on this device, skipping")
            callback.onAccepted()
            return
        }

        BatteryOptimizationUtil.showBatteryOptimizationDialog(
            activity,
            KillerManager.Actions.ACTION_POWERSAVING,
            "Background Performance Guide",
            object : BatteryOptimizationUtil.OnOptimizationActionCallback {
                override fun onAccepted() {
                    batteryLauncherCallback = callback
                    manager.backgroundRestrictionIntent?.let {
                        try {
                            if (batteryLauncher != null) {
                                LogUtils.i("BatteryOptHelper", "🚀 Launching background restriction intent via ActivityResult")
                                batteryLauncher?.launch(it)
                            } else {
                                LogUtils.i("BatteryOptHelper", "🚀 Launching background restriction intent via startActivity")
                                activity.startActivity(it)
                                attachLifecycleObserver(activity)
                            }
                        } catch (e: ActivityNotFoundException) {
                            LogUtils.e("BatteryOptHelper", "Activity not found for background restriction intent: ${e.message}")
                            callback.onCanceled()
                        }
                    } ?: run {
                        LogUtils.i("BatteryOptHelper", "Background restriction intent became null unexpectedly")
                        callback.onAccepted()
                    }
                }
                override fun onCanceled() = callback.onCanceled()
            }
        )
    }

    fun disableAllOptimizations(
        activity: ComponentActivity,
        callback: BatteryOptimizationUtil.OnOptimizationActionCallback
    ) {
        LogUtils.i("BatteryOptHelper", "Starting comprehensive optimization disable flow (PRIORITY: Doze → Background → AutoStart)")

        // Step 3: Auto-start (lowest priority - app survives reboot)
        val nextStepAutoStart = object : BatteryOptimizationUtil.OnOptimizationActionCallback {
            override fun onAccepted() {
                if (!isAutoStartEnabled(activity)) {
                    LogUtils.i("BatteryOptHelper", "→ Step 3: Moving to auto-start configuration")
                    showEnableAutoStart(activity, callback)
                } else {
                    LogUtils.i("BatteryOptHelper", "✅ Step 3: Auto-start already configured")
                    callback.onAccepted()
                }
            }
            override fun onCanceled() {
                LogUtils.i("BatteryOptHelper", "→ Step 3: User skipped auto-start, still attempting final callback")
                callback.onCanceled()
            }
        }

        // Step 2: Background restriction (medium priority - prevents background kills)
        val nextStepBackground = object : BatteryOptimizationUtil.OnOptimizationActionCallback {
            override fun onAccepted() {
                if (!isManBatteryOptimizationDisabled(activity)) {
                    LogUtils.i("BatteryOptHelper", "→ Step 2: Moving to background restriction configuration")
                    showDisableManBatteryOptimization(activity, nextStepAutoStart)
                } else {
                    LogUtils.i("BatteryOptHelper", "✅ Step 2: Background restriction already configured")
                    nextStepAutoStart.onAccepted()
                }
            }
            override fun onCanceled() {
                LogUtils.i("BatteryOptHelper", "→ Step 2: User skipped background restriction, moving to auto-start")
                nextStepAutoStart.onAccepted()
            }
        }

        // Step 1: Battery optimization/Doze (HIGHEST PRIORITY - prevents app from sleeping)
        if (!isBatteryOptimizationDisabled(activity)) {
            LogUtils.i("BatteryOptHelper", "→ Step 1: Starting with battery optimization (CRITICAL)")
            showDisableBatteryOptimization(activity, nextStepBackground)
        } else {
            LogUtils.i("BatteryOptHelper", "✅ Step 1: Battery optimization already configured")
            nextStepBackground.onAccepted()
        }
    }

    /**
     * Verifies the result after returning from settings activity.
     *
     * This is called after ANY settings activity returns (doze, auto-start, or background restriction).
     *
     * Logic:
     * 1. Check which specific setting was just requested (via preference key check)
     * 2. Verify ONLY that setting (not all three)
     * 3. Persist the result if successful
     * 4. For manual-confirmation devices: accept after user visits settings once
     */
    private fun handleVerificationResult(activity: ComponentActivity) {
        val callback = batteryLauncherCallback
        batteryLauncherCallback = null

        val manager = BatteryOptimizationManager.getInstance(activity)
        val capabilities = manager.currentDevice?.getCapabilities(activity)

        LogUtils.i("BatteryOptHelper", "Device: ${manager.currentDevice?.javaClass?.simpleName}, " +
                "Manual confirmation: ${capabilities?.requiresManualConfirmation}")

        // Check DOZE MODE specifically
        val dozeStatus = manager.currentDevice?.checkBatteryOptimizationStatus(activity)
        if (dozeStatus == OptimizationVerificationStatus.VERIFIED) {
            LogUtils.i("BatteryOptHelper", "✅ Doze verification SUCCESS: Battery optimization is disabled")
            PrefUtils.saveToPrefs(activity, PrefKeys.IS_BATTERY_OPTIMIZATION_ACCEPTED, true)
            callback?.onAccepted()
            return
        } else if (dozeStatus == OptimizationVerificationStatus.FAILED &&
                   capabilities?.requiresManualConfirmation == true) {
            LogUtils.i("BatteryOptHelper", "✅ Manual-confirm device: User visited settings, accepting doze")
            PrefUtils.saveToPrefs(activity, PrefKeys.IS_BATTERY_OPTIMIZATION_ACCEPTED, true)
            callback?.onAccepted()
            return
        }

        // Check AUTO-START specifically
        val autoStartStatus = manager.currentDevice?.checkAutoStartStatus(activity)
        if (autoStartStatus == OptimizationVerificationStatus.VERIFIED) {
            LogUtils.i("BatteryOptHelper", "✅ Auto-start verification SUCCESS")
            PrefUtils.saveToPrefs(activity, PrefKeys.IS_MAN_AUTO_START_ACCEPTED, true)
            callback?.onAccepted()
            return
        } else if (autoStartStatus == OptimizationVerificationStatus.FAILED &&
                   capabilities?.requiresManualConfirmation == true) {
            LogUtils.i("BatteryOptHelper", "✅ Manual-confirm device: User visited settings, accepting auto-start")
            PrefUtils.saveToPrefs(activity, PrefKeys.IS_MAN_AUTO_START_ACCEPTED, true)
            callback?.onAccepted()
            return
        }

        // Check BACKGROUND RESTRICTION specifically
        val bgStatus = manager.currentDevice?.checkBackgroundRestrictionStatus(activity)
        if (bgStatus == OptimizationVerificationStatus.VERIFIED) {
            LogUtils.i("BatteryOptHelper", "✅ Background restriction verification SUCCESS")
            PrefUtils.saveToPrefs(activity, PrefKeys.IS_MAN_BATTERY_OPTIMIZATION_ACCEPTED, true)
            callback?.onAccepted()
            return
        } else if (bgStatus == OptimizationVerificationStatus.FAILED &&
                   capabilities?.requiresManualConfirmation == true) {
            LogUtils.i("BatteryOptHelper", "✅ Manual-confirm device: User visited settings, accepting background restriction")
            PrefUtils.saveToPrefs(activity, PrefKeys.IS_MAN_BATTERY_OPTIMIZATION_ACCEPTED, true)
            callback?.onAccepted()
            return
        }

        // Verification failed - user likely cancelled without changing anything
        LogUtils.i("BatteryOptHelper", "❌ Verification FAILED: Setting not applied, user likely cancelled")
        callback?.onCanceled()
    }

    private fun attachLifecycleObserver(activity: ComponentActivity) {
        activity.window.decorView.postDelayed({
            activity.lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onResume(owner: LifecycleOwner) {
                    LogUtils.i("BatteryOptHelper", "↩️ Activity resumed, verifying optimization status")
                    handleVerificationResult(activity)
                    owner.lifecycle.removeObserver(this)
                }
            })
        }, 500)
    }

    fun isAllOptimizationsDisabled(context: Context): Boolean {
        val batteryDisabled = isBatteryOptimizationDisabled(context)
        val autoStartEnabled = isAutoStartEnabled(context)
        val bgRestrictionDisabled = isManBatteryOptimizationDisabled(context)
        
        val allDisabled = batteryDisabled && autoStartEnabled && bgRestrictionDisabled
        LogUtils.i("BatteryOptHelper", "isAllOptimizationsDisabled: " +
                "battery=$batteryDisabled, autoStart=$autoStartEnabled, bgRestriction=$bgRestrictionDisabled → $allDisabled")
        
        return allDisabled
    }
}
