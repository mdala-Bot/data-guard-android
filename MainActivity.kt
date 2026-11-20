package com.dataguard.app

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private val prefs by lazy { getSharedPreferences("firewall", MODE_PRIVATE) }
    private val selected = linkedSetOf<String>()
    private lateinit var list: LinearLayout
    private val requestVpn = 4102

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        selected.addAll(prefs.getStringSet("blocked", emptySet()) ?: emptySet())
        buildScreen()
    }

    private fun buildScreen() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 28, 24, 20)
            setBackgroundColor(0xFF101D3A.toInt())
        }
        val title = TextView(this).apply {
            text = "DATA GUARD 4.0"
            textSize = 25f
            setTextColor(0xFFFFFFFF.toInt())
            gravity = Gravity.CENTER
        }
        val subtitle = TextView(this).apply {
            text = "Chagua programu za kuzuiwa kutumia intaneti"
            textSize = 14f
            setTextColor(0xFFB8D8F0.toInt())
            setPadding(0, 12, 0, 18)
        }
        val save = Button(this).apply {
            text = "HIFADHI NA WASHA FIREWALL"
            setOnClickListener { saveAndStart() }
        }
        val stop = Button(this).apply {
            text = "ZIMA FIREWALL"
            setOnClickListener {
                startService(Intent(this@MainActivity, FirewallVpnService::class.java).setAction(FirewallVpnService.ACTION_STOP))
                Toast.makeText(this@MainActivity, "Firewall imeombwa kuzimwa", Toast.LENGTH_SHORT).show()
            }
        }
        val note = TextView(this).apply {
            text = "Programu zilizowekwa alama ndizo zitakazoelekezwa kwenye VPN ya ndani na pakiti zake kutupwa. Android itaomba ruhusa ya VPN. VPN nyingine haitafanya kazi kwa wakati mmoja."
            textSize = 12f
            setTextColor(0xFFFFD98A.toInt())
            setPadding(0, 12, 0, 12)
        }
        list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val scroll = ScrollView(this).apply { addView(list) }
        root.addView(title)
        root.addView(subtitle)
        root.addView(save)
        root.addView(stop)
        root.addView(note)
        root.addView(scroll, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        setContentView(root)
        loadApps()
    }

    @Suppress("DEPRECATION")
    private fun loadApps() {
        val apps = packageManager.getInstalledApplications(0)
            .filter { it.packageName != packageName }
            .sortedBy { packageManager.getApplicationLabel(it).toString().lowercase() }
        for (app in apps) {
            val name = packageManager.getApplicationLabel(app).toString()
            val cb = CheckBox(this).apply {
                text = "$name  (${app.packageName})"
                textSize = 13f
                setTextColor(0xFFFFFFFF.toInt())
                isChecked = selected.contains(app.packageName)
                setOnCheckedChangeListener { _, checked ->
                    if (checked) selected.add(app.packageName) else selected.remove(app.packageName)
                }
            }
            list.addView(cb)
        }
    }

    private fun saveAndStart() {
        prefs.edit().putStringSet("blocked", selected.toSet()).apply()
        if (selected.isEmpty()) {
            Toast.makeText(this, "Chagua angalau programu moja", Toast.LENGTH_LONG).show()
            return
        }
        val prepare = VpnService.prepare(this)
        if (prepare != null) startActivityForResult(prepare, requestVpn) else startFirewall()
    }

    @Deprecated("Android callback")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == requestVpn && resultCode == RESULT_OK) startFirewall()
    }

    private fun startFirewall() {
        val intent = Intent(this, FirewallVpnService::class.java).setAction(FirewallVpnService.ACTION_START)
        startService(intent)
        Toast.makeText(this, "Ombi la kuwasha firewall limetumwa", Toast.LENGTH_SHORT).show()
    }
}
