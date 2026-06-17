package com.cbo.sfa_utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import com.cbo.sfa_utils.helper.BatteryOptimizationHelper
import com.cbo.sfa_utils.helper.HelperUtils
import com.cbo.sfa_utils.helper.LocationHelper
import disable_battery_optimizations.utils.BatteryOptimizationUtil
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.StandardMethodCodec
import java.io.File

/**
 * SfaUtilsPlugin - Production Grade Native Bridge.
 * Handles Battery, Location, Recording, and Identity for Android 8-15+.
 */
class SfaUtilsPlugin : FlutterPlugin, MethodCallHandler, ActivityAware {

    private var applicationContext: Context? = null
    private var applicationActivity: Activity? = null
    private var componentActivity: ComponentActivity? = null
    private var methodChannel: MethodChannel? = null

    private val methodResults = mutableMapOf<String, Result>()
    private val intentCodeLocation = 2001

    // Recording fields
    private var recorder: MediaRecorder? = null
    private var audioFilePath: String? = null
    private enum class RecordingState { IDLE, RECORDING, PAUSED }
    private var recordingState = RecordingState.IDLE

    private val mainHandler = Handler(Looper.getMainLooper())
    private val tag = "SfaUtilsPlugin"

    // region Plugin Lifecycle

    override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        applicationContext = binding.applicationContext
        methodChannel = MethodChannel(
            binding.binaryMessenger,
            "com.cbo.sfa.utils.native",
            StandardMethodCodec.INSTANCE,
            binding.binaryMessenger.makeBackgroundTaskQueue()
        )
        methodChannel?.setMethodCallHandler(this)
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        methodChannel?.setMethodCallHandler(null)
        methodChannel = null
        recorder?.release()
        recorder = null
        applicationContext = null
    }

    // endregion

    // region Activity Lifecycle

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        applicationActivity = binding.activity
        applicationContext = binding.activity.applicationContext

        if (applicationActivity is ComponentActivity) {
            componentActivity = applicationActivity as ComponentActivity
            BatteryOptimizationHelper.initSetup(componentActivity!!)
        }

        binding.addActivityResultListener { requestCode, resultCode, _ ->
            if (requestCode == intentCodeLocation) {
                methodResults.remove(SfaMethods.REQUEST_GPS)?.let { result ->
                    if (resultCode == Activity.RESULT_OK) {
                        safeSuccess(result, true)
                    } else {
                        safeError(result, "PERMISSION_DENIED", "User denied the GPS request", "")
                    }
                }
                return@addActivityResultListener true
            }
            false
        }
    }

    override fun onDetachedFromActivity() {
        applicationActivity = null
        componentActivity = null
        BatteryOptimizationHelper.clearActivity()
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        onAttachedToActivity(binding)
    }

    override fun onDetachedFromActivityForConfigChanges() {
        onDetachedFromActivity()
    }

    // endregion

    // region Method Call Handler

    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            // Identity & OS
            SfaMethods.GET_IMEI -> getMobileIMEI(result)
            SfaMethods.SET_IMEI -> setMobileIMEI(call, result)
            SfaMethods.OS_DETAIL -> getOsDetails(result)

            // System State
            SfaMethods.BATTERY_PERCENTAGE -> getBatteryPercentage(result)
            SfaMethods.TIME_AUTO -> timeIsAuto(result)
            SfaMethods.TIMEZONE_AUTO -> timeZoneIsAuto(result)
            SfaMethods.DEVELOPER_MODE -> isDeveloperModeOn(result)

            // Settings & Files
            SfaMethods.OPEN_SETTINGS -> mainHandler.post { openSettings(result) }
            SfaMethods.OPEN_FILE -> mainHandler.post { openFile(call, result) }

            // Location
            SfaMethods.GET_LOCATION -> getLocation(result)
            SfaMethods.REQUEST_GPS -> mainHandler.post { requestGPS(result) }
            SfaMethods.LOCATION_PERMISSION -> hasLocationPermission(result)

            // Audio Recording
            SfaMethods.START_RECORDING -> startRecording(result)
            SfaMethods.STOP_RECORDING -> stopRecording(result)
            SfaMethods.PAUSE_RECORDING -> pauseRecording(result)
            SfaMethods.RESUME_RECORDING -> resumeRecording(result)

            // Battery Optimization
            SfaMethods.SHOW_ENABLE_AUTO_START -> mainHandler.post { showEnableAutoStart(call, result) }
            SfaMethods.SHOW_DISABLE_MAN_BATTERY_OPTIMIZATION -> mainHandler.post { showDisableManBatteryOptimization(call, result) }
            SfaMethods.SHOW_DISABLE_BATTERY_OPTIMIZATION -> mainHandler.post { showDisableBatteryOptimization(result) }
            SfaMethods.DISABLE_ALL_OPTIMIZATIONS -> mainHandler.post { disableAllOptimizations(call, result) }
            SfaMethods.IS_AUTO_START_ENABLED -> isAutoStartEnabled(result)
            SfaMethods.IS_BATTERY_OPTIMIZATION_DISABLED -> isBatteryOptimizationDisabled(result)
            SfaMethods.IS_MAN_BATTERY_OPTIMIZATION_DISABLED -> isManBatteryOptimizationDisabled(result)
            SfaMethods.IS_ALL_OPTIMIZATIONS_DISABLED -> isAllOptimizationsDisabled(result)

            else -> result.notImplemented()
        }
    }

    // endregion

    // region Identity & OS Methods

    private fun getMobileIMEI(result: Result) {
        val ctx = applicationContext ?: return safeError(result, "CONTEXT_ERROR", "Context null", null)
        safeSuccess(result, HelperUtils.getDeviceUniqueId(ctx))
    }

    private fun setMobileIMEI(call: MethodCall, result: Result) {
        val ctx = applicationContext ?: return safeError(result, "CONTEXT_ERROR", "Context null", null)
        val token = call.argument<String>("uniqueToken") ?: ""
        safeSuccess(result, HelperUtils.setDeviceUniqueId(ctx, token))
    }

    private fun getOsDetails(result: Result) {
        val ctx = applicationContext ?: return safeError(result, "CONTEXT_ERROR", "Context null", null)
        safeSuccess(result, HelperUtils.getOsDetails(ctx))
    }

    // endregion

    // region System State Methods

    private fun getBatteryPercentage(result: Result) {
        val ctx = applicationContext ?: return safeError(result, "CONTEXT_ERROR", "Context null", null)
        safeSuccess(result, HelperUtils.getBatteryLevel(ctx))
    }

    private fun timeIsAuto(result: Result) {
        val ctx = applicationContext ?: return safeError(result, "CONTEXT_ERROR", "Context null", null)
        val isAuto = Settings.Global.getInt(ctx.contentResolver, Settings.Global.AUTO_TIME, 0) == 1
        safeSuccess(result, isAuto)
    }

    private fun timeZoneIsAuto(result: Result) {
        val ctx = applicationContext ?: return safeError(result, "CONTEXT_ERROR", "Context null", null)
        val isAuto = Settings.Global.getInt(ctx.contentResolver, Settings.Global.AUTO_TIME_ZONE, 0) == 1
        safeSuccess(result, isAuto)
    }

    private fun isDeveloperModeOn(result: Result) {
        val ctx = applicationContext ?: return safeError(result, "CONTEXT_ERROR", "Context null", null)
        val isOn = Settings.Global.getInt(ctx.contentResolver, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) == 1
        safeSuccess(result, isOn)
    }

    // endregion

    // region Settings & File Methods

    private fun openSettings(result: Result) {
        val ctx = applicationContext ?: return safeError(result, "CONTEXT_ERROR", "Context null", null)
        try {
            val intent = Intent(Settings.ACTION_DATE_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ctx.startActivity(intent)
            safeSuccess(result, true)
        } catch (e: Exception) {
            safeError(result, "OPEN_SETTINGS_FAILED", e.localizedMessage, null)
        }
    }

    private fun openFile(call: MethodCall, result: Result) {
        val ctx = applicationContext ?: return safeError(result, "CONTEXT_ERROR", "Context is null", null)
        val path = call.argument<String>("filePath") ?: ""
        safeSuccess(result, HelperUtils.openFile(ctx, path))
    }

    // endregion

    // region Location Methods

    private fun getLocation(result: Result) {
        val ctx = applicationContext ?: return safeError(result, "CONTEXT_ERROR", "Context null", null)
        LocationHelper.getCurrentLocation(ctx) { data ->
            if (data != null) {
                safeSuccess(
                    result, mapOf(
                        "latitude" to data.latitude,
                        "longitude" to data.longitude,
                        "isMock" to LocationHelper.isMockLocation(data),
                        "altitude" to data.altitude,
                        "hasAltitude" to data.hasAltitude(),
                        "speed" to data.speed,
                        "hasSpeed" to data.hasSpeed(),
                        "accuracy" to data.accuracy,
                        "hasAccuracy" to data.hasAccuracy()
                    )
                )
            } else {
                safeError(result, "LOCATION_NOT_FOUND", "Could not fetch location", null)
            }
        }
    }

    private fun requestGPS(result: Result) {
        val activity = applicationActivity ?: return safeError(result, "FAILURE", "Activity null", null)
        val ctx = applicationContext ?: return safeError(result, "CONTEXT_ERROR", "Context null", null)

        if (LocationHelper.isLocationEnabled(ctx)) {
            safeSuccess(result, true)
            return
        }

        methodResults[SfaMethods.REQUEST_GPS] = result
        LocationHelper.requestGps(activity, intentCodeLocation) { data ->
            methodResults.remove(SfaMethods.REQUEST_GPS)?.let { pendingResult ->
                if (data) safeSuccess(pendingResult, true)
                else safeError(pendingResult, "GPS_DISABLED", "User refused to enable GPS", null)
            }
        }
    }

    private fun hasLocationPermission(result: Result) {
        val context = applicationContext ?: return safeError(result, "CONTEXT_ERROR", "Context null", null)
        val fineGranted = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        safeSuccess(result, fineGranted || coarseGranted)
    }

    // endregion

    // region Audio Recording Methods

    private fun startRecording(result: Result) {
        try {
            if (recordingState != RecordingState.IDLE) {
                return safeError(result, "START_ERROR", "Recording already in progress", null)
            }
            val context = applicationContext ?: return safeError(result, "CONTEXT_ERROR", "Context is null", null)

            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                return safeError(result, "PERMISSION_DENIED", "No mic permission", null)
            }

            val dir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
            val file = File(dir, "sfa_audio_${System.currentTimeMillis()}.m4a")
            audioFilePath = file.absolutePath

            recorder = (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(context) else MediaRecorder()).apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(audioFilePath)
                prepare()
                start()
            }
            recordingState = RecordingState.RECORDING
            safeSuccess(result, null)
        } catch (e: Exception) {
            safeError(result, "START_ERROR", e.localizedMessage, null)
        }
    }

    private fun stopRecording(result: Result) {
        try {
            recorder?.apply {
                stop()
                release()
            }
            recorder = null
            recordingState = RecordingState.IDLE
            safeSuccess(result, audioFilePath)
        } catch (e: Exception) {
            safeError(result, "STOP_ERROR", e.localizedMessage, null)
        }
    }

    private fun pauseRecording(result: Result) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && recordingState == RecordingState.RECORDING) {
            try {
                recorder?.pause()
                recordingState = RecordingState.PAUSED
                safeSuccess(result, null)
            } catch (e: Exception) {
                safeError(result, "PAUSE_FAILED", e.localizedMessage, null)
            }
        } else {
            safeError(result, "PAUSE_ERROR", "Not supported or not recording", null)
        }
    }

    private fun resumeRecording(result: Result) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && recordingState == RecordingState.PAUSED) {
            try {
                recorder?.resume()
                recordingState = RecordingState.RECORDING
                safeSuccess(result, null)
            } catch (e: Exception) {
                safeError(result, "RESUME_FAILED", e.localizedMessage, null)
            }
        } else {
            safeError(result, "RESUME_ERROR", "Not supported or not paused", null)
        }
    }

    // endregion

    // region Battery Optimization Methods

    private fun isBatteryOptimizationDisabled(result: Result) {
        val ctx = applicationContext ?: return safeError(result, "CONTEXT_ERROR", "Context null", null)
        safeSuccess(result, BatteryOptimizationHelper.isBatteryOptimizationDisabled(ctx))
    }

    private fun isAutoStartEnabled(result: Result) {
        val ctx = applicationContext ?: return safeError(result, "CONTEXT_ERROR", "Context null", null)
        safeSuccess(result, BatteryOptimizationHelper.isAutoStartEnabled(ctx))
    }

    private fun isManBatteryOptimizationDisabled(result: Result) {
        val ctx = applicationContext ?: return safeError(result, "CONTEXT_ERROR", "Context null", null)
        safeSuccess(result, BatteryOptimizationHelper.isManBatteryOptimizationDisabled(ctx))
    }

    private fun isAllOptimizationsDisabled(result: Result) {
        val ctx = applicationContext ?: return safeError(result, "CONTEXT_ERROR", "Context null", null)
        safeSuccess(result, BatteryOptimizationHelper.isAllOptimizationsDisabled(ctx))
    }

    private fun showDisableBatteryOptimization(result: Result) {
        val activity = componentActivity ?: return safeError(result, "FAILURE", "Activity is null", null)
        BatteryOptimizationHelper.showDisableBatteryOptimization(activity, object : BatteryOptimizationUtil.OnOptimizationActionCallback {
            override fun onAccepted() { safeSuccess(result, true) }
            override fun onCanceled() { safeSuccess(result, false) }
        })
    }

    private fun showEnableAutoStart(call: MethodCall, result: Result) {
        val title = call.argument<String>("title") ?: ""
        val content = call.argument<String>("content") ?: ""
        val activity = componentActivity ?: return safeError(result, "FAILURE", "Activity is null", null)
        BatteryOptimizationHelper.showEnableAutoStart(activity, title, content, object : BatteryOptimizationUtil.OnOptimizationActionCallback {
            override fun onAccepted() { safeSuccess(result, true) }
            override fun onCanceled() { safeSuccess(result, false) }
        })
    }

    private fun showDisableManBatteryOptimization(call: MethodCall, result: Result) {
        val title = call.argument<String>("title") ?: ""
        val content = call.argument<String>("content") ?: ""
        val activity = componentActivity ?: return safeError(result, "FAILURE", "Activity is null", null)
        BatteryOptimizationHelper.showDisableManBatteryOptimization(activity, title, content, object : BatteryOptimizationUtil.OnOptimizationActionCallback {
            override fun onAccepted() { safeSuccess(result, true) }
            override fun onCanceled() { safeSuccess(result, false) }
        })
    }

    private fun disableAllOptimizations(call: MethodCall, result: Result) {
        val activity = componentActivity ?: return safeError(result, "FAILURE", "Activity is null", null)
        BatteryOptimizationHelper.disableAllOptimizations(
            activity,
            call.argument("autoStartTitle") ?: "",
            call.argument("autoStartContent") ?: "",
            call.argument("manBatteryTitle") ?: "",
            call.argument("manBatteryContent") ?: "",
            object : BatteryOptimizationUtil.OnOptimizationActionCallback {
                override fun onAccepted() { safeSuccess(result, true) }
                override fun onCanceled() { safeSuccess(result, false) }
            }
        )
    }

    // endregion

    // region Thread-Safe Helpers

    private fun safeSuccess(result: Result, data: Any?) {
        mainHandler.post {
            try {
                result.success(data)
            } catch (e: IllegalStateException) {
                Log.e(tag, "safeSuccess failed: Reply already submitted - ${e.message}")
            }
        }
    }

    private fun safeError(result: Result, code: String, msg: String?, details: Any?) {
        mainHandler.post {
            try {
                result.error(code, msg, details)
            } catch (e: IllegalStateException) {
                Log.e(tag, "safeError failed: Reply already submitted - ${e.message}")
            }
        }
    }

    // endregion
}
