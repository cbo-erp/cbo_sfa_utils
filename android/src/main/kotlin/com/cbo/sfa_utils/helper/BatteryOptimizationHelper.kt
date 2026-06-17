package com.cbo.sfa_utils.helper

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import disable_battery_optimizations.managers.BatteryOptimizationManager
import disable_battery_optimizations.models.OptimizationVerificationStatus
import disable_battery_optimizations.utils.BatteryOptimizationUtil
import disable_battery_optimizations.utils.PrefKeys
import disable_battery_optimizations.utils.PrefUtils

/**
 * Modernized BatteryOptimizationHelper providing 6 independent APIs.
 */
object BatteryOptimizationHelper {

    private var batteryLauncher: ActivityResultLauncher<Intent?>? = null
    private var batteryLauncherCallback: BatteryOptimizationUtil.OnOptimizationActionCallback? = null

    fun initSetup(activity: ComponentActivity) {
        try {
            batteryLauncher = activity.registerForActivityResult(StartActivityForResult()) {
                handleVerificationResult(activity)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun clearActivity() {
        batteryLauncher = null
        batteryLauncherCallback = null
    }

    // --- 1. API: Check Battery Optimizations ---
    fun isBatteryOptimizationDisabled(context: Context): Boolean {
        return BatteryOptimizationManager.getInstance(context).isBatteryOptimizationDisabled
    }

    // --- 2. API: Check Auto Start ---
    fun isAutoStartEnabled(context: Context): Boolean {
        val status = BatteryOptimizationManager.getInstance(context).checkAutoStartStatus()
        return status == OptimizationVerificationStatus.VERIFIED || 
               status == OptimizationVerificationStatus.USER_CONFIRMED ||
               status == OptimizationVerificationStatus.NOT_SUPPORTED
    }

    // --- 3. API: Check Manufacturing Restrictions (Background Management) ---
    fun isManBatteryOptimizationDisabled(context: Context): Boolean {
        val status = BatteryOptimizationManager.getInstance(context).checkBackgroundRestrictionStatus()
        return status == OptimizationVerificationStatus.VERIFIED || 
               status == OptimizationVerificationStatus.USER_CONFIRMED ||
               status == OptimizationVerificationStatus.NOT_SUPPORTED
    }

    // --- 4. API: Disable Battery Optimization (Action) ---
    fun showDisableBatteryOptimization(
        activity: ComponentActivity, 
        callback: BatteryOptimizationUtil.OnOptimizationActionCallback
    ) {
        if (isBatteryOptimizationDisabled(activity)) {
            callback.onAccepted()
            return
        }

        val manager = BatteryOptimizationManager.getInstance(activity)
        val intent = manager.batteryOptimizationIntent ?: BatteryOptimizationUtil.getAppSettingsIntent(activity)
        
        batteryLauncherCallback = callback
        
        val launcher = batteryLauncher
        if (launcher != null) {
            launcher.launch(intent)
        } else {
            // Fallback for non-component activities or late attach
            activity.startActivity(intent)
            attachLifecycleObserver(activity)
        }
    }

    // --- 5. API: Toggle Auto Start (Action) ---
    fun showEnableAutoStart(
        activity: ComponentActivity,
        title: String,
        content: String,
        callback: BatteryOptimizationUtil.OnOptimizationActionCallback
    ) {
        val manager = BatteryOptimizationManager.getInstance(activity)
        if (manager.autoStartIntent == null) {
            callback.onAccepted()
            return
        }

        BatteryOptimizationUtil.showBatteryOptimizationDialog(
            activity,
            disable_battery_optimizations.managers.KillerManager.Actions.ACTION_AUTOSTART,
            title, content,
            object : BatteryOptimizationUtil.OnOptimizationActionCallback {
                override fun onAccepted() {
                    PrefUtils.saveToPrefs(activity, PrefKeys.IS_MAN_AUTO_START_ACCEPTED, true)
                    callback.onAccepted()
                }
                override fun onCanceled() = callback.onCanceled()
            })
    }

    // --- 6. API: Manufacturing Restrictions Disablement (Action) ---
    fun showDisableManBatteryOptimization(
        activity: ComponentActivity,
        title: String,
        content: String,
        callback: BatteryOptimizationUtil.OnOptimizationActionCallback
    ) {
        val manager = BatteryOptimizationManager.getInstance(activity)
        if (manager.backgroundRestrictionIntent == null) {
            callback.onAccepted()
            return
        }

        BatteryOptimizationUtil.showBatteryOptimizationDialog(
            activity,
            disable_battery_optimizations.managers.KillerManager.Actions.ACTION_POWERSAVING,
            title, content,
            object : BatteryOptimizationUtil.OnOptimizationActionCallback {
                override fun onAccepted() {
                    PrefUtils.saveToPrefs(activity, PrefKeys.IS_MAN_BATTERY_OPTIMIZATION_ACCEPTED, true)
                    callback.onAccepted()
                }
                override fun onCanceled() = callback.onCanceled()
            })
    }

    private fun attachLifecycleObserver(activity: ComponentActivity) {
        activity.window.decorView.postDelayed({
            activity.lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onResume(owner: LifecycleOwner) {
                    handleVerificationResult(activity)
                    owner.lifecycle.removeObserver(this)
                }
            })
        }, 500)
    }

    private fun handleVerificationResult(activity: ComponentActivity) {
        val callback = batteryLauncherCallback
        batteryLauncherCallback = null
        
        if (isBatteryOptimizationDisabled(activity)) {
            callback?.onAccepted()
        } else {
            val manager = BatteryOptimizationManager.getInstance(activity)
            val capabilities = manager.currentDevice?.getCapabilities(activity)
            if (capabilities?.requiresManualConfirmation == true) {
                PrefUtils.saveToPrefs(activity, PrefKeys.IS_BATTERY_OPTIMIZATION_ACCEPTED, true)
                callback?.onAccepted()
            } else {
                callback?.onCanceled()
            }
        }
    }

    fun isAllOptimizationsDisabled(context: Context): Boolean {
        return isBatteryOptimizationDisabled(context) && 
               isAutoStartEnabled(context) && 
               isManBatteryOptimizationDisabled(context)
    }

    fun disableAllOptimizations(
        activity: ComponentActivity,
        autoStartTitle: String, autoStartContent: String,
        manBatteryTitle: String, manBatteryContent: String,
        callback: BatteryOptimizationUtil.OnOptimizationActionCallback
    ) {
        val nextStepIgnore = object : BatteryOptimizationUtil.OnOptimizationActionCallback {
            override fun onAccepted() = showDisableBatteryOptimization(activity, callback)
            override fun onCanceled() = showDisableBatteryOptimization(activity, callback)
        }

        val nextStepMan = object : BatteryOptimizationUtil.OnOptimizationActionCallback {
            override fun onAccepted() {
                if (!isManBatteryOptimizationDisabled(activity)) {
                    showDisableManBatteryOptimization(activity, manBatteryTitle, manBatteryContent, nextStepIgnore)
                } else nextStepIgnore.onAccepted()
            }
            override fun onCanceled() = nextStepIgnore.onAccepted()
        }

        if (!isAutoStartEnabled(activity)) {
            showEnableAutoStart(activity, autoStartTitle, autoStartContent, nextStepMan)
        } else nextStepMan.onAccepted()
    }
}
