package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.data.api.AuraCoachService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = TaskRepository(database)
    private val aiService = AuraCoachService()

    val tasks: StateFlow<List<TaskEntity>> = repository.allTasks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val userStats: StateFlow<UserStatsEntity?> = repository.userStats
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    private val _aiResponse = MutableStateFlow<String>("")
    val aiResponse: StateFlow<String> = _aiResponse.asStateFlow()

    private val _currentTab = MutableStateFlow("DASHBOARD") // "DASHBOARD", "TASKS", "AURA_AI", "UPGRADES"
    val currentTab: StateFlow<String> = _currentTab.asStateFlow()

    private val _shopStatusMessage = MutableStateFlow<String>("")
    val shopStatusMessage: StateFlow<String> = _shopStatusMessage.asStateFlow()

    private var defaultTasksSeeded = false

    init {
        viewModelScope.launch {
            repository.initStats()
            tasks.collect { currentTasks ->
                if (currentTasks.isEmpty() && !defaultTasksSeeded) {
                    defaultTasksSeeded = true
                    seedDefaultTasks()
                }
            }
        }
    }

    fun setTab(tab: String) {
        _currentTab.value = tab
    }

    fun clearShopStatus() {
        _shopStatusMessage.value = ""
    }

    private fun seedDefaultTasks() {
        viewModelScope.launch {
            val today = System.currentTimeMillis()
            repository.insertTask(
                TaskEntity(
                    title = "Daily Focus Aura Setup",
                    description = "Clear focus space, check priority grids, and perform 2 mins deep breathing to align focus.",
                    priority = "HIGH",
                    dueDate = today,
                    isRecurring = true,
                    recurrenceInterval = "DAILY",
                    difficulty = "EASY",
                    category = "Health"
                )
            )
            repository.insertTask(
                TaskEntity(
                    title = "Weekly Tech Architecture Sprint",
                    description = "Analyze engineering deliverables and structure weekly code milestones.",
                    priority = "MEDIUM",
                    dueDate = today + (24 * 60 * 60 * 1000L),
                    isRecurring = true,
                    recurrenceInterval = "WEEKLY",
                    difficulty = "NORMAL",
                    category = "Work"
                )
            )
            repository.insertTask(
                TaskEntity(
                    title = "Beautify Aura Dashboard UI",
                    description = "Implement strict Material 3 guidelines and customize adaptive elements inside the main composition.",
                    priority = "HIGH",
                    dueDate = today,
                    isRecurring = false,
                    recurrenceInterval = "NONE",
                    difficulty = "HARD",
                    category = "Study"
                )
            )
        }
    }

    fun addTask(
        title: String,
        description: String,
        priority: String,
        dueDate: Long,
        isRecurring: Boolean,
        recurrenceInterval: String,
        difficulty: String,
        category: String
    ) {
        viewModelScope.launch {
            repository.insertTask(
                TaskEntity(
                    title = title,
                    description = description,
                    priority = priority,
                    dueDate = dueDate,
                    isRecurring = isRecurring,
                    recurrenceInterval = recurrenceInterval,
                    difficulty = difficulty,
                    category = category
                )
            )
        }
    }

    fun completeTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.completeTask(task)
        }
    }

    fun uncompleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.uncompleteTask(task)
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    fun requestAuraPrioritization() {
        viewModelScope.launch {
            _isAiLoading.value = true
            _aiResponse.value = "Calibrating cybernetic flow lines..."
            val activeTasks = tasks.value.filter { !it.isCompleted }
            val priorityResult = aiService.getDailyFlowPriorities(activeTasks)
            _aiResponse.value = priorityResult
            _isAiLoading.value = false
        }
    }

    fun requestAuraVibeCheck() {
        viewModelScope.launch {
            _isAiLoading.value = true
            _aiResponse.value = "Gauging creative and physiological elements..."
            val stats = userStats.value ?: UserStatsEntity()
            val activeTasks = tasks.value.filter { !it.isCompleted }
            val response = aiService.getVibeCheck(stats, activeTasks)
            _aiResponse.value = response
            _isAiLoading.value = false
        }
    }

    fun requestTaskBreakdown(task: TaskEntity) {
        viewModelScope.launch {
            _isAiLoading.value = true
            _aiResponse.value = "Splitting \"${task.title}\" into manageable, atomic sessions..."
            _currentTab.value = "AURA_AI"
            val response = aiService.breakdownTask(task.title, task.description)
            _aiResponse.value = response
            _isAiLoading.value = false
        }
    }

    fun upgradeCore() {
        viewModelScope.launch {
            val success = repository.buyCrystalCoreUpgrade()
            if (success) {
                _shopStatusMessage.value = "Success! Aura Crystal upgraded to next Tier."
            } else {
                _shopStatusMessage.value = "Insufficient Gold or Core is already at MAX Level (Tier 4)!"
            }
        }
    }

    fun upgradeGlow() {
        viewModelScope.launch {
            val success = repository.buyCrystalGlowUpgrade()
            if (success) {
                _shopStatusMessage.value = "Success! Aura Glow intensity enhanced!"
            } else {
                _shopStatusMessage.value = "Insufficient Gold or Glow is already at MAX Level!"
            }
        }
    }

    fun upgradePedestal() {
        viewModelScope.launch {
            val success = repository.buyPedestalUpgrade()
            if (success) {
                _shopStatusMessage.value = "Success! Focus Pedestal upgraded!"
            } else {
                _shopStatusMessage.value = "Insufficient Gold or Pedestal is already at MAX Level!"
            }
        }
    }
}
