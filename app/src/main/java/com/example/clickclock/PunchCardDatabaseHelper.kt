package com.example.clickclock

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime

class PunchCardDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_NAME = "ClickClock.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "PunchCards"
        private const val COLUMN_ID = "id"
        private const val COLUMN_PUNCH_DATE = "date"
        private const val COLUMN_PUNCH_IN_TIME = "punch_in_time"
        private const val COLUMN_PUNCH_OUT_TIME = "punch_out_time"
        private const val COLUMN_DURATION = "duration"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery =
            "CREATE TABLE $TABLE_NAME ($COLUMN_ID INTEGER PRIMARY KEY, $COLUMN_PUNCH_DATE TEXT, $COLUMN_PUNCH_IN_TIME TEXT, $COLUMN_PUNCH_OUT_TIME TEXT, $COLUMN_DURATION TEXT)"
        db?.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        val dropTableQuery = "DROP TABLE IF EXISTS $TABLE_NAME"
        db?.execSQL(dropTableQuery)
        onCreate(db)
    }

    fun insertPunchCard(punchCard: PunchCard) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_PUNCH_DATE, punchCard.date.toString())
            put(COLUMN_PUNCH_IN_TIME, punchCard.punchIn.toString())
            put(COLUMN_PUNCH_OUT_TIME, punchCard.punchOut.toString())
            put(COLUMN_DURATION, punchCard.duration.toString())
        }
        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getAllPunchCards(): MutableList<PunchCard> {
        val punchCardList = mutableListOf<PunchCard>()
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_NAME"
        val cursor = db.rawQuery(query, null)

        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
            val punchDate = LocalDate.parse(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PUNCH_DATE)))
            val punchInTime = LocalTime.parse(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PUNCH_IN_TIME)))
            val punchOutTime = LocalTime.parse(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PUNCH_OUT_TIME)))
            val duration = Duration.parse(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DURATION)))

            val punchCard = PunchCard(id, punchDate, punchInTime, punchOutTime, duration)
            punchCardList.add(punchCard)
        }

        cursor.close()
        db.close()
        return punchCardList
    }

//    @RequiresApi(Build.VERSION_CODES.O)
//    fun getPunchCardById(itemId: Int): PunchCard {
//        val db = readableDatabase
//        val query = "SELECT * FROM $TABLE_NAME WHERE $COLUMN_ID = $itemId"
//        val cursor = db.rawQuery(query, null)
//        cursor.moveToFirst()
//
//        val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
//        val punchDate = LocalDate.parse(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PUNCH_DATE)))
//        val punchInTime = LocalTime.parse(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PUNCH_IN_TIME)))
//        val punchOutTime = LocalTime.parse(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PUNCH_OUT_TIME)))
//        val duration = Duration.parse(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DURATION)))
//
//        cursor.close()
//        db.close()
//        return PunchCard(id, punchDate, punchInTime, punchOutTime, duration)
//    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getPunchCardsByDate(date: LocalDate): MutableList<PunchCard> {
        val punchCardList = mutableListOf<PunchCard>()
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_NAME WHERE $COLUMN_PUNCH_DATE ='$date'"
        Log.d("Query", query)

        val cursor = db.rawQuery(query, null)
        cursor.moveToFirst()

        while (!cursor.isAfterLast)
        {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
            val punchDate = LocalDate.parse(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PUNCH_DATE)))
            val punchInTime = LocalTime.parse(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PUNCH_IN_TIME)))
            val punchOutTime = LocalTime.parse(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PUNCH_OUT_TIME)))
            val duration = Duration.parse(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DURATION)))

            val punchCard = PunchCard(id, punchDate, punchInTime, punchOutTime, duration)
            punchCardList.add(punchCard)
            cursor.moveToNext()
        }

        cursor.close()
        db.close()
        return punchCardList
    }

    fun deleteItem(punchCardId: Int) {
        val db = writableDatabase
        val whereClause = "$COLUMN_ID = ?"
        val whereArgs = arrayOf(punchCardId.toString())
        db.delete(TABLE_NAME, whereClause, whereArgs)
        db.close()
    }

}