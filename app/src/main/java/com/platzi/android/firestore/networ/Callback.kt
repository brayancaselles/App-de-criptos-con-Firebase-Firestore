package com.platzi.android.firestore.networ

import java.lang.Exception

interface Callback<T> {

    fun onSuccess(result: T?)

    fun onFailed(exception: Exception)
}
