package com.nayem.pslauncher.ps

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.nayem.pslauncher.R

class PsHubActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ps_hub)

        val ip = findViewById<EditText>(R.id.editConsoleIp)
        val psn = findViewById<EditText>(R.id.editPsnId)
        val status = findViewById<TextView>(R.id.psStatus)

        val (savedIp, savedPsn) = PsHub.loadConsole(this)
        ip.setText(savedIp); psn.setText(savedPsn)

        val installed = PsHub.isRemotePlayInstalled(packageManager)
        status.text = if (installed)
            "PS Remote Play is installed — your PS4/PS5 streams from your console. " +
            "Console + phone on the same Wi-Fi works best."
        else
            "PS Remote Play is NOT installed. Install it from Play Store — " +
            "PS4/PS5 games stream from your own console, they don't run inside this launcher."

        findViewById<Button>(R.id.btnSaveConsole).setOnClickListener {
            PsHub.saveConsole(this, ip.text.toString(), psn.text.toString())
            Toast.makeText(this, "Console saved (on this phone only)", Toast.LENGTH_SHORT).show()
        }
        findViewById<Button>(R.id.btnLaunchRemotePlay).setOnClickListener {
            if (!PsHub.launchRemotePlay(this)) {
                Toast.makeText(this, "Remote Play not installed — opening Play Store", Toast.LENGTH_SHORT).show()
                PsHub.openStore(this)
            }
        }
    }
}
