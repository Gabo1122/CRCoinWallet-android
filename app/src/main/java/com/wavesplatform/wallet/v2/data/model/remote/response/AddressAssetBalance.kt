/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.model.remote.response

import com.google.gson.annotations.SerializedName

data class AddressAssetBalance(
    @SerializedName("address") var address: String = "",
    @SerializedName("assetId") var assetId: String = "",
    @SerializedName("balance") var balance: Long = 0L
)