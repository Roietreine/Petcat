package com.example.petcat.ui.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.petcat.network.repository.SmsRepository

class SmsViewModelProviders(
    val smsRepository: SmsRepository,
    val app: Application
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SmsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SmsViewModel(app,smsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}