package com.example.gpswalktracker.utils

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gpswalktracker.App

typealias ViewModelCreator = (App) -> ViewModel?

class ViewModelFactory(
    private val app: App,
    private val viewModelCreator: ViewModelCreator = { null },
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val viewModel = viewModelCreator(app)
            ?: throw java.lang.IllegalStateException("Unknown view model class")
        return viewModel as T
    }
}

@Suppress("ktInlineFunctionLeaksAnonymous")
inline fun <reified VM : ViewModel> Fragment.viewModelCreator(noinline creator: ViewModelCreator): Lazy<VM> {
    return viewModels {
        ViewModelFactory(
            requireContext().applicationContext as App,
            creator
        )
    }
}