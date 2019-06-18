/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.quick_action.receive.cryptocurrency

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.sdk.model.response.node.AssetBalanceResponse
import com.wavesplatform.wallet.v2.data.model.service.coinomat.GetTunnelResponse
import com.wavesplatform.sdk.utils.RxUtil
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.Constants.coinomatCryptoCurrencies
import com.wavesplatform.wallet.v2.data.manager.CoinomatServiceManager
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import javax.inject.Inject

@InjectViewState
class CryptoCurrencyPresenter @Inject constructor() : BasePresenter<CryptoCurrencyView>() {

    @Inject
    lateinit var coinomatServiceManager: CoinomatServiceManager
    var assetBalance: AssetBalanceResponse? = null
    var tunnel: GetTunnelResponse? = null
    private var lang: String = "ru_RU"
    var nextStepValidation = false

    fun getTunnel(assetId: String) {
        val currencyFrom = coinomatCryptoCurrencies()[assetId]
        if (currencyFrom.isNullOrEmpty()) {
            viewState.onShowError(App.getAppContext()
                    .getString(R.string.receive_error_network))
            return
        }

        val currencyTo = "W$currencyFrom"

        addSubscription(coinomatServiceManager.createTunnel(currencyFrom, currencyTo, getWavesAddress(), null)
                .flatMap { createTunnel ->
                    coinomatServiceManager.getTunnel(
                            createTunnel.tunnelId,
                            createTunnel.k1,
                            createTunnel.k2,
                            lang)
                }
                .compose(RxUtil.applySchedulersToObservable())
                .subscribe({ tunnel ->
                    this.tunnel = tunnel
                    viewState.onShowTunnel(tunnel)
                }, {
                    viewState.onGatewayError()
                    it.printStackTrace()
                }))
    }
}
