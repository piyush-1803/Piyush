package com.example.data

import kotlinx.coroutines.flow.Flow

class TaskRepository(private val database: AppDatabase) {
    private val taskDao = database.taskDao()
    private val userStatsDao = database.userStatsDao()

    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()
    val userStats: Flow<UserStatsEntity?> = userStatsDao.getUserStatsFlow()

    suspend fun insertTask(task: TaskEntity) {
        taskDao.insertTask(task)
    }

    suspend fun deleteTask(task: TaskEntity) {
        taskDao.deleteTask(task)
    }

    suspend fun completeTask(task: TaskEntity) {
        // Calculate XP and Gold based on difficulty
        val xpGain = when (task.difficulty.uppercase()) {
            "EASY" -> 25
            "NORMAL" -> 50
            "HARD" -> 100
            else -> 50
        }
        val goldGain = when (task.difficulty.uppercase()) {
            "EASY" -> 15
            "NORMAL" -> 35
            "HARD" -> 80
            else -> 35
        }

        // Fetch user stats
        var stats = userStatsDao.getUserStats()
        if (stats == null) {
            stats = UserStatsEntity()
        }

        // Calculate new XP and Level
        var newXp = stats.xp + xpGain
        var newLevel = stats.level
        val xpRequired = 500 // flat 500 XP per level

        while (newXp >= xpRequired) {
            newXp -= xpRequired
            newLevel++
        }

        // Calculate Streaks
        val today = System.currentTimeMillis()
        val oneDayMs = 24 * 60 * 60 * 1000L
        val lastActive = stats.lastActiveTimestamp
        
        var newStreak = stats.streak
        if (today - lastActive <= oneDayMs * 1.5) {
            if (today - lastActive > 12 * 60 * 60 * 1000L) {
                newStreak++
            } else if (newStreak == 0) {
                newStreak = 1
            }
        } else {
            // Streak broken or reset
            newStreak = 1
        }

        val updatedStats = stats.copy(
            level = newLevel,
            xp = newXp,
            gold = stats.gold + goldGain,
            streak = newStreak,
            lastActiveTimestamp = today
        )
        userStatsDao.insertOrUpdate(updatedStats)

        // Handle task completion and recurring deadlines
        if (task.isRecurring && task.recurrenceInterval != "NONE") {
            // Move due date to the next recurrence interval
            val nextDueDate = calculateNextDueDate(task.dueDate, task.recurrenceInterval)
            val updatedTask = task.copy(
                dueDate = nextDueDate,
                lastCompletedDate = today,
                isCompleted = false // Keep active for next iteration!
            )
            taskDao.updateTask(updatedTask)
        } else {
            // Standard completion
            val updatedTask = task.copy(isCompleted = true, lastCompletedDate = today)
            taskDao.updateTask(updatedTask)
        }
    }

    suspend fun uncompleteTask(task: TaskEntity) {
        if (!task.isCompleted) return
        val updatedTask = task.copy(isCompleted = false, lastCompletedDate = null)
        taskDao.updateTask(updatedTask)
    }

    suspend fun buyCrystalCoreUpgrade(): Boolean {
        val stats = userStatsDao.getUserStats() ?: return false
        val cost = 150
        if (stats.gold >= cost && stats.crystalTier < 4) {
            val updatedStats = stats.copy(
                gold = stats.gold - cost,
                crystalTier = stats.crystalTier + 1
            )
            userStatsDao.insertOrUpdate(updatedStats)
            return true
        }
        return false
    }

    suspend fun buyCrystalGlowUpgrade(): Boolean {
        val stats = userStatsDao.getUserStats() ?: return false
        val cost = 80
        if (stats.gold >= cost && stats.crystalGlow < 5) {
            val updatedStats = stats.copy(
                gold = stats.gold - cost,
                crystalGlow = stats.crystalGlow + 1
            )
            userStatsDao.insertOrUpdate(updatedStats)
            return true
        }
        return false
    }

    suspend fun buyPedestalUpgrade(): Boolean {
        val stats = userStatsDao.getUserStats() ?: return false
        val cost = 200
        if (stats.gold >= cost && stats.pedestalType < 3) {
            val updatedStats = stats.copy(
                gold = stats.gold - cost,
                pedestalType = stats.pedestalType + 1
            )
            userStatsDao.insertOrUpdate(updatedStats)
            return true
        }
        return false
    }

    suspend fun earnXpAndGoldManual(xp: Int, gold: Int) {
        val stats = userStatsDao.getUserStats() ?: UserStatsEntity()
        var newXp = stats.xp + xp
        var newLevel = stats.level
        val xpRequired = 500
        while (newXp >= xpRequired) {
            newXp -= xpRequired
            newLevel++
        }
        userStatsDao.insertOrUpdate(stats.copy(level = newLevel, xp = newXp, gold = stats.gold + gold))
    }

    private fun calculateNextDueDate(currentDue: Long, interval: String): Long {
        val oneDayMs = 24 * 60 * 60 * 1000L
        return when (interval.uppercase()) {
            "DAILY" -> currentDue + oneDayMs
            "WEEKLY" -> currentDue + (oneDayMs * 7)
            "MONTHLY" -> currentDue + (oneDayMs * 30)
            else -> currentDue + oneDayMs
        }
    }

    suspend fun initStats() {
        val stats = userStatsDao.getUserStats()
        if (stats == null) {
            userStatsDao.insertOrUpdate(UserStatsEntity())
        }
    }
}
