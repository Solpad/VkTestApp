package com.example.vktestapp.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.vktestapp.db.Converters

@Entity(tableName = "records")
@TypeConverters(Converters::class)
data class Record(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "title")
    var title: String,
    var time: String,
    var filePath: String,
    var duration: Long,
    var playStopButtonStatus: Boolean = false,
    var clickListener: ((Int) -> Unit)
)