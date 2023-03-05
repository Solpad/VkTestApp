package com.example.vktestapp.db

import android.util.Log
import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromListener(listener: (Int) -> Unit): String {  return "" }

    @TypeConverter
    fun toListener(str: String): ((Int) -> Unit) { return { } }
}