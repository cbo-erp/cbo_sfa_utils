package com.cbo.sfa.utils

import android.location.Location

interface GenericCallback<T> {
    fun onReceive(data: T?)
}