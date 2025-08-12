package com.nomore.example.simplepetonscreen.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nomore.example.simplepetonscreen.repository.PetRepository
import com.nomore.example.simplepetonscreen.repository.PetRepositoryImpl
import com.nomore.example.simplepetonscreen.viewmodel.PetViewModel

//demo
class PetViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PetViewModel::class.java)) {
            val repository: PetRepository = PetRepositoryImpl(context.applicationContext)
            return PetViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

//Dependency Injection (Hilt/Koin)