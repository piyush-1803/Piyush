package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_stats")
data class UserStatsEntity(
    @PrimaryKey val id: Int = 1, // Single row for user stats
    val level: Int = 1,
    val xp: Int = 0,
    val streak: Int = 0,
    val gold: Int = 0,
    val lastActiveTimestamp: Long = System.currentTimeMillis(),
    val crystalTier: Int = 1, // 1 = Quartz Core, 2 = Amethyst Core, 3 = Emerald Core, 4 = Radiant Star Core
    val crystalGlow: Int = 1,  // Glow level (1 to 5)
    val pedestalType: Int = 1  // 1 = Basic Slate, 2 = Obsidian Column, 3 = Celestial Altar
)
