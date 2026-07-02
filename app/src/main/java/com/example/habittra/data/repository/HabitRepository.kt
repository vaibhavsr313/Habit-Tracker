package com.example.habittra.data.repository

import com.example.habittra.data.local.Habit
import com.example.habittra.data.local.HabitCompletion
import com.example.habittra.data.local.HabitDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HabitRepository(private val dao: HabitDao) {

    val allHabits: Flow<List<Habit>> = dao.getAllHabits()

    suspend fun addHabit(habit: Habit) = dao.insertHabit(habit)

    suspend fun deleteHabit(habit: Habit) {
        dao.deleteCompletionsForHabit(habit.id)
        dao.deleteHabit(habit)
    }

    suspend fun markCompletion(habitId: Int, date: String, isDone: Boolean) {
        dao.upsertCompletion(HabitCompletion(habitId = habitId, date = date, isDone = isDone))
    }

    fun getCompletionsForDate(date: String): Flow<List<HabitCompletion>> =
        dao.getCompletionsForDate(date)

    fun getDoneDates(habitId: Int): Flow<Set<String>> =
        dao.getDoneDatesForHabit(habitId).map { it.toSet() }

    fun getCompletionsForHabit(habitId: Int): Flow<List<HabitCompletion>> =
        dao.getCompletionsForHabit(habitId)

    fun getCompletionsForDates(dates: List<String>): Flow<List<HabitCompletion>> =
        dao.getCompletionsForDates(dates)
}