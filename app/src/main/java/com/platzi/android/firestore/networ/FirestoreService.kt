package com.platzi.android.firestore.networ

import com.google.firebase.firestore.FirebaseFirestore
import com.platzi.android.firestore.model.Crypto
import com.platzi.android.firestore.model.User

const val CRYPTO_COLLECTION_NAME = "cryptos"
const val USER_COLLECTION_NAME = "users"

class FirestoreService(private val firebaseFirestore: FirebaseFirestore) {

    fun setDocument(data: Any, collectionName: String, id: String, callback: Callback<Void>) {
        firebaseFirestore.collection(collectionName).document(id).set(data)
            .addOnSuccessListener { callback.onSuccess(null) }
            .addOnFailureListener { exception -> callback.onFailed(exception) }
    }

    fun updateUser(user: User, callback: Callback<User>?) {
        firebaseFirestore.collection(USER_COLLECTION_NAME).document(user.userName)
            .update("criptoList", user.cryptosList)
            .addOnSuccessListener { result ->
                if (callback != null) {
                    callback.onSuccess(user)
                }
            }
            .addOnFailureListener { exception -> callback!!.onFailed(exception) }
    }

    fun updateCrypto(crypto: Crypto) {
        firebaseFirestore.collection(CRYPTO_COLLECTION_NAME).document(crypto.getDocumentId())
            .update("available", crypto.available)
    }

    fun getCryptos(callback: Callback<List<Crypto>>?) {
        firebaseFirestore.collection(CRYPTO_COLLECTION_NAME).get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val cryptoList = result.toObjects(Crypto::class.java)
                    callback!!.onSuccess(cryptoList)
                    break
                }
            }.addOnFailureListener { exception ->
                callback!!.onFailed(exception)
            }
    }

    fun findUserById(id: String, callback: Callback<User>) {
        firebaseFirestore.collection(USER_COLLECTION_NAME).document(id).get()
            .addOnSuccessListener { result ->
                if (result.data != null) {
                    callback.onSuccess(result.toObject(User::class.java))
                } else {
                    callback.onSuccess(null)
                }
            }.addOnFailureListener { exception ->
                callback.onFailed(exception)
            }
    }

    fun listenForUpdates(cryptos: List<Crypto>, listener: RealTimeListener<Crypto>) {
        val cryptoReference = firebaseFirestore.collection(CRYPTO_COLLECTION_NAME)
        for (crypto in cryptos) {
            cryptoReference.document(crypto.getDocumentId())
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        listener.onError(error)
                    }

                    if (snapshot != null && snapshot.exists()) {
                        listener.onDataChange(snapshot.toObject(Crypto::class.java)!!)
                    }
                }
        }
    }

    fun listenForUpdates(user: User, listener: RealTimeListener<User>) {
        val userReference = firebaseFirestore.collection(USER_COLLECTION_NAME)
        userReference.document(user.userName).addSnapshotListener { snapshot, error ->
            if (error != null) {
                listener.onError(error)
            }

            if (snapshot != null && snapshot.exists()) {
                listener.onDataChange(snapshot.toObject(User::class.java)!!)
            }
        }
    }
}
