package com.example.newsapp.roomDb

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.newsapp.models.Article

@Dao
interface ArticleDoa {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarticle(article: Article):Long
    @Query("SELECT * FROM articleNews")
    fun getAllArticle():LiveData<List<Article>>
    @Delete
    suspend fun deleteArticle(article: Article)
}