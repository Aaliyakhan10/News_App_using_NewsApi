package com.example.newsapp.api


import com.example.newsapp.models.NewsResponse
import com.example.newsapp.utils.Constant.Companion.API_ID
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.Locale.IsoCountryCode

interface NewsApi {

        @GET("v2/top-headlines")
        suspend fun getHeadlines(
            @Query("country") countryCode: String,
            @Query("page") pageNumber: Int,
            @Query("apiKey") apiKey: String = API_ID
        ): Response<NewsResponse>


    @GET("v2/everything")
    suspend fun searchForNews(
        @Query("q")
        searchQuery:String,
        @Query("page")
        pageNumber: Int=1,
        @Query("apikey")
        apikey:String=API_ID
    ):Response<NewsResponse>

}