package com.example.newsapp.roomDb

import androidx.room.TypeConverter
import com.example.newsapp.models.Source

class Converters {

    @TypeConverter
    fun fromSource(source: Source): String {
        return source.name!!  // Assuming you only need the name, but you could include both id and name if necessary
    }

    @TypeConverter
    fun toSource(name: String): Source {
        return Source(name, name)
    }
}
