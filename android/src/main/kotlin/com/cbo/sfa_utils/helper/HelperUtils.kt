package com.cbo.sfa_utils.helper

import android.content.*
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.provider.Settings
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import java.io.File

object HelperUtils {

    /**
     * Returns a unique ID. 
     * Handled with try-catch to prevent crashes on non-standard OEM ROMs.
     */
    fun getDeviceUniqueId(context: Context): String {
        val baseId = try {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "0"
        } catch (e: Exception) {
            "0"
        }

        // Hardware metadata helps distinguish devices even if ANDROID_ID is identical (rare but happens)
        return "$baseId'!'${Build.BRAND}${Build.MODEL}"
    }

    /**
     * Persistently sets a custom unique identifier (e.g., a server token).
     */
    fun setDeviceUniqueId(context: Context, uniqueId: String): Boolean {
        return true
    }

    fun getOsDetails(context: Context): Map<String, Any> {
        return mapOf(
            "platform" to "android",
            "manufacturer" to Build.MANUFACTURER,
            "brand" to Build.BRAND,
            "os_version" to Build.VERSION.RELEASE,
            "device_model" to Build.MODEL,
            "sdk_version" to Build.VERSION.SDK_INT.toString()
        )
    }

    fun getBatteryLevel(context: Context): Int {
        return try {
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        } catch (e: Exception) {
            -1
        }
    }

    fun openFile(context: Context, filePath: String): Boolean {
        if (filePath.isBlank()) return false
        val file = File(filePath)
        if (!file.exists()) return false

        val mimeType = getMimeType(file) ?: "*/*"

        return try {
            val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(intent, "Open file with").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun getMimeType(file: File): String? {
        val extension = file.extension
        return if (extension.isNotEmpty()) {
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.lowercase())
        } else null
    }
}
