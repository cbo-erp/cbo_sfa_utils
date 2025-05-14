package com.cbo.sfa_utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
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
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.StandardMethodCodec


/** SfaUtilsPlugin */
class SfaUtilsPlugin : FlutterPlugin, MethodCallHandler, ActivityAware {

    private var applicationContext: Context? = null
    private var applicationActivity: Activity? = null
    private var methodChannel: MethodChannel? = null
    private var methodResult: Result? = null
    private val locationIntentCode = 1999

    override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        this.applicationContext = binding.applicationContext
        methodChannel = MethodChannel(
            binding.binaryMessenger,
            "com.cbo.sfa.utils.native",
            StandardMethodCodec.INSTANCE,
            binding.binaryMessenger.makeBackgroundTaskQueue()
        )
        methodChannel!!.setMethodCallHandler(this)
        print("ChannelUtilsPlugin attached to engine............")
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        methodChannel!!.setMethodCallHandler(null)
        methodChannel = null
        applicationContext = null
        applicationActivity = null
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        this.methodResult = result
        when (call.method) {
            "getBatteryPercentage" -> getBatteryPercentage(call)
            "getMobileIMEI" -> getMobileIMEI(call)
            "setMobileIMEI" -> setMobileIMEI(call)
            "getOsDetail" -> getOsDetails(call)
            "getLocation" -> getLocation(call)
            "requestGPS" -> requestGPS(call)
            "timeIsAuto" -> timeIsAuto(call) // return true in ios
            "timeZoneIsAuto" -> timeZoneIsAuto(call)
            "openSetting" -> openSettings(call)
            "developerModeOn" -> isDeveloperModeOn(call)
            "openFile" -> openFile(call)
            "hasLocationPermission" -> result.notImplemented()
            else -> {
                result.notImplemented()
                methodResult = null
            }
        }
    }

    private fun getLocation(arguments: MethodCall) {
        if (applicationContext == null) {
            methodResult?.error("FAILURE", "Context is null", null)
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
                            methodResult?.error("FAKE_GPS_DETECTED", "Fake GPS detected", null)
                            return
                        }

                        methodResult?.success(
                            mapOf<String, Any>(
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
                        methodResult?.error("LOCATION_NOT_FOUND", "Location not found", null)
                    }
                }
            },
        )
    }


    private fun requestGPS(arguments: MethodCall) {
        if (applicationActivity == null) {
            methodResult?.error("FAILURE", "Context is null", null)
            return
        }

        if (LocationHelper.isLocationEnabled(applicationContext!!)) {
            methodResult?.success(true)
            return
        }
        // Only pass callback for handling permanent failure case (e.g. SETTINGS_CHANGE_UNAVAILABLE)
        LocationHelper.requestGps(applicationActivity!!, locationIntentCode, callback = { success ->
            if (!success) {
                methodResult?.let {
                    it.error("PERMISSION_DENIED", "User denied the request", "")
                    methodResult = null
                }
            }
        })
    }


    private fun getBatteryPercentage(arguments: MethodCall) {
        val percentage = HelperUtils.getBatteryLevel(applicationContext!!)
        methodResult?.success(percentage)

//        var mURL = arguments.argument<String>("url")
//        var _isSubmitted = false
//        CboUtils().getBatteryPercentage(
//            mContext = applicationContext,
//            callBack = object : BatteryCallback {
//                override fun onReceive(batteryPercentage: Int) {
//                    if (!_isSubmitted) {
//                        _isSubmitted = true
//                        result.success(successResult(resultData = "$batteryPercentage"))
//                    }
//                }
//            })

    }

    private fun getMobileIMEI(arguments: MethodCall) {
        val uniqueId = HelperUtils.getDeviceUniqueId(applicationContext!!)
        methodResult?.success(uniqueId)

    }

    private fun setMobileIMEI(arguments: MethodCall) {
        val uniqueToken = arguments.argument<String>("uniqueToken") as String
        val status = HelperUtils.setDeviceUniqueId(applicationContext!!, uniqueId = uniqueToken)
        methodResult?.success(status)
    }

    private fun getOsDetails(arguments: MethodCall) {
        val osDetails = HelperUtils.getOsDetails(applicationContext!!)
        methodResult?.success(osDetails)
    }

    private fun timeIsAuto(arguments: MethodCall) {
        val autoTimeVal = Settings.Global.getInt(
            this.applicationContext!!.contentResolver, Settings.Global.AUTO_TIME, 0
        )
        methodResult?.success(autoTimeVal == 1);
    }

    private fun timeZoneIsAuto(arguments: MethodCall) {

        val autoTimeZone = Settings.Global.getInt(
            this.applicationContext!!.contentResolver, Settings.Global.AUTO_TIME_ZONE, 0
        )
        methodResult?.success(autoTimeZone == 1)
    }

    private fun openSettings(arguments: MethodCall) {
        val intent = Intent(Settings.ACTION_DATE_SETTINGS)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        applicationContext!!.startActivity(intent)
        methodResult?.success(true)
    }

    private fun isDeveloperModeOn(arguments: MethodCall) {

        val developerMode = Settings.Global.getInt(
            this.applicationContext!!.contentResolver,
            Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
            0
        )
        methodResult?.success(developerMode == 1)
    }

    private fun openFile(call: MethodCall) {
        val context = applicationContext ?: run {
            methodResult?.error("FAILURE", "Application context is null", null)
            return
        }

        val filePath = call.argument<String>("filePath")!!
        val isOpened = HelperUtils.openFile(context, filePath)
        methodResult?.success(isOpened)
    }


    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        applicationContext = binding.activity
        applicationActivity = binding.activity
        binding.addActivityResultListener { requestCode, resultCode, data ->
            Log.w(
                "TAG",
                "addActivityResultListener $requestCode, resultCode: $resultCode, data: $data"
            )

            if (requestCode == locationIntentCode) {
                try {
                    Log.w("TAG", "addActivityResultListener:locationIntentCode $locationIntentCode")
                    methodResult?.let {
                        if (resultCode == Activity.RESULT_OK) {
                            it.success(true)
                        } else {
                            it.error("PERMISSION_DENIED", "User denied the GPS request", "")
                        }
                    }
                } catch (e: IllegalStateException) {
                    Log.e(
                        "TAG",
                        "addActivityResultListener:Error: Reply already submitted - ${e.message}"
                    )

                } finally {
                    methodResult = null
                }

                return@addActivityResultListener true
            }

            return@addActivityResultListener false
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