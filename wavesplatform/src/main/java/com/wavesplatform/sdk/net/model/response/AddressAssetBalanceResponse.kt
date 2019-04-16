/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.sdk.net.model.response

import com.google.gson.annotations.SerializedName

data class AddressAssetBalanceResponse(
    @SerializedName("address") var address: String = "",
    @SerializedName("assetId") var assetId: String = "",
    @SerializedName("balance") var balance: Long = 0L
)