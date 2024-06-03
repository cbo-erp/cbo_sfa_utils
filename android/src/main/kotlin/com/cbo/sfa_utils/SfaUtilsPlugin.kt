package com.cbo.sfa_utils

import android.content.Context
import android.location.Location
import android.os.Build
import com.cbo.sfa.utils.HelperUtils
import com.cbo.sfa_utils.helper.UtilsCallback
import com.cbo.sfa.utils.LocationHelper

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.StandardMethodCodec

/** SfaUtilsPlugin */
class SfaUtilsPlugin : FlutterPlugin, MethodCallHandler {

    private var applicationContext: Context? = null
    private var methodChannel: MethodChannel? = null

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
        applicationContext = null
        methodChannel!!.setMethodCallHandler(null)
        methodChannel = null
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            "getBatteryPercentage" -> getBatteryPercentage(result, call)
            "getMobileIMEI" -> getMobileIMEI(result, call)
            "setMobileIMEI" -> setMobileIMEI(result, call)
            "getOsDetail" -> getOsDetails(result, call)
            "getLocation" -> getLocation(result, call)
            "hasLocationPermission" -> result.notImplemented()
            else -> result.notImplemented()
        }
    }

    private fun getLocation(channelResult: Result, arguments: MethodCall) {

        if (applicationContext == null) {
            channelResult.success(failureResult("Context is null"))
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
                            channelResult.error("FAKE_GPS_DETECTED", "Fake GPS detected", null)
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

                        channelResult.success(successResult(locResult))

                    } else {
                        channelResult.error("LOCATION_NOT_FOUND", "Location not found", null)
                    }
                }
            },
        )
    }


    private fun getBatteryPercentage(result: Result, arguments: MethodCall) {

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
//        var mURL = arguments.argument<String>("url")
        val uniqueId = HelperUtils().getDeviceUniqueId(applicationContext!!)
        result.success(successResult(resultData = uniqueId))

    }

    private fun setMobileIMEI(result: Result, arguments: MethodCall) {
        val uniqueToken = arguments.argument<String>("uniqueToken") as String
        val status = HelperUtils().setDeviceUniqueId(applicationContext!!, uniqueId = uniqueToken)
        result.success(successResult(resultData = if (status) "success" else "Failure"))

    }

    private fun getOsDetails(result: Result, arguments: MethodCall) {
//        var mURL = arguments.argument<String>("url")
        val osDetails = HelperUtils().getOsDetails(applicationContext!!)
        result.success(successResult(osDetails))
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


}