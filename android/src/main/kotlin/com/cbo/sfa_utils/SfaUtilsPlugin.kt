package com.cbo.sfa_utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.Location
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Log
import com.cbo.sfa_utils.helper.HelperUtils
import com.cbo.sfa_utils.helper.LocationHelper
import com.cbo.sfa_utils.helper.UtilsCallback
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.StandardMethodCodec
import java.io.File

/** SfaUtilsPlugin */
class SfaUtilsPlugin : FlutterPlugin, MethodChannel.MethodCallHandler, ActivityAware {

    private var applicationContext: Context? = null
    private var applicationActivity: Activity? = null
    private var methodChannel: MethodChannel? = null
    private var recorder: MediaRecorder? = null
    private var audioFilePath: String? = null
    private val locationIntentCode = 1999
    private var methodResults = mutableMapOf<String, MethodChannel.Result>()

    // 🎙️ Custom recording state tracker
    private enum class RecordingState { IDLE, RECORDING, PAUSED }
    private var recordingState = RecordingState.IDLE

    override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        this.applicationContext = binding.applicationContext
        methodChannel = MethodChannel(
            binding.binaryMessenger,
            "com.cbo.sfa.utils.native",
            StandardMethodCodec.INSTANCE,
            binding.binaryMessenger.makeBackgroundTaskQueue()
        )
        methodChannel!!.setMethodCallHandler(this)
        Log.i("SfaUtilsPlugin", "Attached to engine.")
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        methodChannel!!.setMethodCallHandler(null)
        methodChannel = null
        applicationContext = null
        applicationActivity = null
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            SfaMethods.BATTERY_PERCENTAGE -> getBatteryPercentage(call, result)
            SfaMethods.GET_IMEI -> getMobileIMEI(call, result)
            SfaMethods.SET_IMEI -> setMobileIMEI(call, result)
            SfaMethods.OS_DETAIL -> getOsDetails(call, result)
            SfaMethods.GET_LOCATION -> getLocation(call, result)
            SfaMethods.REQUEST_GPS -> requestGPS(call, result)
            SfaMethods.TIME_AUTO -> timeIsAuto(call, result)
            SfaMethods.TIMEZONE_AUTO -> timeZoneIsAuto(call, result)
            SfaMethods.OPEN_SETTINGS -> openSettings(call, result)
            SfaMethods.DEVELOPER_MODE -> isDeveloperModeOn(call, result)
            SfaMethods.OPEN_FILE -> openFile(call, result)

            // 🎙️ Audio recording controls
            SfaMethods.START_RECORDING -> startRecording(result)
            SfaMethods.PAUSE_RECORDING -> pauseRecording(result)
            SfaMethods.RESUME_RECORDING -> resumeRecording(result)
            SfaMethods.STOP_RECORDING -> stopRecording(result)

