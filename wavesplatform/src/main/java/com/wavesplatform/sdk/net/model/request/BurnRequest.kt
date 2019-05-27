/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.sdk.net.model.request

import android.util.Log
import com.google.common.primitives.Bytes
import com.google.common.primitives.Longs
import com.google.gson.annotations.SerializedName
import com.wavesplatform.sdk.utils.Constants
import com.wavesplatform.sdk.Wavesplatform
import com.wavesplatform.sdk.crypto.Base58
import com.wavesplatform.sdk.crypto.CryptoProvider
import com.wavesplatform.sdk.net.model.response.TransactionResponse

data class BurnRequest(
        @SerializedName("assetId") val assetId: String = "",
        @SerializedName("chainId") val chainId: Byte = Wavesplatform.getNetCode(),
        @SerializedName("fee") var fee: Long = 100000L,
        @SerializedName("quantity") var quantity: Long = 1,
        @SerializedName("senderPublicKey") var senderPublicKey: String? = "",
        @SerializedName("timestamp") var timestamp: Long = Wavesplatform.getTime(),
        @SerializedName("type") val type: Int = TransactionResponse.BURN,
        @SerializedName("version") val version: Int = Constants.VERSION,
        @SerializedName("proofs") var proofs: MutableList<String?>? = null,
        @SerializedName("id") var id: String? = null
) {

    fun toSignBytes(): ByteArray {
        return try {
            Bytes.concat(
                    byteArrayOf(TransactionResponse.BURN.toByte()),
                    byteArrayOf(Wavesplatform.getNetCode()),
                    byteArrayOf(chainId),
                    Base58.decode(Wavesplatform.getWallet().publicKeyStr ?: ""),
                    Base58.decode(assetId),
                    Longs.toByteArray(quantity),
                    Longs.toByteArray(fee),
                    Longs.toByteArray(timestamp))
        } catch (e: Exception) {
            Log.e("BurnRequest", "Couldn't create toSignBytes", e)
            ByteArray(0)
        }
    }

    fun sign(privateKey: ByteArray) {
        proofs = mutableListOf(Base58.encode(CryptoProvider.sign(privateKey, toSignBytes())))
    }
}