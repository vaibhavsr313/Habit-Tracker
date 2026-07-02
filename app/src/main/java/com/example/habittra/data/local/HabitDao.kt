package com.example.habittra.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Insert
    suspend fun insertHabit(habit: Habit): Long

    @Query("SELECT * FROM habits")
    fun getAllHabits(): Flow<List<Habit>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCompletion(completion: HabitCompletion)

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId")
    fun getCompletionsForHabit(habitId: Int): Flow<List<HabitCompletion>>

    @Query("SELECT * FROM habit_completions WHERE date = :date")
    fun getCompletionsForDate(date: String): Flow<List<HabitCompletion>>

    // NEW — for streak and heatmap, only done=true rows
    @Query("SELECT date FROM habit_completions WHERE habitId = :habitId AND isDone = 1")
    fun getDoneDatesForHabit(habitId: Int): Flow<List<String>>

    // NEW — for delete habit
    @Delete
    suspend fun deleteHabit(habit: Habit)

    @Query("DELETE FROM habit_completions WHERE habitId = :habitId")
    suspend fun deleteCompletionsForHabit(habitId: Int)

    @Query("SELECT * FROM habit_completions WHERE date IN (:dates) AND isDone = 1")
    fun getCompletionsForDates(dates: List<String>): Flow<List<HabitCompletion>>

}