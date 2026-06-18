package com.cbo.sfa_utils.helper

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import disable_battery_optimizations.managers.BatteryOptimizationManager
import disable_battery_optimizations.managers.KillerManager
import disable_battery_optimizations.models.OptimizationVerificationStatus
import disable_battery_optimizations.utils.BatteryOptimizationUtil
import disable_battery_optimizations.utils.PrefKeys
import disable_battery_optimizations.utils.PrefUtils

/**
 * Modernized BatteryOptimizationHelper.
 * Displays interactive text-based guides in the native flow to replace outdated images.
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

    // --- Check APIs ---

    fun isBatteryOptimizationDisabled(context: Context): Boolean {
        return BatteryOptimizationManager.getInstance(context).isBatteryOptimizationDisabled
    }

    fun isAutoStartEnabled(context: Context): Boolean {
        val status = BatteryOptimizationManager.getInstance(context).checkAutoStartStatus()
        return status == OptimizationVerificationStatus.VERIFIED || 
               status == OptimizationVerificationStatus.USER_CONFIRMED ||
               status == OptimizationVerificationStatus.NOT_SUPPORTED
    }

    fun isManBatteryOptimizationDisabled(context: Context): Boolean {
        val status = BatteryOptimizationManager.getInstance(context).checkBackgroundRestrictionStatus()
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
            callback.onAccepted()
            return
        }

        // Display the native text-based guide dialog before opening settings
        BatteryOptimizationUtil.showBatteryOptimizationDialog(
            activity,
            KillerManager.Actions.ACTION_POWERSAVING,
            "Battery Optimization Guide",
            object : BatteryOptimizationUtil.OnOptimizationActionCallback {
                override fun onAccepted() {
                    batteryLauncherCallback = callback
                    val intent = BatteryOptimizationManager.getInstance(activity).batteryOptimizationIntent
                            ?: BatteryOptimizationUtil.getAppSettingsIntent(activity)
                    
                    if (batteryLauncher != null) {
                        batteryLauncher?.launch(intent)
                    } else {
                        activity.startActivity(intent)
                        attachLifecycleObserver(activity)
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
                        if (batteryLauncher != null) batteryLauncher?.launch(it)
                        else {
                            activity.startActivity(it)
                            attachLifecycleObserver(activity)
                        }
                    } ?: callback.onAccepted()
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
                        if (batteryLauncher != null) batteryLauncher?.launch(it)
                        else {
                            activity.startActivity(it)
                            attachLifecycleObserver(activity)
                        }
                    } ?: callback.onAccepted()
                }
                override fun onCanceled() = callback.onCanceled()
            }
        )
    }

    fun disableAllOptimizations(
        activity: ComponentActivity,
        callback: BatteryOptimizationUtil.OnOptimizationActionCallback
    ) {
        val nextStepIgnore = object : BatteryOptimizationUtil.OnOptimizationActionCallback {
            override fun onAccepted() = showDisableBatteryOptimization(activity, callback)
            override fun onCanceled() = showDisableBatteryOptimization(activity, callback)
        }
        val nextStepMan = object : BatteryOptimizationUtil.OnOptimizationActionCallback {
            override fun onAccepted() {
                if (!isManBatteryOptimizationDisabled(activity)) {
                    showDisableManBatteryOptimization(activity, nextStepIgnore)
                } else nextStepIgnore.onAccepted()
            }
            override fun onCanceled() = nextStepIgnore.onAccepted()
        }
        if (!isAutoStartEnabled(activity)) showEnableAutoStart(activity, nextStepMan)
        else nextStepMan.onAccepted()
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

    fun isAllOptimizationsDisabled(context: Context): Boolean {
        return isBatteryOptimizationDisabled(context) && 
               isAutoStartEnabled(context) && 
               isManBatteryOptimizationDisabled(context)
    }
}
