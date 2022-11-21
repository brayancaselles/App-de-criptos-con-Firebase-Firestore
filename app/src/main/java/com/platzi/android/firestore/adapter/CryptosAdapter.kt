package com.platzi.android.firestore.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.platzi.android.firestore.R
import com.platzi.android.firestore.databinding.CryptoRowBinding
import com.platzi.android.firestore.model.Crypto
import com.squareup.picasso.Picasso

class CryptosAdapter(private val cryptosAdapterListener: CryptosAdapterListener) :
    RecyclerView.Adapter<CryptosAdapter.ViewHolder>() {

    var cryptoList: List<Crypto> = ArrayList()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val binding = CryptoRowBinding.bind(view)

        fun bind(crypto: Crypto, cryptosAdapterListener: CryptosAdapterListener) {
            with(binding) {
                Picasso.get().load(crypto.imageUrl).into(image)
                nameTextView.text = crypto.name
                availableTextView.text =
                    itemView.context.getString(
                        R.string.available_message,
                        crypto.available.toString()
                    )
                buyButton.setOnClickListener {
                    cryptosAdapterListener.onBuyCryptoClicked(crypto)
                }
            }
        }
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(p0.context)
        return ViewHolder(layoutInflater.inflate(R.layout.crypto_row, p0, false))
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        val crypto = cryptoList[p1]
        p0.bind(crypto, cryptosAdapterListener)
    }

    override fun getItemCount(): Int {
        return cryptoList.size
    }
}
