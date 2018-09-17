package tech.eroland.gaitimu

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity() {
    private var serverSettings:ServerSettings?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        setSupportActionBar(toolbar)
        supportActionBar!!.setTitle(R.string.config_str)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        serverSettings = ServerSettings(this)
        btn_save.setOnClickListener {
            saveSettings()
        }
        populateForm()
    }

    private fun saveSettings(){
        val ip = inputIP.text.toString()
        val port = inputPort.text.toString().toInt()
        val sensors = inputSensors.text.toString().toInt()
        serverSettings!!.saveData(ip, port, sensors)
    }

    private fun populateForm(){
        val values = serverSettings!!.toContentValues()
        inputIP.setText(values.getAsString(this.getString(R.string.wsIP)))
        inputPort.setText(values.getAsString(this.getString(R.string.wsPort)))
        inputSensors.setText(values.getAsString(this.getString(R.string.nSensors)))
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
