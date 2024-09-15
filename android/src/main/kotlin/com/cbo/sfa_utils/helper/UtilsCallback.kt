package com.cbo.sfa_utils.helper

fun interface UtilsCallback<T> {
    fun onReceive(data: T)
}