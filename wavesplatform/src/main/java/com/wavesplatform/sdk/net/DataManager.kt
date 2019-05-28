package com.wavesplatform.sdk.net

import android.content.Context
import android.util.Log
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.ihsanbal.logging.Level
import com.ihsanbal.logging.LoggingInterceptor
import com.wavesplatform.sdk.BuildConfig
import com.wavesplatform.sdk.net.service.ApiService
import com.wavesplatform.sdk.net.service.MatcherService
import com.wavesplatform.sdk.net.service.NodeService
import com.wavesplatform.sdk.utils.Constants
import com.wavesplatform.sdk.utils.Servers
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import ren.yale.android.retrofitcachelibrx2.RetrofitCache
import ren.yale.android.retrofitcachelibrx2.intercept.CacheForceInterceptorNoNet
import ren.yale.android.retrofitcachelibrx2.intercept.CacheInterceptorOnNet
import retrofit2.CallAdapter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Singleton
open class DataManager(var context: Context,
                       private var adapterFactory: CallAdapter.Factory? = RxJava2CallAdapterFactory.create()) {

    lateinit var nodeService: NodeService
    lateinit var apiService: ApiService
    lateinit var matcherService: MatcherService
    private var cookies: HashSet<String> = hashSetOf()
    var servers: Servers = Servers.DEFAULT
    private val onErrorListeners = mutableListOf<OnErrorListener>()

    init {
        adapterFactory = CallAdapterFactory(object : OnErrorListener{
            override fun onError(exception: RetrofitException) {
                onErrorListeners.forEach { it.onError(exception) }
            }
        })
        createServices()
    }

    fun createServices() {
        nodeService = createService(
                addSlash(servers.nodeUrl),
                adapterFactory ?: RxJava2CallAdapterFactory.create())
                .create(NodeService::class.java)

        apiService = createService(
                addSlash(servers.dataUrl),
                adapterFactory ?: RxJava2CallAdapterFactory.create())
                .create(ApiService::class.java)

        matcherService = createService(
                addSlash(servers.matcherUrl),
                adapterFactory ?: RxJava2CallAdapterFactory.create())
                .create(MatcherService::class.java)
    }


    fun addOnErrorListener(errorListener: OnErrorListener) {
        onErrorListeners.add(errorListener)
    }

    fun removeOnErrorListener(errorListener: OnErrorListener) {
        onErrorListeners.remove(errorListener)
    }

    fun createService(
            baseUrl: String,
            adapterFactory: CallAdapter.Factory = RxJava2CallAdapterFactory.create()): Retrofit {
        val retrofit = Retrofit.Builder()
                .baseUrl(addSlash(baseUrl))
                .client(createClient())
                .addConverterFactory(ScalarsConverterFactory.create())
                .addCallAdapterFactory(adapterFactory)
                .addConverterFactory(createGsonFactory())
                .build()
        RetrofitCache.getInstance().addRetrofit(retrofit)
        return retrofit
    }

    private fun addSlash(url: String): String {
        return if (url.endsWith("/")) {
            url
        } else {
            "$url/"
        }
    }

    private fun createClient(timeout: Long = 30L): OkHttpClient {
        return OkHttpClient.Builder()
                .cache(createCache())
                .readTimeout(timeout, TimeUnit.SECONDS)
                .writeTimeout(timeout, TimeUnit.SECONDS)
                .addInterceptor(receivedCookiesInterceptor())
                .addInterceptor(addCookiesInterceptor())
                .addInterceptor(CacheForceInterceptorNoNet())
                .addInterceptor(createHostInterceptor())
                .addNetworkInterceptor(CacheInterceptorOnNet())
                .addInterceptor(LoggingInterceptor.Builder()
                        .loggable(BuildConfig.DEBUG)
                        .setLevel(Level.BASIC)
                        .log(Log.INFO)
                        .request("Request")
                        .response("Response")
                        .build())
                .build()
    }

    private fun receivedCookiesInterceptor(): Interceptor {
        return Interceptor { chain ->
            val originalResponse = chain.proceed(chain.request())
            if (originalResponse.request().url().url().toString()
                            .contains(Constants.URL_NODE)
                    && originalResponse.headers("Set-Cookie").isNotEmpty()
                    && this.cookies.isEmpty()) {
                val cookies = originalResponse.headers("Set-Cookie")
                        .toHashSet()
                this.cookies = cookies
            }
            originalResponse
        }
    }

    private fun addCookiesInterceptor(): Interceptor {
        return Interceptor { chain ->
            if (this.cookies.isNotEmpty() && chain.request().url().url().toString()
                            .contains(Constants.URL_NODE)) {
                val builder = chain.request().newBuilder()
                this.cookies.forEach {
                    builder.addHeader("Cookie", it)
                }
                chain.proceed(builder.build())
            } else {
                chain.proceed(chain.request())
            }
        }
    }

    private fun createHostInterceptor(): HostSelectionInterceptor {
        return HostSelectionInterceptor(servers)
    }

    private fun createCache(): Cache {
        val cacheSize = 200 * 1024 * 1024
        val cacheDirectory = File(context.cacheDir, "httpcache")
        return Cache(cacheDirectory, cacheSize.toLong())
    }

    private fun createGsonFactory(): GsonConverterFactory {
        return GsonConverterFactory.create(GsonBuilder()
                .setLenient()
                .setPrettyPrinting()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .create())
    }
}
