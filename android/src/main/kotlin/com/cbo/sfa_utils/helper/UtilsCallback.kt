package com.cbo.sfa_utils.helper

interface UtilsCallback<T> {
    fun onReceive(data: T?)
}