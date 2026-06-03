package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import com.example.data.TaskEntity
import com.example.data.UserStatsEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuraCoachService {

    private val apiKey: String = BuildConfig.GEMINI_API_KEY

    fun isApiKeyConfigured(): Boolean {
        return apiKey.isNotEmpty() && apiKey != "MY_GEMINI_API_KEY" && apiKey != "placeholder"
    }

    suspend fun getDailyFlowPriorities(tasks: List<TaskEntity>): String = withContext(Dispatchers.IO) {
        if (tasks.isEmpty()) {
            return@withContext "You have no active tasks in your aura grid! Add a task to analyze your daily flow priorities."
        }

        if (!isApiKeyConfigured()) {
            return@withContext "🔮 [AURA AI SIMULATED FLOW]\n\n" +
                    "1. Execute High-Priority items first during your peak morning aura window:\n" +
                    "   • Activity: \"${tasks.firstOrNull { it.priority == "HIGH" }?.title ?: tasks.first().title}\"\n\n" +
                    "2. Conserve your cognitive battery. Tackle smaller tasks during normal focus lulls in the afternoon.\n\n" +
                    "⚠️ To unlock real Gemini AI Coach guidance, set your GEMINI_API_KEY in the Secrets panel!"
        }

        val taskListStr = tasks.joinToString("\n") { task ->
            "- [Title: ${task.title}] [Priority: ${task.priority}] [Difficulty: ${task.difficulty}] [Category: ${task.category}]"
        }

        val prompt = """
            You are Aura Coach, an elite productivity strategist and gamified zen mentor.
            Analyze these user tasks and synthesize a focused, personalized "Aura Flow Path" (execution order).
            Select the top 2 tasks they should execute first to maximize positive flow, with a 1-sentence aesthetic reason for each.
            Keep the tone supportive, modern, gamified, and clean. No bullet list dumps or markdown tables.
            Use a focus-centered dark mode theme (phrases like "peak focus", "luminous energy", "focus reserve", "zen zone").
            Keep the entire response under 120 words.
            
            Tasks:
            $taskListStr
        """.trimIndent()

        callGemini(prompt)
    }

    suspend fun getVibeCheck(stats: UserStatsEntity, activeTasks: List<TaskEntity>): String = withContext(Dispatchers.IO) {
        if (!isApiKeyConfigured()) {
            val element = when (stats.streak % 3) {
                0 -> "Focus Fire 🔥 (Highly Driven)"
                1 -> "Zen Water 💧 (Fluid & Unshakable)"
                else -> "Glow Crystal 💎 (Resilient & Radiant)"
            }
            return@withContext "🔮 [AURA AI SIMULATED VIBE]\n\n" +
                    "• Current Element: $element\n" +
                    "• Level ${stats.level} | Current Streak: ${stats.streak} Days\n" +
                    "• Outlook: Your focus grid is stable. Establish momentum by checking off an Easy task first!\n\n" +
                    "⚠️ To activate Gemini AI Horoscopes, configure your GEMINI_API_KEY in the Secrets panel."
        }

        val activeSummary = if (activeTasks.isEmpty()) "All tasks complete!" else "${activeTasks.size} tasks pending."
        val prompt = """
            You are Aura Coach, an elite productivity strategist and zen mentor.
            The user wants their daily Focus Vibe Check based on these stats:
            - Level: ${stats.level}
            - Streak: ${stats.streak} Days
            - Crystals/Gold: ${stats.gold}
            - Tasks Left: $activeSummary

            Draft a brief, high-contrast, atmospheric "Aura Horoscope" describing their focus element today (e.g., Luminous Plasma, Steady Obsidian, Jade Spark).
            Detail:
            1. Their focus element today.
            2. A 2-sentence prophecy on how their Level ${stats.level} and Streak ${stats.streak} can unleash peak focus flow.
            Tone: Gamified, encouraging, elite, clean. Keep it under 100 words. No excessive bold tags.
        """.trimIndent()

        callGemini(prompt)
    }

    suspend fun breakdownTask(taskTitle: String, taskDescription: String): String = withContext(Dispatchers.IO) {
        if (!isApiKeyConfigured()) {
            return@withContext "🔮 [AURA AI SIMULATED BREAKDOWN]\n\n" +
                    "Action Steps for \"$taskTitle\":\n" +
                    "1. 🏁 Prep & Shield (Turn off notifications; clear focus space - 5 mins)\n" +
                    "2. 💡 Skeleton Build (Outline the skeleton parameters - 15 mins)\n" +
                    "3. ⚡ Pomodoro Sprint (Commit to 25 mins of solid momentum - 25 mins)\n" +
                    "4. 🏆 Review & Claim (Polish details, mark complete, and claim your XP! - 10 mins)" +
                    "\n\nTo activate genuine Gemini breakdowns, update your GEMINI_API_KEY in the Secrets panel."
        }

        val prompt = """
            You are Aura Coach, an elite productivity strategist.
            The user is struggling to get started on this task:
            Title: $taskTitle
            Description: $taskDescription

            Break this task down into 3-4 granular, highly actionable steps. Recommend duration in minutes for each step.
            Provide only the steps. Keep it structured, highly specific, inspiring, and concise. No conversational intro or padding.
            Response must be under 120 words.
        """.trimIndent()

        callGemini(prompt)
    }

    private suspend fun callGemini(prompt: String): String {
        try {
            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent(parts = listOf(GeminiPart(text = prompt)))
                ),
                generationConfig = GeminiGenerationConfig(
                    temperature = 0.7f,
                    maxOutputTokens = 300
                ),
                systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = "You are Aura Coach. Speak directly to the developer/player. Maintain an inspiring, gamified, elegant dark mode vibe. Be highly respectful of their focus and screen space. No markdown tables or overly long formats.")))
            )
            val response = RetrofitClient.service.generateContent(apiKey, request)
            return response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "Failed to generate guidance from Aura AI."
        } catch (e: Exception) {
            Log.e("AuraCoachService", "Gemini API error", e)
            return "Failed to establish aura connection: ${e.localizedMessage ?: "Unknown network interruption."}"
        }
    }
}
