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


    fun launchTurnByTurn(context: Context?, latitude: Double, longitude: Double) {
//        &mode=l ==> motorized two-wheeler
        val gmmIntentUri = Uri.parse("google.navigation:q=$latitude,$longitude&mode=l")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        context?.startActivity(mapIntent)
    }

    fun openFile(context: Context, filePath: String): Boolean {
        if (filePath.isEmpty()) {
            return false
        }
        // Create a file object from the path
        val file = File(filePath)

        // Get the MIME type based on the file extension
        val mimeType = getMimeType(file)

        // Ensure the file exists and MIME type is valid
        if (file.exists() && mimeType != null) {
            // Get the URI for the file using FileProvider
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider", // You can use the application ID here
                file
            )

            // Create an intent to view the file
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType) // Set the MIME type for the file
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Grant read permission to the external app
            }

            // Start the intent to open the file
            context.startActivity(Intent.createChooser(intent, "Open file with"))
            return true
        } else {
            return false
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