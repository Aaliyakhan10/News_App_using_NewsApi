package com.example.newsapp.viewModels

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.newsapp.models.Article
import com.example.newsapp.models.NewsResponse
import com.example.newsapp.repository.NewsRepository
import com.example.newsapp.utils.Resources
import kotlinx.coroutines.launch
import okio.IOException
import retrofit2.Response

class NewsViewModel(app: Application, val newsRepository: NewsRepository) : AndroidViewModel(app) {

    val headlines: MutableLiveData<Resources<NewsResponse>> = MutableLiveData()
    var headlinePage = 1
    var headlineResponse: NewsResponse? = null
    val searchNews: MutableLiveData<Resources<NewsResponse>> = MutableLiveData()
    var searchNewsPage = 1
    var searchResponse: NewsResponse? = null
    var newSearchQuery: String? = null
    var oldSearchQuery: String? = null
    init {
        getHeadlines("us")
    }
    fun getHeadlines(countryCode: String) = viewModelScope.launch {
        headlines.postValue(Resources.Loading())
        try {
            val response = newsRepository.getHeadlines(countryCode, headlinePage)
            if (response.isSuccessful) {
                headlines.postValue(handleHeadlinesResponse(response))
                Log.d("NewsViewModel", "Headlines fetched successfully")
            } else {
                val errorMsg = "Error Code: ${response.code()} - ${response.message()}"
                headlines.postValue(Resources.Error(errorMsg))
                Log.e("NewsViewModel", errorMsg)
            }
        } catch (e: Exception) {
            val errorMsg = "Exception: ${e.message}"
            headlines.postValue(Resources.Error(errorMsg))
            Log.e("NewsViewModel", errorMsg, e)
        }
    }



    fun getSearchNews(searchQuery: String) = viewModelScope.launch {
        searchInternet(searchQuery)
    }

    private fun handleHeadlinesResponse(response: Response<NewsResponse>): Resources<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                if (headlineResponse == null) {
                    headlineResponse = resultResponse
                } else {
                    val oldArticle = headlineResponse!!.articles
                    val newArticle = resultResponse.articles
                    oldArticle.addAll(newArticle)
                }
                headlinePage++  // Only increment after successful response
                return Resources.Success(headlineResponse ?: resultResponse)
            }
        }
        return Resources.Error(response.message())
    }

    private fun handleSearchResponse(response: Response<NewsResponse>): Resources<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                if (searchResponse == null || newSearchQuery != oldSearchQuery) {
                    searchNewsPage++
                    oldSearchQuery = newSearchQuery
                    searchResponse = resultResponse
                } else {
                    searchNewsPage++
                    val oldArticle = searchResponse!!.articles
                    val newArticle = resultResponse.articles
                    oldArticle.addAll(newArticle)
                }
                return Resources.Success(searchResponse ?: resultResponse)
            }
        }
        return Resources.Error(response.message())
    }

    fun addToFavourite(article: Article) = viewModelScope.launch {
        newsRepository.insert(article)
    }

    fun getFavouriteNews() = newsRepository.getFavouriteNews()

    fun removefavouriteNews(article: Article) = viewModelScope.launch {
        newsRepository.delete(article)
    }

    fun checkInternetConnection(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)?.run {
            when {
                hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } ?: false
    }

    private suspend fun headlineInternet(countryCode: String) {
        headlines.postValue(Resources.Loading())
        try {
            if (checkInternetConnection(this.getApplication())) {
                val response = newsRepository.getHeadlines(countryCode, headlinePage)
                headlines.postValue(handleHeadlinesResponse(response))
            } else {
                headlines.postValue(Resources.Error("No internet"))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> headlines.postValue(Resources.Error("Unable to connect"))
                else -> headlines.postValue(Resources.Error("No signal"))
            }
        }
    }

    private suspend fun searchInternet(searchQuery: String) {
        newSearchQuery = searchQuery
        searchNews.postValue(Resources.Loading())
        try {
            if (checkInternetConnection(this.getApplication())) {
                val response = newsRepository.searchArticle(newSearchQuery!!, searchNewsPage)
                searchNews.postValue(handleSearchResponse(response))
            } else {
                searchNews.postValue(Resources.Error("No internet"))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> searchNews.postValue(Resources.Error("Unable to connect"))
                else -> searchNews.postValue(Resources.Error("No signal"))
            }
        }
    }
}
