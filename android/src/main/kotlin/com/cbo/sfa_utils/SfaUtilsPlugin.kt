package com.cbo.sfa_utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.provider.Settings
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
    private var locationRequestInWIP = false

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

        when (call.method) {
            "getBatteryPercentage" -> getBatteryPercentage(result, call)
            "getMobileIMEI" -> getMobileIMEI(result, call)
            "setMobileIMEI" -> setMobileIMEI(result, call)
            "getOsDetail" -> getOsDetails(result, call)
            "getLocation" -> getLocation(result, call)
            "requestGPS" -> requestGPS(result, call)
            "timeIsAuto" -> timeIsAuto(result, call) // return true in ios
            "timeZoneIsAuto" -> timeZoneIsAuto(result, call)
            "openSetting" -> openSettings(result, call)
            "developerModeOn" -> isDeveloperModeOn(result, call)
            "openFile" -> openFile(result, call)
            "hasLocationPermission" -> result.notImplemented()
            else -> result.notImplemented()
        }
    }

    private fun getLocation(result: Result, arguments: MethodCall) {
        methodResult = result
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
                        result.error("LOCATION_NOT_FOUND", "Location not found", null)
                    }
                }
            },
        )
    }


    private fun requestGPS(result: Result, arguments: MethodCall) {

        if (applicationActivity == null) {
            result.error("FAILURE", "Context is null", null)
            return
        }
        if (locationRequestInWIP) {
            result.error("FAILURE", "Another request in Progress", null)
            return
        }

        if (LocationHelper.isLocationEnabled(applicationContext!!)) {
            result.success(true)
            return
        }

        locationRequestInWIP = true

        LocationHelper.requestGps(applicationActivity!!, locationIntentCode, callback = { data ->
            if (locationRequestInWIP) {
                locationRequestInWIP = false
                if (data) {
                    result.success(true)
                } else {
                    result.error("PERMISSION_DENIED", "User denied the request", "")
                }
            }


        })
    }


    private fun getBatteryPercentage(result: Result, arguments: MethodCall) {
        methodResult = result
        val percentage = HelperUtils.getBatteryLevel(applicationContext!!)
        result.success(percentage)

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

    private fun getMobileIMEI(result: Result, arguments: MethodCall) {
        methodResult = result
        val uniqueId = HelperUtils.getDeviceUniqueId(applicationContext!!)
        result.success(uniqueId)

    }

    private fun setMobileIMEI(result: Result, arguments: MethodCall) {
        methodResult = result
        val uniqueToken = arguments.argument<String>("uniqueToken") as String
        val status = HelperUtils.setDeviceUniqueId(applicationContext!!, uniqueId = uniqueToken)
        result.success(status)
    }

    private fun getOsDetails(result: Result, arguments: MethodCall) {
        methodResult = result
        val osDetails = HelperUtils.getOsDetails(applicationContext!!)
        result.success(osDetails)
    }

    private fun timeIsAuto(result: Result, arguments: MethodCall) {
        methodResult = result

        val autoTimeVal = Settings.Global.getInt(
            this.applicationContext!!.contentResolver, Settings.Global.AUTO_TIME, 0
        )

        result.success(autoTimeVal == 1);
    }

    private fun timeZoneIsAuto(result: Result, arguments: MethodCall) {
        methodResult = result

        val autoTimeZone = Settings.Global.getInt(
            this.applicationContext!!.contentResolver, Settings.Global.AUTO_TIME_ZONE, 0
        )
        result.success(autoTimeZone == 1)
    }

    private fun openSettings(result: Result, arguments: MethodCall) {
        methodResult = result
        val intent = Intent(Settings.ACTION_DATE_SETTINGS)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        applicationContext!!.startActivity(intent)
        result.success(true)
    }


    private fun isDeveloperModeOn(result: Result, arguments: MethodCall) {
        methodResult = result

        val developerMode = Settings.Global.getInt(
            this.applicationContext!!.contentResolver,
            Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
            0
        )
        result.success(developerMode == 1)
    }

    private fun openFile(result: Result, call: MethodCall) {
        val context = applicationContext ?: run {
            result.error("FAILURE", "Application context is null", null)
            return
        }

        val filePath = call.argument<String>("filePath")!!
        val isOpened = HelperUtils.openFile(context, filePath)
        result.success(isOpened)
    }


    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        applicationContext = binding.activity
        applicationActivity = binding.activity
        binding.addActivityResultListener { requestCode, resultCode, data ->
            if (requestCode == locationIntentCode && locationRequestInWIP) {
                locationRequestInWIP = false

                if (resultCode == Activity.RESULT_OK) {
                    methodResult?.success(true)
                } else {
                    methodResult?.error("PERMISSION_DENIED", "User denied the request", "")
                }
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