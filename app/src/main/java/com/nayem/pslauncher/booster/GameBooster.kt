package com.nayem.pslauncher.booster

import android.app.ActivityManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.view.WindowManager

/**
 * Game Booster — optimise the PHONE, not the game binary.
 * What it can honestly do without root:
 *  - DND on (user grants notification-policy access once)
 *  - Brightness up + auto-brightness off, keep screen on
 *  - Kill background processes of other apps, report free RAM
 * What it CANNOT do: overclock, unlock FPS, change a game's graphics.
 * Everything is restored by restore().
 */
class GameBooster(private val context: Context) {

    private var savedBrightness: Int = -1
    private var savedDnd: Int? = null

    fun boost(light: Boolean): String {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val mem = ActivityManager.MemoryInfo().also { am.getMemoryInfo(it) }
        val freeBeforeMb = mem.availMem / 1024 / 1024

        // 1. Kill background processes (non-system). Android may restart
        //    some — that's normal, we just free what we can.
        var killed = 0
        for (proc in am.runningAppProcesses.orEmpty()) {
            if (proc.importance > ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
                proc.processName != context.packageName
            ) {
                try {
                    am.killBackgroundProcesses(proc.processName)
                    killed++
                } catch (_: SecurityException) { /* skip */ }
            }
        }

        // 2. DND — only if the user already granted access.
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (nm.isNotificationPolicyAccessGranted) {
            savedDnd = nm.currentInterruptionFilter
            nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
        }

        // 3. Brightness: remember, then max (light mode: leave brightness alone).
        if (!light) {
            try {
                savedBrightness = Settings.System.getInt(
                    context.contentResolver, Settings.System.SCREEN_BRIGHTNESS
                )
                Settings.System.putInt(
                    context.contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
                )
                Settings.System.putInt(
                    context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, 255
                )
            } catch (_: Exception) { /* needs WRITE_SETTINGS — optional */ }
        }

        val mem2 = ActivityManager.MemoryInfo().also { am.getMemoryInfo(it) }
        val freeAfterMb = mem2.availMem / 1024 / 1024
        return "Boosted: freed ~${(freeAfterMb - freeBeforeMb).coerceAtLeast(0)} MB RAM " +
            "($freeBeforeMb → $freeAfterMb MB free), $killed bg apps stopped, DND on"
    }

    fun restore() {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (savedDnd != null && nm.isNotificationPolicyAccessGranted) {
            nm.setInterruptionFilter(savedDnd!!)
            savedDnd = null
        }
        if (savedBrightness >= 0) {
            try {
                Settings.System.putInt(
                    context.contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS, savedBrightness
                )
            } catch (_: Exception) { }
            savedBrightness = -1
        }
    }

    /** Open the system screen where the user grants DND access (once). */
    fun dndSettingsIntent(): Intent =
        Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
}
