package com.platzi.android.firestore.networ

interface RealTimeListener<T> {

    fun onDataChange(updateData: T)

    fun onError(exception: Exception)
}
