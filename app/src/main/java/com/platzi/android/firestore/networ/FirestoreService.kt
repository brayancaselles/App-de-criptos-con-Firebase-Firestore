package com.platzi.android.firestore.networ

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FirebaseFirestore
import com.platzi.android.firestore.model.Crypto
import com.platzi.android.firestore.model.User

const val  CRYPTO_COLLECTION_NAME = "cryptos"
const val  USER_COLLECTION_NAME = "users"

class FirestoreService(val firebaseFirestore: FirebaseFirestore) {

    fun setDocument(data: Any,collectionName: String,id: String,callback: Callback<Void>){
        firebaseFirestore.collection(collectionName).document(id).set(data)
            .addOnSuccessListener { callback.onSuccess(null) }
            .addOnFailureListener{ exception ->  callback.onFailed(exception)}
    }

    fun udpateUser(user: User, callback: Callback<User>?){
        firebaseFirestore.collection(USER_COLLECTION_NAME).document(user.userName)
            .update("criptoList",user.cryptosList)
            .addOnSuccessListener {
                result -> if (callback != null)
                    callback.onSuccess(user)
            }
            .addOnFailureListener { exception -> callback!!.onFailed(exception) }
    }

    fun updateCrypto(crypto: Crypto){
        firebaseFirestore.collection(CRYPTO_COLLECTION_NAME).document(crypto.getDocumentId())
            .update("available", crypto.available)
    }
}