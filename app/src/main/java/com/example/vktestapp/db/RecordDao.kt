package com.example.vktestapp.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.vktestapp.model.Record

@Dao
interface RecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: Record): Long

    @Query("SELECT * FROM records")
    fun getRecordsLive(): LiveData<List<Record>>

    @Query("SELECT title FROM records")
    suspend fun getTitlesRecords(): List<String>

    @Delete
    suspend fun deleteRecord(location: Record)
}