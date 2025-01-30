package com.example.swipeapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.swipeapp.ui.ProductListingFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            val fragmentManager = supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.fragment_container, ProductListingFragment())
            fragmentTransaction.commit()
        }
    }
}