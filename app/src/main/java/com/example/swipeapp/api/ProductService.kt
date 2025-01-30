package com.example.swipeapp.api

import com.example.swipeapp.model.Product
import retrofit2.Response
import retrofit2.http.GET

interface ProductService {
    @GET("api/public/get")
    suspend fun getProducts(): Response<List<Product>>
} 