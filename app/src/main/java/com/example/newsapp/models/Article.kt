package com.example.newsapp.models

import androidx.room.Entity
import androidx.room.PrimaryKey

import java.io.Serializable

@Entity(tableName = "articleNews")

data class Article(
    @PrimaryKey(autoGenerate = true)
    val Id: Int? = null,
    val author: String,
    val content: String,
    val description: String,
    val publishedAt: String,
    val source: Source = Source("",""),  // Default Source to avoid null issues
    val title: String,
    val url: String,
    val urlToImage: String
) : Serializable
