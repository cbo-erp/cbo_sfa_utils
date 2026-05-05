package com.cbo.sfa_utils.helper

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import disable_battery_optimizations.managers.KillerManager
import disable_battery_optimizations.utils.BatteryOptimizationUtil
import disable_battery_optimizations.utils.LogUtils
import disable_battery_optimizations.utils.PrefKeys
import disable_battery_optimizations.utils.PrefUtils

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner


object BatteryOptimizationHelper {

    private var batteryLauncher: ActivityResultLauncher<Intent?>? = null
    private var batteryLauncherCallback: BatteryOptimizationUtil.OnOptimizationActionCallback? =
        null

    fun initSetup(activity: ComponentActivity) {
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
        } finally {
            LogUtils.i(
                "BatteryOptimizationHelper",
                "Launcher is not setup. extend FlutterFragmentActivity in application's main activity instead of FlutterActivity"
            )
        }

    }

    fun clearActivity() {
        batteryLauncher = null
        batteryLauncherCallback = null
    }

    fun showEnableAutoStart(
        context: ComponentActivity,
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
            })
    }

    fun showDisableManBatteryOptimization(
        activity: ComponentActivity,
        title: String,
        content: String,
        callback: BatteryOptimizationUtil.OnOptimizationActionCallback
    ) {
        BatteryOptimizationUtil.showBatteryOptimizationDialog(
            activity,
            KillerManager.Actions.ACTION_POWERSAVING,
            title,
            content,
            object : BatteryOptimizationUtil.OnOptimizationActionCallback {
                override fun onAccepted() {
                    PrefUtils.saveToPrefs(
                        activity, PrefKeys.IS_MAN_BATTERY_OPTIMIZATION_ACCEPTED, true
                    )
                    callback.onAccepted()
                }

                override fun onCanceled() {
                    callback.onCanceled()
                }
            })
    }

    fun showDisableBatteryOptimization(
        activity: ComponentActivity, callback: BatteryOptimizationUtil.OnOptimizationActionCallback
    ) {
        if (isBatteryOptimizationDisabled(activity)) {
            callback.onAccepted()
            return
        }
        val useStartActivity = true

        val intent = BatteryOptimizationUtil.getIgnoreBatteryOptimizationsIntent(activity)
        if (intent != null && batteryLauncher != null && !useStartActivity) {
            batteryLauncherCallback = callback
            batteryLauncher!!.launch(intent)
        } else {
            try {

                val launchIntent = intent ?: BatteryOptimizationUtil.getAppSettingsIntent(activity)
                activity.startActivity(launchIntent)

                activity.window.decorView.postDelayed({
                    // 1. Attach the "Listener"
                    val observer = BatteryResultListener {
                        if (isBatteryOptimizationDisabled(activity)) {
                            callback.onAccepted()
                        } else {
                            callback.onCanceled()
                        }
                    }
                    activity.lifecycle.addObserver(observer)
                }, 500)


            } catch (e: Exception) {
                callback.onCanceled()
                e.printStackTrace()
            }
        }
    }

    fun disableAllOptimizations(
        activity: ComponentActivity,
        autoStartTitle: String,
        autoStartContent: String,
        manBatteryTitle: String,
        manBatteryContent: String,
        callback: BatteryOptimizationUtil.OnOptimizationActionCallback
    ) {
        val nextStepIgnore = object : BatteryOptimizationUtil.OnOptimizationActionCallback {
            override fun onAccepted() {
                showDisableBatteryOptimization(activity, callback)
            }

            override fun onCanceled() {
                showDisableBatteryOptimization(activity, callback)
            }
        }

        val nextStepMan = object : BatteryOptimizationUtil.OnOptimizationActionCallback {
            override fun onAccepted() {
                if (!isManBatteryOptimizationDisabled(activity)) {
                    showDisableManBatteryOptimization(
                        activity, manBatteryTitle, manBatteryContent, nextStepIgnore
                    )
                } else {
                    nextStepIgnore.onAccepted()
                }
            }

            override fun onCanceled() {
                nextStepIgnore.onAccepted()
            }
        }

        if (!isAutoStartEnabled(activity)) {
            showEnableAutoStart(activity, autoStartTitle, autoStartContent, nextStepMan)
        } else {
            nextStepMan.onAccepted()
        }
    }

    fun isAutoStartEnabled(context: Context): Boolean {
        return if (PrefUtils.hasKey(context, PrefKeys.IS_MAN_AUTO_START_ACCEPTED)) {
            PrefUtils.getFromPrefs(context, PrefKeys.IS_MAN_AUTO_START_ACCEPTED, false) as Boolean
        } else {
            val available =
                KillerManager.isActionAvailable(context, KillerManager.Actions.ACTION_AUTOSTART)
            (!available).also {
                PrefUtils.saveToPrefs(context, PrefKeys.IS_MAN_AUTO_START_ACCEPTED, it)
            }
        }
    }

    fun isManBatteryOptimizationDisabled(context: Context): Boolean {
        return if (PrefUtils.hasKey(context, PrefKeys.IS_MAN_BATTERY_OPTIMIZATION_ACCEPTED)) {
            PrefUtils.getFromPrefs(
                context, PrefKeys.IS_MAN_BATTERY_OPTIMIZATION_ACCEPTED, false
            ) as Boolean
        } else {
            val available =
                KillerManager.isActionAvailable(context, KillerManager.Actions.ACTION_POWERSAVING)
            (!available).also {
                PrefUtils.saveToPrefs(context, PrefKeys.IS_MAN_BATTERY_OPTIMIZATION_ACCEPTED, it)
            }
        }
    }

    fun isAllOptimizationsDisabled(context: Context): Boolean {
        return isAutoStartEnabled(context) && isBatteryOptimizationDisabled(context) && isManBatteryOptimizationDisabled(
            context
        )
    }

    fun isBatteryOptimizationDisabled(context: Context): Boolean {
        return BatteryOptimizationUtil.isIgnoringBatteryOptimizations(context)
    }
}

class BatteryResultListener(private val onResult: () -> Unit) : DefaultLifecycleObserver {

    override fun onResume(owner: LifecycleOwner) {
        onResult()

        // Remove observer so it only runs once per launch
        owner.lifecycle.removeObserver(this)
    }

}

