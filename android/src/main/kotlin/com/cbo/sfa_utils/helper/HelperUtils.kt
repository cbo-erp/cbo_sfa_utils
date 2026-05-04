package com.cbo.sfa_utils.helper

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import android.webkit.MimeTypeMap
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import java.io.File
import android.app.Activity


object HelperUtils {

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
        return true
    }

    fun getOsDetails(mContext: Context): Map<String, Any> {
        return mapOf<String, Any>(
            "platform" to "android",
            "manufacturer" to Build.MANUFACTURER,
            "os_version" to Build.VERSION.RELEASE,
            "device_model" to Build.MODEL,
            "sdk_version" to Build.VERSION.SDK_INT.toString(),
//            "brand" to Build.BRAND
        )
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


    fun openFile(context: Context, filePath: String): Boolean {
        if (filePath.isBlank()) return false

        val file = File(filePath)
        if (!file.exists()) return false

        val mimeType = getMimeType(file) ?: "*/*"

        return try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // ✅ ALWAYS
            }

            context.startActivity(Intent.createChooser(intent, "Open file with"))
            true

        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }


    // Function to get the MIME type based on file extension
    private fun getMimeType(file: File): String? {
        // Get the file extension from the file object
        val fileExtension = file.extension

        // Get the MIME type from the extension
        return if (fileExtension.isNotEmpty()) {
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.lowercase())
        } else {
            null
        }
    }

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
}