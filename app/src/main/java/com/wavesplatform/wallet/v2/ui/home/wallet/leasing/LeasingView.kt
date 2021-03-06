/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.wallet.leasing

import com.wavesplatform.sdk.model.response.node.AssetBalanceResponse
import com.wavesplatform.sdk.model.response.node.HistoryTransactionResponse
import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface LeasingView : BaseMvpView {
    fun showBalances(wavesAsset: AssetBalanceResponse)
    fun showActiveLeasingTransaction(transactions: List<HistoryTransactionResponse>)
    fun afterFailedLoadLeasing()
}
