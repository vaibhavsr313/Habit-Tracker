package com.example.habittra.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val colorHex: String,   // e.g. "#FFD700" for yellow
    val iconName: String    // e.g. "gym", "skincare"
)