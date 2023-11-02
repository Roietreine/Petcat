package com.example.petcat.ui

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import com.example.petcat.databinding.ActivityWebBinding

class WebActivity : AppCompatActivity() {
    private lateinit var binding : ActivityWebBinding

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWebBinding.inflate(layoutInflater)
        val webSettings: WebSettings = binding.wvContent.settings
        webSettings.javaScriptEnabled = true
        binding.wvContent.settings.cacheMode = WebSettings.LOAD_DEFAULT
        binding.wvContent.settings.domStorageEnabled = true
        binding.wvContent.webChromeClient = WebChromeClient()
        binding.wvContent.loadUrl("https://developer.android.com/docs")

        setContentView(binding.root)


    }
}