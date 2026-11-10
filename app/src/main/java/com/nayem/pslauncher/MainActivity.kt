package com.nayem.pslauncher

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nayem.pslauncher.booster.GameBooster
import com.nayem.pslauncher.controller.GamepadManager
import com.nayem.pslauncher.ps.PsHubActivity
import com.nayem.pslauncher.ui.GameAdapter
import com.nayem.pslauncher.ui.GameItem

/**
 * PS Launcher v1 — Home.
 * Lightweight: single Activity, RecyclerView, no Compose, no ads.
 * Flow: list games -> tap Boost -> tap game -> play.
 * PS4/PS5 games are NOT launched natively — see PsHubActivity / README.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var booster: GameBooster
    private lateinit var gamepad: GamepadManager
    private lateinit var adapter: GameAdapter
    private lateinit var statusText: TextView
    private var boosted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // AUDIT FIX: statusText must exist BEFORE the gamepad callback
        // can touch it — it was created first and used statusText
        // before findViewById ran (latent crash on a fast pad event).
        statusText = findViewById(R.id.statusText)
        booster = GameBooster(this)
        gamepad = GamepadManager(this) { msg -> statusText.text = msg }

        // AUDIT FIX: the virtual pad was drawn but never wired up.
        findViewById<com.nayem.pslauncher.controller.VirtualControllerView>(R.id.virtualPad)
            .onButton = { msg -> statusText.text = "Virtual pad: $msg" }

        val list = findViewById<RecyclerView>(R.id.gameList)
        list.layoutManager = LinearLayoutManager(this)
        adapter = GameAdapter { game -> launchGame(game) }
        list.adapter = adapter

        findViewById<Button>(R.id.btnBoost).setOnClickListener { toggleBoost() }
        findViewById<Button>(R.id.btnPsHub).setOnClickListener {
            startActivity(Intent(this, PsHubActivity::class.java))
        }
        findViewById<Button>(R.id.btnRefresh).setOnClickListener { loadGames() }
        findViewById<Switch>(R.id.switchVirtualPad).setOnCheckedChangeListener { _, on ->
            findViewById<android.view.View>(R.id.virtualPad).visibility =
                if (on) android.view.View.VISIBLE else android.view.View.GONE
            statusText.text = if (on) "Virtual controller: ON" else "Virtual controller: OFF"
        }

        gamepad.refresh()
        statusText.text = gamepad.summary()
        loadGames()
    }

    private fun loadGames() {
        val games = mutableListOf<GameItem>()
        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        for (ri in pm.queryIntentActivities(intent, 0)) {
            val info = ri.activityInfo.applicationInfo
            val isGame = (info.flags and ApplicationInfo.FLAG_IS_GAME) != 0 ||
                info.category == ApplicationInfo.CATEGORY_GAME
            if (!isGame) continue
            games += GameItem(
                label = ri.loadLabel(pm).toString(),
                packageName = info.packageName,
                icon = ri.loadIcon(pm)
            )
        }
        games.sortBy { it.label.lowercase() }
        adapter.submit(games)
        findViewById<TextView>(R.id.countText).text =
            if (games.isEmpty()) "No games found — install a game, then Refresh"
            else "${games.size} games on this phone"
    }

    private fun launchGame(game: GameItem) {
        val launch = packageManager.getLaunchIntentForPackage(game.packageName)
        if (launch == null) {
            Toast.makeText(this, "Can't launch ${game.label}", Toast.LENGTH_SHORT).show()
            return
        }
        if (!boosted) booster.boost(light = true) // auto light-boost on launch
        startActivity(launch)
    }

    private fun toggleBoost() {
        boosted = if (!boosted) {
            val report = booster.boost(light = false)
            statusText.text = report
            Toast.makeText(this, "Booster ON — good luck!", Toast.LENGTH_SHORT).show()
            true
        } else {
            booster.restore()
            statusText.text = "Booster OFF — phone restored"
            false
        }
        findViewById<Button>(R.id.btnBoost).text =
            if (boosted) "BOOST ON — Tap to restore" else "⚡ BOOST PHONE"
    }

    // Physical controller: feed every key / stick event to GamepadManager.
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (gamepad.handleKeyEvent(event)) return true
        return super.dispatchKeyEvent(event)
    }

    override fun dispatchGenericMotionEvent(event: MotionEvent): Boolean {
        if (gamepad.handleMotionEvent(event)) return true
        return super.dispatchGenericMotionEvent(event)
    }

    override fun onDestroy() {
        if (boosted) booster.restore()
        super.onDestroy()
    }
}

// Keep MainActivity lean — data class lives with the adapter.
