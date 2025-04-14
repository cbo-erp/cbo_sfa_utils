package com.cbo.sfa_utils.helper

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startIntentSenderForResult
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task

object LocationHelper {

//    fun checkAccessFineLocationGranted(context: Context): Boolean {
//        return ContextCompat.checkSelfPermission(
//            context, Manifest.permission.ACCESS_FINE_LOCATION
//        ) == PackageManager.PERMISSION_GRANTED
//    }
//
    fun isLocationEnabled(context: Context): Boolean {
        val gfgLocationManager: LocationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        //return gfgLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || gfgLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        return gfgLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || gfgLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }


    fun requestGps(activity: Activity, locationRequestCode: Int, callback: UtilsCallback<Boolean>) {
        val mLocationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(500)
            .setMaxUpdateDelayMillis(1000).build();

        val builder = LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest)
        val mSettingsClient = LocationServices.getSettingsClient(activity)

        mSettingsClient.checkLocationSettings(builder.build())
            .addOnSuccessListener {
                callback.onReceive(true)
            }
            .addOnFailureListener { e ->
                if (e is ResolvableApiException) {
                    when (e.statusCode) {
                        LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                            try {
//                            e.startResolutionForResult(context, locationRequestCode)
                                startIntentSenderForResult(
                                    activity,
                                    e.resolution.intentSender,
                                    locationRequestCode,
                                    null,
                                    0,
                                    0,
                                    0,
                                    null
                                )
                            } catch (sie: IntentSender.SendIntentException) {
                                Log.e("LocationHelper", "Error in requestGps: $sie")
                                callback.onReceive(false)
                            }
                        }

                        LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                            callback.onReceive(false)
                        }
                    }
                } else {
                    Log.e("LocationHelper", "Error in requestGps: $e")
                    callback.onReceive(false)
                }
            }
    }


    fun getCurrentLocation(context: Context, callback: UtilsCallback<Location?>) {

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        val cancellationTokenSource = CancellationTokenSource()


        if (ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val currentLocationTask: Task<Location> = fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.token
        )

        var isSubmitted = false
        currentLocationTask.addOnCompleteListener { task: Task<Location> ->
            val result = if (task.isSuccessful && task.result != null) {
                val result: Location = task.result
                "Location (success): ${result.latitude}, ${result.longitude}"

            } else {
                val exception = task.exception
                "Location (failure): $exception"
            }
            if (!isSubmitted) {
                isSubmitted = true;
                callback.onReceive(task.result)
            }

            Log.d("TAG", "getCurrentLocation() result: $result")

        }
    }
}