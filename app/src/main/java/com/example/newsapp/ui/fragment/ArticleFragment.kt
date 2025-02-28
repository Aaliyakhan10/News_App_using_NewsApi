package com.example.newsapp.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.example.newsapp.R
import com.example.newsapp.databinding.FragmentArticleBinding
import com.example.newsapp.models.Article
import com.example.newsapp.ui.NewsActivity
import com.example.newsapp.viewModels.NewsViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.MutableStateFlow

class ArticleFragment : Fragment(R.layout.fragment_article) {

    private lateinit var viewModel: NewsViewModel
    private lateinit var binding: FragmentArticleBinding
    private val args: ArticleFragmentArgs by navArgs()  // Type-safe argument retrieval
    private lateinit var article: Article
    private  var isAddedTOFavourite= MutableStateFlow(false)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentArticleBinding.inflate(inflater, container, false)
        viewModel = (activity as NewsActivity).viewModel
        article = args.article  // Using NavArgs to retrieve the article

        setupWebView()

        setupFabButton()
        getFavouriteNews()
        toCheckFovourite()

        return binding.root
    }



    private fun setupFabButton() {
        binding.favBtn.setOnClickListener {
            viewModel.addToFavourite(article)
            view?.let {
                Snackbar.make(it, "Added to Favourites", Snackbar.LENGTH_SHORT).show()
                isAddedTOFavourite.value=true
            }
        }
    }
    private fun getFavouriteNews(){
        viewModel.getFavouriteNews().observe(viewLifecycleOwner, Observer { saved->
            for(i in saved){
                if(i.Id==article.Id){
                    isAddedTOFavourite.value=true
                    toCheckFovourite()

                }
            }

        })
    }
    private fun toCheckFovourite() {
     if(isAddedTOFavourite.value==true){
         binding.FavAlreadyBtn.visibility=View.VISIBLE
         binding.favBtn.visibility=View.GONE

     }else if(isAddedTOFavourite.value==false){
         binding.FavAlreadyBtn.visibility=View.GONE
         binding.favBtn.visibility=View.VISIBLE

     }

    }


    private fun setupWebView() {
        binding.webView.apply {
            webViewClient = WebViewClient()
            article.url?.let { loadUrl(it) }
        }
    }
}
