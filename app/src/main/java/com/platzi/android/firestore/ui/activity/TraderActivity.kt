package com.platzi.android.firestore.ui.activity

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.platzi.android.firestore.R
import com.platzi.android.firestore.adapter.CryptosAdapter
import com.platzi.android.firestore.adapter.CryptosAdapterListener
import com.platzi.android.firestore.model.Crypto
import com.platzi.android.firestore.model.User
import com.platzi.android.firestore.networ.Callback
import com.platzi.android.firestore.networ.FirestoreService
import com.platzi.android.firestore.networ.RealTimeListener
import com.squareup.picasso.Picasso
import kotlin.Exception
import kotlinx.android.synthetic.main.activity_trader.*

/**
 * @author Brayan Bermudez
 * github brayancaselles
 * https://github.com/brayancaselles/App-de-criptos-con-Firebase-Firestore
 */
class TraderActivity : AppCompatActivity(), CryptosAdapterListener {

    lateinit var firestoreService: FirestoreService
    private val cryptosAdapter: CryptosAdapter = CryptosAdapter(this)
    private var userName: String? = null
    private var user: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trader)
        firestoreService = FirestoreService(FirebaseFirestore.getInstance())

        userName = intent.extras!![USERNAME_KEY]!!.toString()
        usernameTextView.text = userName

        configureRecyclerView()
        loadCryptos()

        fab.setOnClickListener { view ->
            Snackbar.make(view, getString(R.string.generating_new_cryptos), Snackbar.LENGTH_SHORT)
                .setAction("Info", null).show()
        }
    }

    private fun loadCryptos() {
        firestoreService.getCryptos(object : Callback<List<Crypto>> {
            override fun onSuccess(cryptoList: List<Crypto>?) {
                firestoreService.findUserById(
                    userName!!,
                    object : Callback<User> {
                        override fun onSuccess(result: User?) {
                            user = result
                            if (user!!.cryptosList == null) {
                                val userCryptoList = mutableListOf<Crypto>()
                                for (crypto in cryptoList!!) {
                                    val cryptoUser = Crypto()
                                    cryptoUser.name = crypto.name
                                    cryptoUser.available = crypto.available
                                    cryptoUser.imageUrl = crypto.imageUrl
                                    userCryptoList.add(cryptoUser)
                                }
                                user!!.cryptosList = userCryptoList
                                firestoreService.updateUser(user!!, this)
                            }

                            loadUserCryptos()
                            addRealTimeDataBaseListeners(user!!, cryptoList!!)
                        }

                        override fun onFailed(exception: Exception) {
                            showGeneralServerErrorMessage()
                        }
                    }
                )

                this@TraderActivity.runOnUiThread {
                    cryptosAdapter.cryptoList = cryptoList!!
                    cryptosAdapter.notifyDataSetChanged()
                }
            }

            override fun onFailed(exception: Exception) {
                Log.e("TraderActivity", "error loading cryptos $exception")
                showGeneralServerErrorMessage()
            }
        })
    }

    private fun addRealTimeDataBaseListeners(user: User, cryptoList: List<Crypto>) {
        firestoreService.listenForUpdates(
            user,
            object : RealTimeListener<User> {
                override fun onDataChange(updateData: User) {
                    this@TraderActivity.user = updateData
                    loadUserCryptos()
                }

                override fun onError(exception: Exception) {
                    showGeneralServerErrorMessage()
                }
            }
        )

        firestoreService.listenForUpdates(
            cryptoList,
            object : RealTimeListener<Crypto> {
                override fun onDataChange(updateData: Crypto) {
                    var pos = 0
                    for (crypto in cryptosAdapter.cryptoList) {
                        if (crypto.name == updateData.name) {
                            crypto.available = updateData.available
                            cryptosAdapter.notifyItemChanged(pos)
                        }
                        pos++
                    }
                }

                override fun onError(exception: Exception) {
                    showGeneralServerErrorMessage()
                }
            }
        )
    }

    private fun loadUserCryptos() {
        runOnUiThread {
            if (user != null && user!!.cryptosList != null) {
                infoPanel.removeAllViews()
                for (crypto in user!!.cryptosList!!) {
                    addUserCryptoInformationRow(crypto)
                }
            }
        }
    }

    private fun addUserCryptoInformationRow(crypto: Crypto) {
        val view = LayoutInflater.from(this).inflate(R.layout.coin_information, infoPanel, false)
        view.findViewById<TextView>(R.id.coinLabel).text =
            getString(R.string.coin_info, crypto.name, crypto.available.toString())
        Picasso.get().load(crypto.imageUrl).into(view.findViewById<ImageView>(R.id.coinIcon))
        infoPanel.addView(view)
    }

    private fun configureRecyclerView() {
        recyclerView.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = cryptosAdapter
    }

    fun showGeneralServerErrorMessage() {
        Snackbar.make(
            fab,
            getString(R.string.error_while_connecting_to_the_server),
            Snackbar.LENGTH_LONG
        )
            .setAction("Info", null).show()
    }

    override fun onBuyCryptoClicked(crypto: Crypto) {
        if (crypto.available > 0) {
            for (userCrypto in user!!.cryptosList!!) {
                if (userCrypto.name == crypto.name) {
                    userCrypto.available += 1
                    break
                }
            }
            crypto.available--

            firestoreService.updateUser(user!!, null)
            firestoreService.updateCrypto(crypto)
        }
    }
}
