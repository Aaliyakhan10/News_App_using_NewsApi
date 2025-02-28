package com.example.newsapp.repository

import com.example.newsapp.api.RetrofitInstance
import com.example.newsapp.models.Article
import com.example.newsapp.roomDb.ArticleDatabase

class NewsRepository(val db:ArticleDatabase) {
    suspend fun getHeadlines(countryCode:String,pageNumber: Int)=
        RetrofitInstance.api.getHeadlines(countryCode,pageNumber)
    suspend fun searchArticle(searchQuery: String,pageNumber: Int)=
        RetrofitInstance.api.searchForNews(searchQuery,pageNumber)
    suspend fun insert(article: Article)=db.getArticleDao().insertarticle(article)
    suspend fun delete(article: Article)=db.getArticleDao().deleteArticle(article)
    fun getFavouriteNews() = db.getArticleDao().getAllArticle()
}