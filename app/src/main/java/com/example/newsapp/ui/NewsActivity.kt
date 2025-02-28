package com.example.newsapp.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.newsapp.R
import com.example.newsapp.databinding.ActivityNewsBinding
import com.example.newsapp.repository.NewsRepository
import com.example.newsapp.roomDb.ArticleDatabase
import com.example.newsapp.ui.fragment.HeadlinesFragment
import com.example.newsapp.ui.fragment.SavedFragment
import com.example.newsapp.ui.fragment.SearchFragment
import com.example.newsapp.viewModels.NewsViewModel
import com.example.newsapp.viewModels.NewsViewModelFactory
import com.google.android.material.bottomnavigation.BottomNavigationView

class NewsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNewsBinding
    lateinit var viewModel: NewsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNewsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val newsRepository = NewsRepository(ArticleDatabase(this))
        val viewModelFactory = NewsViewModelFactory(application, newsRepository)
        viewModel = ViewModelProvider(this, viewModelFactory).get(NewsViewModel::class.java)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.newsNavHostFragment) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNavigationViewMain.setupWithNavController(navController)
    }
}

