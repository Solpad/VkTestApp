package com.example.vktestapp.screen.main

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.vktestapp.repo.RecordRepo

class MainViewModelProviderFactory(
    val recordRepository: RecordRepo,
    val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(recordRepository,application) as T
    }
}