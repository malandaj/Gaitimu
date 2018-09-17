package tech.eroland.gaitimu

import android.content.ContentValues
import org.json.JSONObject

class Patient{
    private var patientName:String?=null
    private var patientAge:Int?=null
    private var patientGender:Int?=null
    private var patientLocation:String?=null
    private var patientObservations:String?=null

    constructor(patientName:String, patientAge:Int, patientGender:Int, patientLocation:String, patientObservations:String){
        this.patientName = patientName
        this.patientAge = patientAge
        this.patientGender = patientGender
        this.patientLocation = patientLocation
        this.patientObservations = patientObservations
    }

    fun toContentValues(): ContentValues {
        val values = ContentValues()
        values.put("Name", this.patientName)
        values.put("Age", this.patientAge)
        values.put("Gender", this.patientGender)
        values.put("Location", this.patientLocation)
        values.put("Observations", this.patientObservations)
        return values
    }

    fun toJSON(): JSONObject {
        val jsonObject = JSONObject()
        try {
            jsonObject.put("type", "patient")
            jsonObject.put("name", this.patientName)
            jsonObject.put("age", this.patientAge)
            jsonObject.put("gender", this.patientGender)
            jsonObject.put("location", this.patientLocation)
            jsonObject.put("observations", this.patientObservations)
        }catch (e:Exception){
            e.printStackTrace()
        }
        return jsonObject
    }
}