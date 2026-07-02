package com.example.habittra.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.habittra.data.local.Habit
import com.example.habittra.data.repository.HabitRepository
import com.example.habittra.utils.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


data class HomeUiState(
    val habits: List<Habit> = emptyList(),
    val todayCompletions: Map<Int, Boolean> = emptyMap()
)

class HomeViewModel(private val repository: HabitRepository) : ViewModel() {

    // selectedDate drives which day's completions are shown
    private val _selectedDate = MutableStateFlow(DateUtils.todayString())
    val selectedDate: StateFlow<String> = _selectedDate
    private val weekDateStrings = DateUtils.currentWeekDates().map { it.third }

    val uiState: StateFlow<HomeUiState> = _selectedDate.flatMapLatest { date ->
        combine(
            repository.allHabits,
            repository.getCompletionsForDate(date)
        ) { habits, completions ->
            val completionMap = completions.associate { it.habitId to it.isDone }
            HomeUiState(habits = habits, todayCompletions = completionMap)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    fun selectDate(dateStr: String) {
        _selectedDate.value = dateStr
    }
    // Map of "yyyy-MM-dd" → completion fraction (0.0 to 1.0) for the week strip rings
    val weekRates: StateFlow<Map<String, Float>> = combine(
        repository.allHabits,
        repository.getCompletionsForDates(weekDateStrings)
    ) { habits, completions ->
        if (habits.isEmpty()) return@combine emptyMap()
        weekDateStrings.associateWith { date ->
            val doneCount = completions.count { it.date == date && it.isDone }
            doneCount.toFloat() / habits.size.toFloat()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun addHabit(name: String, colorHex: String) {
        viewModelScope.launch {
            repository.addHabit(Habit(name = name, colorHex = colorHex, iconName = "default"))
        }
    }

    fun toggleHabit(habitId: Int, isDone: Boolean) {
        viewModelScope.launch {
            repository.markCompletion(habitId, _selectedDate.value, isDone)
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch { repository.deleteHabit(habit) }
    }

    fun getDoneDates(habitId: Int): Flow<Set<String>> =
        repository.getDoneDates(habitId)

    fun getStreakForHabit(habitId: Int): Flow<Int> =
        repository.getDoneDates(habitId).map { doneDates ->
            calculateStreak(doneDates)
        }

    private fun calculateStreak(doneDates: Set<String>): Int {
        if (doneDates.isEmpty()) return 0
        var streak = 0
        val cal = java.util.Calendar.getInstance()
        val fmt = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())

        // Walk backwards from today — count consecutive done days
        while (true) {
            val dateStr = fmt.format(cal.time)
            if (dateStr in doneDates) {
                streak++
                cal.add(java.util.Calendar.DAY_OF_MONTH, -1)
            } else break
        }
        return streak
    }
}

class HomeViewModelFactory(private val repository: HabitRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = HomeViewModel(repository) as T
}