/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.model.remote.response

import com.google.gson.annotations.SerializedName

data class Height(
    @SerializedName("height") var height: Int = 0
)