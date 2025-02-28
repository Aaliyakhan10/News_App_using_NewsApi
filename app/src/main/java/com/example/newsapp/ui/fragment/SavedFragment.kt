package com.example.newsapp.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.R
import com.example.newsapp.adapters.NewsAdapter
import com.example.newsapp.databinding.FragmentSavedBinding
import com.example.newsapp.ui.NewsActivity
import com.example.newsapp.viewModels.NewsViewModel
import com.google.android.material.snackbar.Snackbar

class SavedFragment : Fragment(R.layout.fragment_saved) {

    private lateinit var viewModel: NewsViewModel
    private lateinit var adapter: NewsAdapter
    private lateinit var binding: FragmentSavedBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSavedBinding.inflate(inflater, container, false)
        viewModel = (activity as NewsActivity).viewModel

        setUpSavedRecycler()

        // Handle item click
        adapter.setItemClickListener {
            val bundle = Bundle().apply {
                putSerializable("article", it)
            }
            findNavController().navigate(R.id.action_savedFragment_to_articleFragment, bundle)
        }

        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder
            ): Boolean = true

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val article = adapter.differ.currentList[position]
                viewModel.removefavouriteNews(article)
                view?.let {
                    Snackbar.make(it, "Removed From favorites", Snackbar.LENGTH_LONG).apply {
                        setAction("Undo") {
                            viewModel.addToFavourite(article)
                        }
                    }.show()
                }
            }
        }

        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(binding.recyclerViewSaved)

        // Observe favorite news
        viewModel.getFavouriteNews().observe(viewLifecycleOwner, Observer { articles ->
            adapter.differ.submitList(articles)
        })

        return binding.root
    }

    private fun setUpSavedRecycler() {
        adapter = NewsAdapter()
        binding.recyclerViewSaved.apply {
            adapter = this@SavedFragment.adapter
            layoutManager = LinearLayoutManager(activity)
        }
    }
}
