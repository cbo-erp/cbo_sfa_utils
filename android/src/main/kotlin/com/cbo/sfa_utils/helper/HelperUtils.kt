package com.cbo.sfa.utils

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import androidx.core.app.ActivityCompat

import java.lang.reflect.Field


class HelperUtils {
    fun getDeviceUniqueId(context: Context): String {
        val telephonyManager =
            ContextWrapper(context).getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager?

        val permissionStatus = ActivityCompat.checkSelfPermission(
            context, Manifest.permission.READ_PHONE_STATE
        )

        var deviceIdStr: String? = try {
            //DEVICE_ID = telephonyManager.getDeviceId();
            if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                return ""
            }

            telephonyManager?.deviceId ?: Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID
            )

        } catch (e: Exception) {
//            DEVICE_ID = FirebaseInstanceId.getInstance().getId();;
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        }

        if (deviceIdStr == null) {
            deviceIdStr = "0"
        }
        return ("$deviceIdStr'!'${Build.BRAND}${Build.MODEL}")
    }

    fun setDeviceUniqueId(mContext: Context, uniqueId: String): Boolean {
        return false
    }

    fun getOsDetails(mContext: Context): HashMap<String, Any> {

        val osDetails = HashMap<String, Any>()
        osDetails["device"] = "android"
        osDetails["sdkVersionNumber"] = Build.VERSION.SDK_INT.toString()
        osDetails["sdkVersionRelease"] = Build.VERSION.RELEASE

        val fields: Array<Field> = Build.VERSION_CODES::class.java.fields
        for (field in fields) {
//            val fieldName: String = field.name
            var fieldValue = -1
            try {
                fieldValue = field.getInt(Any())
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (fieldValue == Build.VERSION.SDK_INT) {
                osDetails["brand"] = Build.BRAND
            }
        }

        return osDetails
    }

//    fun getBatteryPercentage(mContext: Context, callBack: BatteryCallback) {
//
//        val br: BroadcastReceiver = object : BroadcastReceiver() {
//            override fun onReceive(context: Context?, intent: Intent) {
//                //context.unregisterReceiver(this);
//                val currentLevel: Int = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
//                val scale: Int = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
//                var level = -1
//                if (currentLevel >= 0 && scale > 0) {
//                    level = currentLevel * 100 / scale
//                }
//                callBack.onReceive(level)
//            }
//        }
//        val batteryLevelFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
//        (mContext as MainActivity).registerReceiver(br, batteryLevelFilter)
//    }


    fun getBatteryLevel(context: Context): Int {
        return getBatteryProperty(context, BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }

    private fun getBatteryProperty(context: Context, property: Int): Int {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        return batteryManager.getIntProperty(property)
    }


//    fun launchTurnByTurn(context: Context, result: MethodChannel.Result, arguments: MethodCall) {
//        val mLat: Double = arguments.argument<Double>("mLat").toString().toDouble()
//        val mLon: Double = arguments.argument<Double>("mLon").toString().toDouble()
////        &mode=l ==> motorized two-wheeler
//        val gmmIntentUri = Uri.parse("google.navigation:q=$mLat,$mLon&mode=l")
//        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
//        mapIntent.setPackage("com.google.android.apps.maps")
//        context.startActivity(mapIntent)
//    }

//    fun launchNativeCamera(
//        context: Context,
//        result: MethodChannel.Result,
//        arguments: MethodCall,
//        cameraRequestCode: Int
//    ) {
//        channelSuccess = result
//        mContext = context
//        val isFront: Boolean = arguments.argument<Boolean>("isFront") == true
////        val camType: String = arguments.argument<String>("type").toString()
////        val isGallery: Boolean = (camType == "gallery")
//        val intent = Intent(context as MainActivity, CameraActivity::class.java)
//        intent.putExtra("isFrontFace", isFront)
//        startActivityForResult(context, intent, cameraRequestCode, null)
//
//    }
//
//
//    fun requestGps(
//        context: Context, locationRequestCode: Int, callback: LocationCallback
//    ) {
//
//        val mLocationRequest = LocationRequest.create()
//        mLocationRequest.interval = 5000
//        mLocationRequest.fastestInterval = 2500
//        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
//        mLocationRequest.smallestDisplacement = 0f
//
//        val builder = LocationSettingsRequest.Builder()
//        builder.addLocationRequest(mLocationRequest)
//
//        val mSettingsClient = LocationServices.getSettingsClient(context)
//        val mLocationSettingsRequest = builder.build()
//
//        mSettingsClient.checkLocationSettings(mLocationSettingsRequest).addOnFailureListener(
//            context as MainActivity
//        ) { re ->
//            if (re is ResolvableApiException) {
//                when (re.statusCode) {
//                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
////                        rae.startResolutionForResult(context, locationRequestCode)
//                        startIntentSenderForResult(
//                            context,
//                            re.resolution.intentSender,
//                            locationRequestCode,
//                            null,
//                            0,
//                            0,
//                            0,
//                            null
//                        )
//
//                    } catch (sie: IntentSender.SendIntentException) {
//                        callback.onResponse(allowed = false)
//                    }
//
//                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> callback.onResponse(
//                        allowed = false
//                    )
//                }
//            } else {
//                callback.onResponse(allowed = false)
//            }
//        }
//    }
}