package com.platzi.android.firestore.networ

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FirebaseFirestore

const val  CRYPTO_COLLECTION_NAME = "cryptos"
const val  USER_COLLECTION_NAME = "cryptos"

class FirestoreService(val firebaseFirestore: FirebaseFirestore) {

    fun setDocument(data: Any,collectionName: String,id: String,callback: Callback<Void>){
        firebaseFirestore.collection(collectionName).document(id).set(data)
            .addOnSuccessListener { callback.onSuccess(null) }
            .addOnFailureListener{ exception ->  callback.onFailed(exception)}
    }
}