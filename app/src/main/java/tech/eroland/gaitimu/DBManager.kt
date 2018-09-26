package tech.eroland.gaitimu

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteQueryBuilder
import android.widget.Toast

class DBManager{
    private var sqlDB:SQLiteDatabase?=null

    val dbName = "MyPatients"
    private val dbTable = "Patients"
    private val colID = "ID"
    private val colName = "Name"
    private val colAge = "Age"
    private val colGender = "Gender"
    private val colLocation = "Location"
    private val colObservations = "Observations"
    private val dbVersion = 1

    val sqlCreateTable = "CREATE TABLE IF NOT EXISTS " + dbTable + " (" +
            colID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            colName + " TEXT NOT NULL," +
            colAge + " TEXT," +
            colGender + " TEXT," +
            colLocation + " TEXT," +
            colObservations + " TEXT);"

    constructor(context:Context){
        val db=DatabaseHelperPatients(context)
        sqlDB = db.writableDatabase
    }

    fun insert(values:ContentValues):Long{
        val id = sqlDB!!.insert(dbTable, "", values)
        return id
    }

//    fun delete(selection: String, selectionArgs: Array<String>):Int{
//        val count = sqlDB!!.delete(dbTable, selection, selectionArgs)
//        return count
//    }

    fun query(projection: Array<String>, selection:String, selectionArgs:Array<String>, sortOrder:String):Cursor{
        val qb = SQLiteQueryBuilder()
        qb.tables = dbTable
        val cursor = qb.query(sqlDB, projection, selection, selectionArgs, null, null, sortOrder)
        return cursor
    }

    inner class DatabaseHelperPatients:SQLiteOpenHelper{
        private var context:Context?=null

        constructor(context:Context):super(context, dbName, null, dbVersion){
            this.context = context
        }
        override fun onCreate(p0: SQLiteDatabase?) {
            p0!!.execSQL(sqlCreateTable)
            Toast.makeText(this.context," database is created", Toast.LENGTH_LONG).show()
        }

        override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
            p0!!.execSQL("Drop table IF EXISTS $dbTable")
        }

    }
}