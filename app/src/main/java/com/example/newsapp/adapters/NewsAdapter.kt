package com.example.newsapp.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.newsapp.R
import com.example.newsapp.databinding.ItemNewsViewBinding
import com.example.newsapp.models.Article

class NewsAdapter : RecyclerView.Adapter<NewsAdapter.ViewHolder>() {

    private lateinit var binding: ItemNewsViewBinding

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private val differCallback = object : DiffUtil.ItemCallback<Article>() {
        override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem.url == newItem.url
        }

        override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = ItemNewsViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding.root)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    private var onItemsClick: ((Article) -> Unit)? = null

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val article = differ.currentList[position]
        Log.d("NewsAdapter", "Binding article: ${article.title} at position: $position")

        binding.articleTitle.text = article.title
        binding.articleDateTime.text = article.publishedAt
        binding.articleDescription.text = article.description
        binding.articleSource.text = article.source.name

        Glide.with(holder.itemView.context)
            .load(article.urlToImage)

            .into(binding.articleImage)

        holder.itemView.setOnClickListener {
            onItemsClick?.invoke(article)
        }
    }

    fun setItemClickListener(listener: (Article) -> Unit) {
        onItemsClick = listener
        Log.d("NewsAdapter", "Listener set: $onItemsClick")
    }
}
