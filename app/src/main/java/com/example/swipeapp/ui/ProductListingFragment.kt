package com.example.swipeapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.swipeapp.R
import com.example.swipeapp.adapter.ProductAdapter
import com.example.swipeapp.api.ProductService
import com.example.swipeapp.model.Product
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProductListingFragment : Fragment() {

    private lateinit var productAdapter: ProductAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var progressBar: ProgressBar
    private lateinit var addProductFab: FloatingActionButton
    private var allProducts = listOf<Product>()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://app.getswipe.in/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val productService = retrofit.create(ProductService::class.java)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_product_listing, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews(view)
        setupRecyclerView()
        setupSearchView()
        setupAddProductButton()
        loadProducts()
    }

    private fun setupViews(view: View) {
        recyclerView = view.findViewById(R.id.productsRecyclerView)
        searchView = view.findViewById(R.id.searchView)
        progressBar = view.findViewById(R.id.progressBar)
        addProductFab = view.findViewById(R.id.addProductFab)
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter()
        recyclerView.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = productAdapter
        }
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterProducts(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterProducts(newText)
                return true
            }
        })
    }

    private fun setupAddProductButton() {
        addProductFab.setOnClickListener {
            // Navigate to Add Product screen
            // TODO: Implement navigation to Add Product screen
            Toast.makeText(context, "Add Product clicked", Toast.LENGTH_SHORT).show()
        }
    }

    private fun filterProducts(query: String?) {
        if (query.isNullOrBlank()) {
            productAdapter.submitList(allProducts)
        } else {
            val filteredList = allProducts.filter {
                it.product_name.contains(query, ignoreCase = true) ||
                it.product_type.contains(query, ignoreCase = true)
            }
            productAdapter.submitList(filteredList)
        }
    }

    private fun loadProducts() {
        lifecycleScope.launch {
            try {
                progressBar.visibility = View.VISIBLE
                val response = withContext(Dispatchers.IO) {
                    productService.getProducts()
                }
                
                if (response.isSuccessful) {
                    allProducts = response.body() ?: emptyList()
                    productAdapter.submitList(allProducts)
                } else {
                    Toast.makeText(
                        context,
                        "Error: ${response.message()}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }
} 