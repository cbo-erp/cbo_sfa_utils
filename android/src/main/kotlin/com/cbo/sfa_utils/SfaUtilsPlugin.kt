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
import kotlin.collections.set


/** SfaUtilsPlugin */
class SfaUtilsPlugin : FlutterPlugin, MethodCallHandler, ActivityAware {

    private var applicationContext: Context? = null
    private var applicationActivity: Activity? = null
    private var methodChannel: MethodChannel? = null
    private var methodResults = mutableMapOf<String, Result>()
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
            SfaMethods.LOCATION_PERMISSION -> result.notImplemented()
            else -> {
                result.notImplemented()
            }
        }
    }


    private fun getLocation(arguments: MethodCall, result: Result) {
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


    private fun requestGPS(arguments: MethodCall, result: Result) {

        if (applicationActivity == null) {
            result.error("FAILURE", "Context is null", null)
            return
        }

        if (LocationHelper.isLocationEnabled(applicationContext!!)) {
            result.success(true)
            return
        }

        Log.e("TAG","+Calling REQUEST_GPS")
        methodResults[SfaMethods.REQUEST_GPS] = result
        // Only pass callback for handling permanent failure case (e.g. SETTINGS_CHANGE_UNAVAILABLE)
        LocationHelper.requestGps(applicationActivity!!, locationIntentCode, callback = { success ->
            if (!success) {
                result.error("PERMISSION_DENIED", "User denied the request", "")
            }
        })
    }


    private fun getBatteryPercentage(arguments: MethodCall, result: Result) {
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

    private fun getMobileIMEI(arguments: MethodCall, result: Result) {
        val uniqueId = HelperUtils.getDeviceUniqueId(applicationContext!!)
        result.success(uniqueId)

    }

    private fun setMobileIMEI(arguments: MethodCall, result: Result) {
        val uniqueToken = arguments.argument<String>("uniqueToken") as String
        val status = HelperUtils.setDeviceUniqueId(applicationContext!!, uniqueId = uniqueToken)
        result.success(status)
    }

    private fun getOsDetails(arguments: MethodCall, result: Result) {
        val osDetails = HelperUtils.getOsDetails(applicationContext!!)
        result.success(osDetails)
    }

    private fun timeIsAuto(arguments: MethodCall, result: Result) {
        val autoTimeVal = Settings.Global.getInt(
            this.applicationContext!!.contentResolver, Settings.Global.AUTO_TIME, 0
        )
        result.success(autoTimeVal == 1);
    }

    private fun timeZoneIsAuto(arguments: MethodCall, result: Result) {

        val autoTimeZone = Settings.Global.getInt(
            this.applicationContext!!.contentResolver, Settings.Global.AUTO_TIME_ZONE, 0
        )
        result.success(autoTimeZone == 1)
    }

    private fun openSettings(arguments: MethodCall, result: Result) {
        val intent = Intent(Settings.ACTION_DATE_SETTINGS)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        applicationContext!!.startActivity(intent)
        result.success(true)
    }

    private fun isDeveloperModeOn(arguments: MethodCall, result: Result) {

        val developerMode = Settings.Global.getInt(
            this.applicationContext!!.contentResolver,
            Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
            0
        )
        result.success(developerMode == 1)
    }

    private fun openFile(arguments: MethodCall, result: Result) {
        val context = applicationContext ?: run {
            result.error("FAILURE", "Application context is null", null)
            return
        }

        val filePath = arguments.argument<String>("filePath")!!
        val isOpened = HelperUtils.openFile(context, filePath)
        result.success(isOpened)
    }


    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        applicationContext = binding.activity
        applicationActivity = binding.activity
        binding.addActivityResultListener { requestCode, resultCode, data ->
            Log.w(
                "TAG",
                "+addActivityResultListener $requestCode, resultCode: $resultCode, data: $data"
            )

            if (requestCode == locationIntentCode) {
                try {
                    Log.w("TAG", "+addActivityResultListener:locationIntentCode $locationIntentCode")
                    methodResults[SfaMethods.REQUEST_GPS]?.let {
                        if (resultCode == Activity.RESULT_OK) {
                            it.success(true)
                        } else {
                            it.error("PERMISSION_DENIED", "User denied the GPS request", "")
                        }
                    }
                } catch (e: IllegalStateException) {
                    Log.e(
                        "TAG",
                        "+addActivityResultListener:Error: Reply already submitted - ${e.message}"
                    )

                } finally {
                    methodResults.remove(SfaMethods.REQUEST_GPS)
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