            else -> result.notImplemented()
        }
    }

    // ==================== 🎙️ AUDIO RECORDING HANDLERS ====================

    private fun startRecording(channelResult: MethodChannel.Result) {
        try {
            if (recordingState == RecordingState.RECORDING) {
                channelResult.error("START_ERROR", "Recording already in progress.", null)
                return
            }

            val context = applicationContext ?: run {
                channelResult.error("CONTEXT_ERROR", "Context is null", null)
                return
            }

            val dir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
            if (dir == null) {
                channelResult.error("DIR_ERROR", "Cannot access external music directory", null)
                return
            }

            val file = File(dir, "sfa_recorded_audio_${System.currentTimeMillis()}.m4a")
            audioFilePath = file.absolutePath

            recorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(audioFilePath)
                prepare()
                start()
            }

            recordingState = RecordingState.RECORDING
            channelResult.success(null)
        } catch (e: Exception) {
            e.printStackTrace()
            channelResult.error("START_ERROR", e.localizedMessage, null)
        }
    }

    private fun pauseRecording(channelResult: MethodChannel.Result) {
        try {
            if (recordingState != RecordingState.RECORDING) {
                channelResult.error("PAUSE_ERROR", "Cannot pause — recording not in progress.", null)
                return
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                recorder?.pause()
                recordingState = RecordingState.PAUSED
                channelResult.success(null)
            } else {
                channelResult.error("PAUSE_ERROR", "Pause not supported on this Android version.", null)
            }
        } catch (e: Exception) {
            channelResult.error("PAUSE_ERROR", e.localizedMessage, null)
        }
    }

    private fun resumeRecording(channelResult: MethodChannel.Result) {
        try {
            if (recordingState != RecordingState.PAUSED) {
                channelResult.error("RESUME_ERROR", "Cannot resume — recorder is not paused.", null)
                return
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                recorder?.resume()
                recordingState = RecordingState.RECORDING
                channelResult.success(null)
            } else {
                channelResult.error("RESUME_ERROR", "Resume not supported on this Android version.", null)
            }
        } catch (e: Exception) {
            channelResult.error("RESUME_ERROR", e.localizedMessage, null)
        }
    }

    private fun stopRecording(channelResult: MethodChannel.Result) {
        try {
            if (recordingState == RecordingState.IDLE) {
                channelResult.error("STOP_ERROR", "Recording has not started yet.", null)
                return
            }

            recorder?.apply {
                stop()
                release()
            }

            recorder = null
            recordingState = RecordingState.IDLE
            channelResult.success(audioFilePath)
        } catch (e: Exception) {
            channelResult.error("STOP_ERROR", "Failed to stop recording: ${e.localizedMessage}", null)
        }
    }

    // ==================== 📍 LOCATION + SYSTEM METHODS ====================

    private fun getLocation(arguments: MethodCall, result: MethodChannel.Result) {
        if (applicationContext == null) {
            result.error("FAILURE", "Context is null", null)
            return
        }

        LocationHelper.getCurrentLocation(
            context = applicationContext!!,
            callback = object : UtilsCallback<Location?> {
                override fun onReceive(data: Location?) {
                    if (data != null) {
                        val isMockLocation = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            data.isMock
                        } else {
                            data.isFromMockProvider
                        }

                        if (isMockLocation) {
                            result.error("FAKE_GPS_DETECTED", "Fake GPS detected", null)
                            return
                        }

                        result.success(
                            mapOf(
                                "latitude" to data.latitude,
                                "longitude" to data.longitude,
                                "isMock" to false,
                                "altitude" to data.altitude,
                                "hasAltitude" to data.hasAltitude(),
                                "speed" to data.speed,
                                "hasSpeed" to data.hasSpeed(),
                                "accuracy" to data.accuracy,
                                "hasAccuracy" to data.hasAccuracy(),
                            )
                        )
                    } else {
                        result.error("LOCATION_NOT_FOUND", "Location not found", null)
                    }
                }
            }
        )
    }

    private fun requestGPS(arguments: MethodCall, result: MethodChannel.Result) {
        if (applicationActivity == null) {
            result.error("FAILURE", "Context is null", null)
            return
        }

        if (LocationHelper.isLocationEnabled(applicationContext!!)) {
            result.success(true)
            return
        }

        methodResults[SfaMethods.REQUEST_GPS] = result
        LocationHelper.requestGps(applicationActivity!!, locationIntentCode) { success ->
            if (!success) {
                result.error("PERMISSION_DENIED", "User denied the request", "")
            }
        }
    }

    // ==================== ⚙️ SYSTEM HELPERS ====================

    private fun getBatteryPercentage(arguments: MethodCall, result: MethodChannel.Result) {
        val percentage = HelperUtils.getBatteryLevel(applicationContext!!)
        result.success(percentage)
    }

    private fun getMobileIMEI(arguments: MethodCall, result: MethodChannel.Result) {
        val uniqueId = HelperUtils.getDeviceUniqueId(applicationContext!!)
        result.success(uniqueId)
    }

    private fun setMobileIMEI(arguments: MethodCall, result: MethodChannel.Result) {
        val uniqueToken = arguments.argument<String>("uniqueToken") as String
        val status = HelperUtils.setDeviceUniqueId(applicationContext!!, uniqueId = uniqueToken)
        result.success(status)
    }

    private fun getOsDetails(arguments: MethodCall, result: MethodChannel.Result) {
        val osDetails = HelperUtils.getOsDetails(applicationContext!!)
        result.success(osDetails)
    }

    private fun timeIsAuto(arguments: MethodCall, result: MethodChannel.Result) {
        val autoTimeVal = Settings.Global.getInt(
            applicationContext!!.contentResolver, Settings.Global.AUTO_TIME, 0
        )
        result.success(autoTimeVal == 1)
    }

    private fun timeZoneIsAuto(arguments: MethodCall, result: MethodChannel.Result) {
        val autoTimeZone = Settings.Global.getInt(
            applicationContext!!.contentResolver, Settings.Global.AUTO_TIME_ZONE, 0
        )
        result.success(autoTimeZone == 1)
    }

    private fun openSettings(arguments: MethodCall, result: MethodChannel.Result) {
        val intent = Intent(Settings.ACTION_DATE_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        applicationContext!!.startActivity(intent)
        result.success(true)
    }

    private fun isDeveloperModeOn(arguments: MethodCall, result: MethodChannel.Result) {
        val developerMode = Settings.Global.getInt(
            applicationContext!!.contentResolver,
            Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
            0
        )
        result.success(developerMode == 1)
    }

    private fun openFile(arguments: MethodCall, result: MethodChannel.Result) {
        val context = applicationContext ?: run {
            result.error("FAILURE", "Application context is null", null)
            return
        }

        val filePath = arguments.argument<String>("filePath")!!
        val isOpened = HelperUtils.openFile(context, filePath)
        result.success(isOpened)
    }

    // ==================== 🎯 ACTIVITY HANDLERS ====================

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        applicationContext = binding.activity
        applicationActivity = binding.activity
        binding.addActivityResultListener { requestCode, resultCode, data ->
            if (requestCode == locationIntentCode) {
                methodResults[SfaMethods.REQUEST_GPS]?.let {
                    if (resultCode == Activity.RESULT_OK) {
                        it.success(true)
                    } else {
                        it.error("PERMISSION_DENIED", "User denied the GPS request", "")
                    }
                }
                methodResults.remove(SfaMethods.REQUEST_GPS)
                return@addActivityResultListener true
            }
            false
        }
    }

    override fun onDetachedFromActivityForConfigChanges() {
        applicationContext = null
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        applicationContext = binding.activity
    }

    override fun onDetachedFromActivity() {
        applicationContext = null
    }
}
