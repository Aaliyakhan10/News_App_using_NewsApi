package com.example.newsapp.roomDb

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.newsapp.models.Article

@Database(
    entities = [Article::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ArticleDatabase : RoomDatabase() {
    abstract fun getArticleDao(): ArticleDoa

    companion object {
        @Volatile
        private var database: ArticleDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context): ArticleDatabase = database ?: synchronized(LOCK) {
            database ?: createDatabase(context).also { it ->
                database = it
            }
        }

        private fun createDatabase(context: Context): ArticleDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                ArticleDatabase::class.java,
                "article.db"
            ).build()
        }
    }
}
