package com.example.newsapp.ui.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.R
import com.example.newsapp.adapters.NewsAdapter
import com.example.newsapp.databinding.FragmentSearchBinding
import com.example.newsapp.ui.NewsActivity
import com.example.newsapp.utils.Constant
import com.example.newsapp.utils.Resources
import com.example.newsapp.viewModels.NewsViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchFragment : Fragment(R.layout.fragment_search) {
    private lateinit var viewModel: NewsViewModel
    private lateinit var adapter: NewsAdapter
    private lateinit var binding: FragmentSearchBinding
    private lateinit var cardView: CardView
    private lateinit var errorText: TextView
    private lateinit var errorBtn: Button

    private var isError = false
    private var isLoading = false
    private var isScroll = false
    private var isLastPage = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentSearchBinding.bind(view)
        viewModel = (activity as NewsActivity).viewModel


        cardView = binding.includeLayoutSearch.cardViewError
        errorBtn = binding.includeLayoutSearch.retryBtn
        errorText =binding.includeLayoutSearch.errorTxt
        // Set up RecyclerView
        setSearchRecycler()

        // Handle item click in the adapter
        adapter.setItemClickListener {
            val bundle = Bundle().apply {
                putSerializable("article", it)
            }
            findNavController().navigate(R.id.action_searchFragment_to_articleFragment, bundle)
        }

        // Retry logic for error message
        errorBtn.setOnClickListener {
            val query = binding.editTextSearch.text.toString()
            if (query.isNotEmpty()) {
                viewModel.getSearchNews(query)
            } else {
                hideErrorMessage()
            }
        }

        // Search functionality with debounce
        var job: Job? = null
        binding.editTextSearch.addTextChangedListener { editable ->
            job?.cancel()
            job = MainScope().launch {
                delay(Constant.SEARCH_TIME_DELAY)
                editable?.let {
                    if (it.isNotEmpty()) {
                        viewModel.getSearchNews(it.toString())
                    }
                }
            }
        }

        // Observe the search results
        viewModel.searchNews.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resources.Success<*> -> {
                    hideProgressBar()
                    hideErrorMessage()
                    response.data?.let { newsResponse ->
                        adapter.differ.submitList(newsResponse.articles.toList())
                        val totalPages = newsResponse.totalResults / Constant.QUERY_PAGE_SIZE + 2
                        isLastPage = viewModel.searchNewsPage == totalPages
                        if (isLastPage) {
                            binding.recViewSearch.setPadding(0, 0, 0, 0)
                        }
                    }
                }
                is Resources.Error<*> -> {
                    hideProgressBar()
                    showErrorMessage(response.message ?: "Sorry, an error occurred")
                }
                is Resources.Loading<*> -> {
                    showProgressBar()
                }
            }
        })


        binding.includeLayoutSearch.retryBtn.setOnClickListener {
            val query = binding.editTextSearch.text.toString()
            if (query.isNotEmpty()) {
                viewModel.getSearchNews(query)
            } else {
                hideErrorMessage()
            }
        }
    }

    private fun hideProgressBar() {
        binding.progressBarSearch.visibility = View.INVISIBLE
        isLoading = false
    }

    private fun showProgressBar() {
        binding.progressBarSearch.visibility = View.VISIBLE
        isLoading = true
    }

    private fun hideErrorMessage() {
        cardView.visibility = View.INVISIBLE
        isError = false
    }

    private fun showErrorMessage(message: String) {
        cardView.visibility = View.VISIBLE
        errorText.text = message
        isError = true
    }

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val isNoError = !isError
            val isNotLoadingAndNotLastPage = !isLoading && !isLastPage
            val isAtLastItem = (firstVisibleItemPosition + visibleCount) >= totalItemCount
            val isNotBeginning = firstVisibleItemPosition >= 0
            val isTotalMoreThanVisible = totalItemCount >= Constant.QUERY_PAGE_SIZE

            val shouldPaginate = isNoError && isNotLoadingAndNotLastPage && isAtLastItem && isNotBeginning && isTotalMoreThanVisible && isScroll
            if (shouldPaginate) {
                viewModel.getSearchNews(binding.editTextSearch.text.toString())
                isScroll = false
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                isScroll = true
            }
        }
    }

    private fun setSearchRecycler() {
        Log.d("SearchFragment", "Setting up RecyclerView adapter")
        adapter = NewsAdapter()
        binding.recViewSearch.apply {
            adapter = this@SearchFragment.adapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(scrollListener)
        }
        Log.d("SearchFragment", "RecyclerView adapter set up successfully")
    }
}
