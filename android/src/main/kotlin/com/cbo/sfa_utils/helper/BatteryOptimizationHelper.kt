package com.cbo.sfa_utils.helper

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import disable_battery_optimizations.managers.KillerManager
import disable_battery_optimizations.utils.BatteryOptimizationUtil
import disable_battery_optimizations.utils.PrefKeys
import disable_battery_optimizations.utils.PrefUtils

object BatteryOptimizationHelper {

    private var batteryLauncher: ActivityResultLauncher<Intent?>? = null
    private var batteryLauncherCallback: BatteryOptimizationUtil.OnOptimizationActionCallback? = null

    fun initSetup(activity: Activity) {
        if (activity is AppCompatActivity) {
            try {
                batteryLauncher = activity.registerForActivityResult(StartActivityForResult()) {
                    if (isBatteryOptimizationDisabled(activity)) {
                        batteryLauncherCallback?.onAccepted()
                    } else {
                        batteryLauncherCallback?.onCanceled()
                    }
                    batteryLauncherCallback = null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                batteryLauncherCallback?.onCanceled()
                batteryLauncherCallback = null
            }
        }
    }

    fun clearActivity() {
        batteryLauncher = null
        batteryLauncherCallback = null
    }

    fun showEnableAutoStart(
        context: Context,
        title: String,
        content: String,
        callback: BatteryOptimizationUtil.OnOptimizationActionCallback
    ) {
        BatteryOptimizationUtil.showBatteryOptimizationDialog(
            context,
            KillerManager.Actions.ACTION_AUTOSTART,
            title,
            content,
            object : BatteryOptimizationUtil.OnOptimizationActionCallback {
                override fun onAccepted() {
                    PrefUtils.saveToPrefs(context, PrefKeys.IS_MAN_AUTO_START_ACCEPTED, true)
                    callback.onAccepted()
                }

                override fun onCanceled() {
                    PrefUtils.saveToPrefs(context, PrefKeys.IS_MAN_AUTO_START_ACCEPTED, false)
                    callback.onCanceled()
                }
            }
        )
    }

    fun showDisableManBatteryOptimization(
        context: Context,
        title: String,
        content: String,
        callback: BatteryOptimizationUtil.OnOptimizationActionCallback
    ) {
        BatteryOptimizationUtil.showBatteryOptimizationDialog(
            context,
            KillerManager.Actions.ACTION_POWERSAVING,
            title,
            content,
            object : BatteryOptimizationUtil.OnOptimizationActionCallback {
                override fun onAccepted() {
                    PrefUtils.saveToPrefs(context, PrefKeys.IS_MAN_BATTERY_OPTIMIZATION_ACCEPTED, true)
                    callback.onAccepted()
                }

                override fun onCanceled() {
                    callback.onCanceled()
                }
            })
    }

    fun showDisableBatteryOptimization(
        context: Context,
        callback: BatteryOptimizationUtil.OnOptimizationActionCallback
    ) {
        if (isBatteryOptimizationDisabled(context)) {
            callback.onAccepted()
            return
        }

        val intent = BatteryOptimizationUtil.getIgnoreBatteryOptimizationsIntent(context)
        if (intent != null && batteryLauncher != null) {
            batteryLauncherCallback = callback
            batteryLauncher?.launch(intent)
        } else {
            try {
                val launchIntent = intent ?: BatteryOptimizationUtil.getAppSettingsIntent(context)
                if (context is Activity) {
                    context.startActivity(launchIntent)
                } else {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                }
                callback.onAccepted()
            } catch (e: Exception) {
                callback.onCanceled()
            }
        }
    }

    fun disableAllOptimizations(
        context: Context,
        autoStartTitle: String,
        autoStartContent: String,
        manBatteryTitle: String,
        manBatteryContent: String,
        callback: BatteryOptimizationUtil.OnOptimizationActionCallback
    ) {
        val nextStepIgnore = object : BatteryOptimizationUtil.OnOptimizationActionCallback {
            override fun onAccepted() {
                showDisableBatteryOptimization(context, callback)
            }
            override fun onCanceled() {
                showDisableBatteryOptimization(context, callback)
            }
        }

        val nextStepMan = object : BatteryOptimizationUtil.OnOptimizationActionCallback {
            override fun onAccepted() {
                if (!isManBatteryOptimizationDisabled(context)) {
                    showDisableManBatteryOptimization(context, manBatteryTitle, manBatteryContent, nextStepIgnore)
                } else {
                    nextStepIgnore.onAccepted()
                }
            }
            override fun onCanceled() {
                nextStepIgnore.onAccepted()
            }
        }

        if (!isAutoStartEnabled(context)) {
            showEnableAutoStart(context, autoStartTitle, autoStartContent, nextStepMan)
        } else {
            nextStepMan.onAccepted()
        }
    }

    fun isAutoStartEnabled(context: Context): Boolean {
        return if (PrefUtils.hasKey(context, PrefKeys.IS_MAN_AUTO_START_ACCEPTED)) {
            PrefUtils.getFromPrefs(context, PrefKeys.IS_MAN_AUTO_START_ACCEPTED, false) as Boolean
        } else {
            val available = KillerManager.isActionAvailable(context, KillerManager.Actions.ACTION_AUTOSTART)
            (!available).also {
                PrefUtils.saveToPrefs(context, PrefKeys.IS_MAN_AUTO_START_ACCEPTED, it)
            }
        }
    }

    fun isManBatteryOptimizationDisabled(context: Context): Boolean {
        return if (PrefUtils.hasKey(context, PrefKeys.IS_MAN_BATTERY_OPTIMIZATION_ACCEPTED)) {
            PrefUtils.getFromPrefs(context, PrefKeys.IS_MAN_BATTERY_OPTIMIZATION_ACCEPTED, false) as Boolean
        } else {
            val available = KillerManager.isActionAvailable(context, KillerManager.Actions.ACTION_POWERSAVING)
            (!available).also {
                PrefUtils.saveToPrefs(context, PrefKeys.IS_MAN_BATTERY_OPTIMIZATION_ACCEPTED, it)
            }
        }
    }

    fun isAllOptimizationsDisabled(context: Context): Boolean {
        return isAutoStartEnabled(context) &&
                isBatteryOptimizationDisabled(context) &&
                isManBatteryOptimizationDisabled(context)
    }

    fun isBatteryOptimizationDisabled(context: Context): Boolean {
        return BatteryOptimizationUtil.isIgnoringBatteryOptimizations(context)
    }
}
