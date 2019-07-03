/*
 * Created by Eduard Zaydel on 25/6/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.manager.gateway.manager

import com.wavesplatform.wallet.v1.util.MoneyUtil
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.manager.base.BaseDataManager
import com.wavesplatform.wallet.v2.data.model.local.gateway.GatewayDepositArgs
import com.wavesplatform.wallet.v2.data.model.local.gateway.GatewayMetadataArgs
import com.wavesplatform.wallet.v2.data.model.local.gateway.GatewayWithdrawArgs
import com.wavesplatform.wallet.v2.data.model.remote.request.TransactionsBroadcastRequest
import com.wavesplatform.wallet.v2.data.model.remote.request.gateway.InitGatewayRequest
import com.wavesplatform.wallet.v2.data.model.remote.response.gateway.GatewayDeposit
import com.wavesplatform.wallet.v2.data.model.remote.response.gateway.GatewayMetadata
import com.wavesplatform.wallet.v2.data.model.remote.response.gateway.gateway.InitDepositResponse
import com.wavesplatform.wallet.v2.data.model.remote.response.gateway.gateway.InitWithdrawResponse
import com.wavesplatform.wallet.v2.data.model.remote.response.gateway.gateway.SendTransactionResponse
import com.wavesplatform.wallet.v2.util.clearBalance
import com.wavesplatform.wallet.v2.util.parseAlias
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GatewayDataManager @Inject constructor() : BaseDataManager(), BaseGateway {

    override fun loadGatewayMetadata(args: GatewayMetadataArgs): Observable<GatewayMetadata> {
        return gatewayService.initWithdraw(InitGatewayRequest(args.address, args.asset?.assetId))
                .map { response ->
                    val gatewayFee = MoneyUtil.getScaledText(response.fee, args.asset).clearBalance().toBigDecimal()
                    val gatewayMin = MoneyUtil.getScaledText(response.minAmount, args.asset).clearBalance().toBigDecimal()
                    val gatewayMax = MoneyUtil.getScaledText(response.maxAmount, args.asset).clearBalance().toBigDecimal()

                    return@map GatewayMetadata(gatewayMin, gatewayMax, gatewayFee, response.processId, response.recipientAddress)
                }
    }

    override fun makeWithdraw(args: GatewayWithdrawArgs): Observable<TransactionsBroadcastRequest> {
        return loadGatewayMetadata(GatewayMetadataArgs(args.asset, args.transaction.recipient))
                .flatMap { metadata ->
                    args.transaction.attachment = metadata.gatewayProcessId
                    args.transaction.recipient = metadata.gatewayRecipientAddress ?: args.transaction.recipient

                    args.transaction.sign(getPrivateKey())

                    return@flatMap gatewayService.sendWithdrawTransaction(args.transaction)
                            .map {
                                args.transaction.recipient = args.transaction.recipient.parseAlias()
                                return@map args.transaction
                            }
                }
    }

    override fun makeDeposit(args: GatewayDepositArgs): Observable<GatewayDeposit> {
        return gatewayService.initDeposit(InitGatewayRequest(getAddress(), args.asset?.assetId))
                .map { response ->
                    val currencyFrom = Constants.coinomatCryptoCurrencies()[args.asset?.assetId]
                    val address = response.address
                    val gatewayMin = MoneyUtil.getScaledText(response.minAmount, args.asset).clearBalance().toBigDecimal()

                    return@map GatewayDeposit(gatewayMin, address, currencyFrom)
                }
    }

}