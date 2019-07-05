/*
 * Created by Eduard Zaydel on 26/6/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.model.local.gateway

import com.wavesplatform.wallet.v2.data.model.remote.request.TransactionsBroadcastRequest
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance

data class GatewayWithdrawArgs(var transaction: TransactionsBroadcastRequest,
                               var asset: AssetBalance?,
                               var coinomatMoneroPaymentId: String? = null)