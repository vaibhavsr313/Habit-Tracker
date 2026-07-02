package com.example.habittra.ui.addHabit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.habittra.data.local.Habit
import com.example.habittra.data.repository.HabitRepository
import kotlinx.coroutines.launch

class AddHabitViewModel(private val repository: HabitRepository) : ViewModel() {
    fun addHabit(name: String, colorHex: String) {
        viewModelScope.launch {
            repository.addHabit(Habit(name = name, colorHex = colorHex, iconName = "default"))
        }
    }
}

class AddHabitViewModelFactory(private val repository: HabitRepository) : ViewModelProvider.Factory
{
    override fun <T : ViewModel> create(modelClass: Class<T>): T = AddHabitViewModel(repository) as T
}