package com.azamovhudstc.taskapp.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import dev.brahmkshatriya.nicehttp.addGenericDns
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File
import java.io.PrintWriter
import java.io.Serializable
import java.io.StringWriter
import java.util.concurrent.CancellationException
import kotlin.reflect.KFunction

lateinit var okHttpClient: OkHttpClient
lateinit var cache: Cache

fun initializeNetwork(context: Context) {
    val dns = loadData<Int>("settings_dns")
    cache = Cache(
        File(context.cacheDir, "http_cache"),
        5 * 1024L * 1024L // 5 MiB
    )
    okHttpClient = OkHttpClient.Builder()
        .followRedirects(true)
        .followSslRedirects(true)
        .cache(cache)
        .apply {
            when (dns) {
                1 -> addGoogleDns()
                2 -> addCloudFlareDns()
                3 -> addAdGuardDns()
            }
        }
        .build()
}

fun OkHttpClient.Builder.addGoogleDns() = (
        addGenericDns(
            "https://dns.google/dns-query",
            listOf(
                "8.8.4.4",
                "8.8.8.8"
            )
        ))

fun OkHttpClient.Builder.addCloudFlareDns() = (
        addGenericDns(
            "https://cloudflare-dns.com/dns-query",
            listOf(
                "1.1.1.1",
                "1.0.0.1",
                "2606:4700:4700::1111",
                "2606:4700:4700::1001"
            )
        ))

fun OkHttpClient.Builder.addAdGuardDns() = (
        addGenericDns(
            "https://dns.adguard.com/dns-query",
            listOf(
                // "Non-filtering"
                "94.140.14.140",
                "94.140.14.141",
            )
        ))

fun isOnline(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    return tryWith {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val cap = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            return@tryWith if (cap != null) {
                when {
                    cap.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) ||
                            cap.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            cap.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ||
                            cap.hasTransport(NetworkCapabilities.TRANSPORT_LOWPAN) ||
                            cap.hasTransport(NetworkCapabilities.TRANSPORT_USB) ||
                            cap.hasTransport(NetworkCapabilities.TRANSPORT_VPN) ||
                            cap.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            cap.hasTransport(NetworkCapabilities.TRANSPORT_WIFI_AWARE) -> true
                    else -> false
                }
            } else false
        } else true
    } ?: false
}

fun <T> tryWith(post: Boolean = false, snackbar: Boolean = true, call: () -> T): T? {
    return try {
        call.invoke()
    } catch (e: Throwable) {
        logError(e, post, snackbar)
        null
    }
}

fun logError(e: Throwable, post: Boolean = true, snackbar: Boolean = true) {
    val sw = StringWriter()
    val pw = PrintWriter(sw)
    e.printStackTrace(pw)
    val stackTrace: String = sw.toString()
    if (post) {
        if (snackbar)
            snackString(e.localizedMessage, null, stackTrace)
        else
            toast(e.localizedMessage)
    }
    e.printStackTrace()
}


data class FileUrl(
    val url: String,
    val headers: Map<String, String> = mapOf()
) : Serializable {
    companion object {
        operator fun get(url: String?, headers: Map<String, String> = mapOf()): FileUrl? {
            return FileUrl(url ?: return null, headers)
        }
    }
}

//Credits to leg
data class Lazier<T>(
    val lClass: KFunction<T>,
    val name: String
) {
    val get = lazy { lClass.call() }
}


