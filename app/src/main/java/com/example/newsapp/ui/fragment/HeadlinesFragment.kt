package com.example.newsapp.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.R
import com.example.newsapp.adapters.NewsAdapter
import com.example.newsapp.databinding.FragmentHeadlinesBinding
import com.example.newsapp.utils.Resources
import com.example.newsapp.viewModels.NewsViewModel
import com.example.newsapp.ui.NewsActivity
import com.example.newsapp.utils.Constant

class HeadlinesFragment : Fragment(R.layout.fragment_headlines) {

    private lateinit var viewModel: NewsViewModel
    private lateinit var adapter: NewsAdapter
    private lateinit var binding: FragmentHeadlinesBinding
    private var isLoading = false
    private var isError = false
    private var isScroll = false
    private var isLastPage = false
    private lateinit var cardView: CardView
    private lateinit var errorText: TextView
    private lateinit var errorBtn: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHeadlinesBinding.bind(view)
        Log.d("HeadlinesFragment", "Binding initialized: $binding")

        viewModel = (activity as NewsActivity).viewModel
        Log.d("HeadlinesFragment", "ViewModel initialized")

        cardView = binding.includeLayoutHeadlines.cardViewError
        errorBtn = binding.includeLayoutHeadlines.retryBtn
        errorText = binding.includeLayoutHeadlines.errorTxt
        setUpHeadlineRecycler()

        Log.d("HeadlinesFragment", "function call")

        adapter.setItemClickListener { it ->
            val bundle = Bundle().apply {
                putSerializable("article", it)
            }
            findNavController().navigate(R.id.action_headlinesFragment_to_articleFragment, bundle)
        }

        viewModel.headlines.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resources.Success<*> -> {
                    hideProgressBar()
                    hideErrorMessage()
                    response.data?.let { newsResponse ->
                        adapter.differ.submitList(newsResponse.articles.toList())
                        val totalPages = newsResponse.totalResults / Constant.QUERY_PAGE_SIZE + 2
                        Log.d("HeadlinesFragment", "Data fetched successfully")
                        isLastPage = viewModel.headlinePage == totalPages
                        if (isLastPage) {
                            binding.recViewHeadlines.setPadding(0, 0, 0, 0)
                        }
                    }
                }
                is Resources.Error<*> -> {
                    Log.e("HeadlinesFragment", "Error fetching data: ${response.message}")
                    hideProgressBar()
                    Toast.makeText(activity,"Sorry error",Toast.LENGTH_SHORT).show()
                    showErrorMessage(response.message ?: "Sorry, an error occurred")
                }
                is Resources.Loading<*> -> {
                    Log.d("HeadlinesFragment", "Loading data")
                    showProgressBar()
                }
            }
        })




        binding.includeLayoutHeadlines.retryBtn.setOnClickListener {
            viewModel.getHeadlines("us")
        }
    }




    private fun hideProgressBar() {
        binding.progressBarHead.visibility = View.INVISIBLE
        isLoading = false
    }

    private fun showProgressBar() {
        binding.progressBarHead.visibility = View.VISIBLE
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
                viewModel.getHeadlines("us")
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


    private fun setUpHeadlineRecycler() {
        adapter = NewsAdapter()
        binding.recViewHeadlines.apply {
            adapter = this@HeadlinesFragment.adapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(scrollListener)
        }
    }
}
