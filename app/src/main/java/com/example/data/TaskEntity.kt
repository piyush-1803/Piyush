package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val priority: String, // "HIGH", "MEDIUM", "LOW"
    val isCompleted: Boolean = false,
    val dueDate: Long, // timestamp
    val isRecurring: Boolean = false,
    val recurrenceInterval: String = "NONE", // "DAILY", "WEEKLY", "MONTHLY", "NONE"
    val lastCompletedDate: Long? = null,
    val difficulty: String = "NORMAL", // "EASY", "NORMAL", "HARD"
    val category: String = "General" // "Work", "Personal", "Health", "Study", "Fitness"
)
