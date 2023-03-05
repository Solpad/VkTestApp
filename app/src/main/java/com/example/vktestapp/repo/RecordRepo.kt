package com.example.vktestapp.repo

import com.example.vktestapp.db.RecordDatabase
import com.example.vktestapp.model.Record

class RecordRepo(val db: RecordDatabase) {
    suspend fun insertRecord(record: Record) = db.getRecordDao().insertRecord(record)

    suspend fun getTitlesRecords() = db.getRecordDao().getTitlesRecords()

    fun getRecordsLive() = db.getRecordDao().getRecordsLive()

    suspend fun deleteRecord(record: Record) = db.getRecordDao().deleteRecord(record)

}