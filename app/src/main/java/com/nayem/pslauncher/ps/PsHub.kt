package com.nayem.pslauncher.ps

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri

/**
 * PS Hub — the honest PS4 / PS5 path on Android.
 *
 * There is no native PS4/PS5 play on Android. This hub:
 *  - saves your console details (IP + PSN ID) locally only,
 *  - checks if official PS Remote Play is installed and launches it,
 *  - otherwise opens its Play Store page.
 *
 * v2: Chiaki (open-source Remote Play client, github.com/streetpea/chiaki)
 * would let the launcher stream directly — that needs your PSN account
 * link + console registration, done on your phone, never with a shared key.
 */
object PsHub {
    const val REMOTE_PLAY_PKG = "com.playstation.remoteplay"
    const val REMOTE_PLAY_STORE =
        "https://play.google.com/store/apps/details?id=$REMOTE_PLAY_PKG"

    fun isRemotePlayInstalled(pm: PackageManager): Boolean =
        try {
            pm.getPackageInfo(REMOTE_PLAY_PKG, 0); true
        } catch (_: PackageManager.NameNotFoundException) { false }

    fun launchRemotePlay(context: Context): Boolean {
        val intent = context.packageManager
            .getLaunchIntentForPackage(REMOTE_PLAY_PKG) ?: return false
        context.startActivity(intent)
        return true
    }

    fun openStore(context: Context) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(REMOTE_PLAY_STORE)))
    }

    fun saveConsole(context: Context, ip: String, psnId: String) {
        context.getSharedPreferences("ps_hub", Context.MODE_PRIVATE).edit()
            .putString("console_ip", ip.trim())
            .putString("psn_id", psnId.trim())
            .apply()
    }

    fun loadConsole(context: Context): Pair<String, String> {
        val p = context.getSharedPreferences("ps_hub", Context.MODE_PRIVATE)
        return (p.getString("console_ip", "") ?: "") to
            (p.getString("psn_id", "") ?: "")
    }
}
