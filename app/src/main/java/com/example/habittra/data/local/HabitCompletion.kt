package com.example.habittra.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "habit_completions",
    primaryKeys = ["habitId", "date"]   // composite key — one row per habit per day
)
data class HabitCompletion(
    val habitId: Int,
    val date: String,       // "yyyy-MM-dd"
    val isDone: Boolean
)