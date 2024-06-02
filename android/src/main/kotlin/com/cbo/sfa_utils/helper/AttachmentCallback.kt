package com.cbo.sfa.utils

import java.io.File

interface AttachmentCallback {
    fun onAttached(file: File)
}
interface LocationCallback {
    fun onResponse(allowed: Boolean)
}

interface BatteryCallback {
    fun onReceive(batteryPercentage: Int)
}
