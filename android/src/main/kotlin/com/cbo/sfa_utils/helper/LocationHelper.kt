package com.cbo.sfa_utils.helper

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task

object LocationHelper {

    fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || 
               locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    fun requestGps(
        activity: Activity,
        locationRequestCode: Int,
        callback: UtilsCallback<Boolean>?
    ) {
        val mLocationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(500)
            .setMaxUpdateDelayMillis(1000).build()

        val builder = LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest)
        val mSettingsClient = LocationServices.getSettingsClient(activity)

        mSettingsClient.checkLocationSettings(builder.build())
            .addOnSuccessListener {
                callback?.onReceive(true)
            }
            .addOnFailureListener { e ->
                if (e is ResolvableApiException) {
                    if (e.statusCode == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                        try {
                            e.startResolutionForResult(activity, locationRequestCode)
                        } catch (sie: IntentSender.SendIntentException) {
                            callback?.onReceive(false)
                        }
                    } else callback?.onReceive(false)
                } else callback?.onReceive(false)
            }
    }

    fun getCurrentLocation(context: Context, callback: UtilsCallback<Location?>) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        val cancellationTokenSource = CancellationTokenSource()

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && 
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            callback.onReceive(null)
            return
        }

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.token)
            .addOnCompleteListener { task: Task<Location> ->
                if (task.isSuccessful && task.result != null) {
                    callback.onReceive(task.result)
                } else {
                    callback.onReceive(null)
                }
            }
    }

    /**
     * Modern mock location detection.
     */
    fun isMockLocation(location: Location): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            location.isMock
        } else {
            location.isFromMockProvider
        }
    }
}
