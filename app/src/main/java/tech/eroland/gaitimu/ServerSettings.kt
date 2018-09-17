package tech.eroland.gaitimu

import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast

class ServerSettings{
    private var context:Context?=null
    private var sharedRef:SharedPreferences?=null
    private var ws_ip:String?=null
    private var ws_port:Int?=null
    private var nSensors:Int?=null

    constructor(context: Context){
        this.context = context
        sharedRef = context.getSharedPreferences("myref", Context.MODE_PRIVATE)
        ws_ip = sharedRef!!.getString(this.context!!.getString(R.string.wsIP), "0.0.0.0")
        ws_port = sharedRef!!.getInt(this.context!!.getString(R.string.wsPort), 3000)
        nSensors = sharedRef!!.getInt(this.context!!.getString(R.string.nSensors), 1)
    }

    fun saveData(ip:String, port:Int, sensors:Int){
        val editor = sharedRef!!.edit()
        editor.putString(this.context!!.getString(R.string.wsIP), ip)
        editor.putInt(this.context!!.getString(R.string.wsPort), port)
        editor.putInt(this.context!!.getString(R.string.nSensors), sensors)
        editor.apply()
        Toast.makeText(this.context, "Configuración guardada", Toast.LENGTH_LONG).show()
    }

    fun toContentValues(): ContentValues {
        val values = ContentValues()
        values.put(this.context!!.getString(R.string.wsIP), this.ws_ip)
        values.put(this.context!!.getString(R.string.wsPort), this.ws_port)
        values.put(this.context!!.getString(R.string.nSensors), this.nSensors)
        return values
    }
}