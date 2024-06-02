package com.cbo.sfa.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task

object PermissionUtils {

    fun checkAccessFineLocationGranted(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun isLocationEnabled(context: Context): Boolean {
        val gfgLocationManager: LocationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        //return gfgLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || gfgLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        return gfgLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || gfgLocationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }


    fun getCurrentLocation(context: Context, callback: GenericCallback<Location?>) {

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        val cancellationTokenSource = CancellationTokenSource()


        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val currentLocationTask: Task<Location> = fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource.token
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

//        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
//            callback.onReceive(location)
//            // Got last known location. In some rare situations this can be null.
//        }

    }
}