package com.cbo.sfa_utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.provider.Settings
import com.cbo.sfa.utils.HelperUtils
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
import android.util.Log


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
            // return true in ios
            "timeIsAuto" -> timeIsAuto(result, call)
            "developerModeOn" -> isDeveloperModeOn(result,call)
            "timeZoneIsAuto" -> timeZoneIsAuto(result, call)
            "openSetting" -> openSettings(result, call)
            "hasLocationPermission" -> result.notImplemented()
            else -> result.notImplemented()
        }
    }

    private fun getLocation(result: Result, arguments: MethodCall) {
        methodResult = result
        if (applicationContext == null) {
            result.success(failureResult("Context is null"))
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

                        val locResult = HashMap<String, Any>()
                        locResult["latitude"] = data.latitude
                        locResult["longitude"] = data.longitude
                        locResult["isMock"] = isMockLocation
                        locResult["altitude"] = data.altitude
                        locResult["hasAltitude"] = data.hasAltitude()
                        locResult["speed"] = data.speed
                        locResult["hasSpeed"] = data.hasSpeed()
                        locResult["accuracy"] = data.accuracy
                        locResult["hasAccuracy"] = data.hasAccuracy()

                        result.success(successResult(locResult))

                    } else {
                        result.error("LOCATION_NOT_FOUND", "Location not found", null)
                    }
                }
            },
        )
    }


    private fun requestGPS(result: Result, arguments: MethodCall) {

        Log.d("ChannelUtilsPlugin", "requestGPS: Function called")

        if (applicationActivity == null) {
            result.success(failureResult("Context is null"))
            return
        }
        if (locationRequestInWIP) {
            result.success(failureResult("Another request in Progress"))
            return
        }
        locationRequestInWIP = true
        methodResult = result

        LocationHelper.requestGps(
            applicationActivity!!, locationIntentCode, callback = { data ->
                locationRequestInWIP = false;
                if (data) {
                    methodResult?.success(successResult(resultData = ""))
                } else {
                    methodResult?.success(failureResult(message = "User denied the request"))
                }

            }
        )
    }


    private fun getBatteryPercentage(result: Result, arguments: MethodCall) {
        methodResult = result

        val percentage = HelperUtils().getBatteryLevel(applicationContext!!)
        result.success(successResult("$percentage"))

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
//        var mURL = arguments.argument<String>("url")
        val uniqueId = HelperUtils().getDeviceUniqueId(applicationContext!!)
        result.success(successResult(resultData = uniqueId))

    }

    private fun setMobileIMEI(result: Result, arguments: MethodCall) {
        methodResult = result
        val uniqueToken = arguments.argument<String>("uniqueToken") as String
        val status = HelperUtils().setDeviceUniqueId(applicationContext!!, uniqueId = uniqueToken)
        result.success(successResult(resultData = if (status) "success" else "Failure"))

    }

    private fun getOsDetails(result: Result, arguments: MethodCall) {
        methodResult = result
//        var mURL = arguments.argument<String>("url")
        val osDetails = HelperUtils().getOsDetails(applicationContext!!)
        result.success(successResult(osDetails))
    }

    private fun timeIsAuto(result: Result, arguments: MethodCall) {
        methodResult = result

        val autoTimeVal = Settings.Global.getInt(
            this.applicationContext!!.contentResolver,
            Settings.Global.AUTO_TIME,
            0
        )

        result.success(autoTimeVal == 1);
    }

    private fun timeZoneIsAuto(result: Result, arguments: MethodCall) {
        methodResult = result

        val autoTimeZone = Settings.Global.getInt(
            this.applicationContext!!.contentResolver,
            Settings.Global.AUTO_TIME_ZONE,
            0
        )
        result.success(autoTimeZone == 1)
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

    private fun openSettings(result: Result, arguments: MethodCall) {
        methodResult = result
        val intent = Intent(Settings.ACTION_DATE_SETTINGS)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        applicationContext!!.startActivity(intent)
        result.success(true)
    }


    private fun successResult(resultData: Any): HashMap<String, Any> {
        val result = HashMap<String, Any>()
        result["status"] = "1"
        result["msg"] = "success"
        result["data"] = resultData
        return result
    }

    private fun failureResult(message: String): HashMap<String, String> {
        val result = HashMap<String, String>()
        result["status"] = "0"
        result["data"] = ""
        result["msg"] = message
        return result
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        applicationContext = binding.activity
        applicationActivity = binding.activity
        binding.addActivityResultListener { requestCode, resultCode, data ->
            if (requestCode == locationIntentCode && locationRequestInWIP) {
                locationRequestInWIP = false

                if (resultCode == Activity.RESULT_OK) {
                    methodResult?.success(successResult("SUCCESS"))
                } else {
                    methodResult?.success(failureResult("Request Cancelled"))
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