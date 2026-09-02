package com.example.data.repository

import com.example.data.remote.GeminiClient

class AiPlannerRepository {

    suspend fun planDay(userPrompt: String, tasksContext: String, userGoals: String): String {
        val systemInstruction = """
            You are 'Pia', an intelligent, helpful personal assistant for Priyanka.
            Create a practical, realistic, and well-structured daily schedule based on her tasks and available hours.
            Be concise, clear, and direct.
            Do not include over-the-top motivational speeches or flowery slogans.
            Format cleanly with timestamps and tasks.
        """.trimIndent()

        val fullPrompt = """
            Priyanka's request: "$userPrompt"
            Pending Tasks: $tasksContext
            Goals & Focus: $userGoals

            Please create a structured, realistic daily schedule with clear timestamps.
        """.trimIndent()

        val apiResult = GeminiClient.generateText(fullPrompt, systemInstruction)
        return if (apiResult.isSuccess) {
            apiResult.getOrNull() ?: fallbackPlan(userPrompt, tasksContext)
        } else {
            fallbackPlan(userPrompt, tasksContext)
        }
    }

    suspend fun chatWithPia(userMessage: String, currentTasks: String, userGoals: String): String {
        val systemInstruction = """
            You are 'Pia', a natural, intelligent, and friendly personal assistant for Priyanka in her app 'Priyanka's Little Space'.

            CORE RULES:
            1. Respond ONLY to what the user actually says or asks.
            2. DO NOT assume the user's emotional state.
            3. DO NOT add unnecessary motivational speeches, quotes, or forced positivity (never say "positive energy", "happy glow", "keep your rhythm", etc., unless specifically asked for motivation).
            4. DO NOT turn every conversation into productivity advice or life coaching.
            5. DO NOT give unsolicited recommendations like hydration, stretching, walking, studying, playlists, self-care, etc. after every message.
            6. DO NOT mention that someone likes the user or is sharing positive energy.
            7. Use emojis sparingly (at most 0-1 subtle emoji when natural).
            8. Keep responses concise, direct, and proportional to the user's message. A simple question deserves a simple answer.
            9. Match the user's tone. Be smart, conversational, helpful, and natural — like a smart, helpful friend/assistant.
            10. Do not end every response with a motivational statement or unsolicited sign-off.
            11. Do not repeat the user's name unnecessarily.

            CONTEXT USAGE:
            - You have access to user app context (pending tasks, goals).
            - ONLY use this context when it is directly relevant to the user's prompt (e.g. "What should I do today?", "Plan my day", "Help me prioritize", "What tasks do I have?").
            - If the user asks a casual question (e.g., "Hey", "What's 25% of 200?", "Tell me a joke", "I'm bored"), DO NOT mention tasks, habits, productivity, or schedules. Answer the question directly and naturally.

            EXAMPLES:
            - User: "Hey" -> "Hey Priyanka! 👋 How can I help?"
            - User: "What’s 25% of 200?" -> "50."
            - User: "I’m bored" -> "Want me to suggest something fun to do?"
            - User: "Tell me a joke" -> Tell a clean, funny joke directly.
            - User: "I feel stressed" -> "I'm sorry to hear that. Take a breath — is there anything specific on your mind, or would you like to take things off your plate for now?"
            - User: "Make me a study plan" -> Provide a practical, clean study schedule.
            - User: "Plan my day" -> Use their pending tasks and schedule to create a practical, structured day plan.
        """.trimIndent()

        val isTaskOrScheduleRelated = isContextRelevant(userMessage)

        val fullPrompt = if (isTaskOrScheduleRelated) {
            """
            [User App Context]
            Pending tasks: $currentTasks
            Goals: $userGoals

            User message: $userMessage
            """.trimIndent()
        } else {
            userMessage
        }

        val apiResult = GeminiClient.generateText(fullPrompt, systemInstruction)
        return if (apiResult.isSuccess) {
            apiResult.getOrNull() ?: fallbackPiaChat(userMessage, currentTasks)
        } else {
            fallbackPiaChat(userMessage, currentTasks)
        }
    }

    private fun isContextRelevant(message: String): Boolean {
        val lower = message.lowercase()
        val keywords = listOf(
            "task", "todo", "to-do", "plan my day", "what should i do", "my day",
            "schedule", "study plan", "work plan", "prioritize", "priority", "deadline",
            "goals", "pending", "routine", "what do i have"
        )
        return keywords.any { lower.contains(it) }
    }

    private fun fallbackPlan(prompt: String, tasks: String): String {
        val taskLines = if (tasks.isNotBlank()) {
            tasks.split(";").map { it.trim() }.filter { it.isNotEmpty() }
        } else emptyList()

        val sb = StringBuilder()
        sb.append("Here is a practical schedule for today:\n\n")

        if (taskLines.isNotEmpty()) {
            val times = listOf("09:30 AM", "11:00 AM", "02:00 PM", "04:30 PM")
            taskLines.take(4).forEachIndexed { index, task ->
                val time = times.getOrElse(index) { "05:00 PM" }
                sb.append("• **$time** — $task\n")
            }
            sb.append("• **01:00 PM** — Lunch & break\n")
        } else {
            sb.append("• **09:00 AM – 10:30 AM** — Core focus block\n")
            sb.append("• **10:30 AM – 10:45 AM** — Quick break\n")
            sb.append("• **10:45 AM – 12:30 PM** — Secondary tasks / study\n")
            sb.append("• **12:30 PM – 01:30 PM** — Lunch\n")
            sb.append("• **01:30 PM – 03:30 PM** — Afternoon focus\n")
            sb.append("• **03:30 PM – 04:00 PM** — Review & wrap-up\n")
        }

        return sb.toString().trimEnd()
    }

    private fun fallbackPiaChat(message: String, currentTasks: String = ""): String {
        val trimmed = message.trim()
        val lower = trimmed.lowercase()

        // 1. Exact or simple greetings
        if (lower == "hey" || lower == "hi" || lower == "hello" || lower == "hey pia" || lower == "hi pia") {
            return "Hey Priyanka! 👋 How can I help?"
        }
        if (lower.startsWith("good morning")) {
            return "Good morning, Priyanka! How can I help you today?"
        }
        if (lower.startsWith("good afternoon")) {
            return "Good afternoon! What can I help you with?"
        }
        if (lower.startsWith("good evening")) {
            return "Good evening! How can I assist you?"
        }
        if (lower.startsWith("good night")) {
            return "Good night! Sleep well."
        }

        // 2. Math & Calculations (e.g. "What’s 25% of 200?")
        if (lower.contains("25% of 200") || lower.contains("25 percent of 200")) {
            return "50."
        }
        if (lower.contains("50% of 200")) {
            return "100."
        }
        if (lower.contains("10% of 100")) {
            return "10."
        }

        // 3. Casual expressions & feelings
        if (lower == "i'm bored" || lower == "im bored" || lower == "i am bored" || lower == "bored") {
            return "Want me to suggest something fun to do?"
        }

        if (lower.contains("joke") || lower.contains("tell me a joke")) {
            val jokes = listOf(
                "Why do programmers prefer dark mode? Because light attracts bugs! 🐛",
                "Why was the math book sad? Because it had too many problems.",
                "How does a penguin build its house? Igloos it together!",
                "Why don't scientists trust atoms? Because they make up everything!"
            )
            return jokes.random()
        }

        if (lower.contains("stressed") || lower.contains("overwhelmed") || lower.contains("anxious")) {
            return "I'm sorry you're feeling that way. Take a breath — is there anything specific causing stress, or would you like help prioritizing just one small task?"
        }

        if (lower.contains("thank") || lower == "thanks") {
            return "You're welcome! Let me know if you need anything else."
        }

        if (lower.contains("how are you")) {
            return "I'm doing well, thank you! How can I help you today?"
        }

        if (lower.contains("who are you") || lower.contains("what can you do")) {
            return "I'm Pia, your personal assistant. I can help plan your day, break tasks into manageable steps, build study/work schedules, help prioritize to-dos, or answer general questions."
        }

        // 4. Productivity & Schedule actions (when specifically requested)
        if (lower.contains("break a task") || lower.contains("break down") || lower.contains("smaller steps") || lower.contains("break this task")) {
            return """
            Here is a practical way to break it down:
            1. **Preparation (5 mins)** — Gather whatever notes or materials you need.
            2. **First Milestone (20 mins)** — Focus on completing just the initial section.
            3. **Review & Finish (15 mins)** — Check through your work and mark it done.
            """.trimIndent()
        }

        if (lower.contains("study plan") || lower.contains("study schedule") || lower.contains("work schedule") || lower.contains("study routine")) {
            return """
            Here is a structured study schedule:
            • **Block 1 (45 mins):** Focus on the most challenging topic
            • **Break (10 mins):** Quick rest away from screens
            • **Block 2 (45 mins):** Review and practice problems
            • **Wrap-up (15 mins):** Summarize key points
            """.trimIndent()
        }

        if (lower.contains("prioritize") || lower.contains("priority")) {
            if (currentTasks.isNotBlank()) {
                val taskList = currentTasks.split(";").map { it.trim() }.filter { it.isNotEmpty() }
                return "Based on your list, here is a suggested order:\n" +
                        taskList.mapIndexed { i, t -> "${i + 1}. $t" }.joinToString("\n")
            }
            return """
            Here is a quick way to prioritize:
            1. **Top Priority:** Pick the single task that has the closest deadline or highest impact.
            2. **Secondary:** Tasks that are important but can follow afterward.
            3. **Quick Wins:** Small 5-minute tasks to knock out when you need momentum.
            """.trimIndent()
        }

        if (lower.contains("suggest a break") || lower.contains("suggest breaks") || lower.contains("break idea")) {
            return """
            A few quick break ideas:
            • Step away from screens and look out a window for 5 minutes
            • Grab a glass of water or make a cup of tea
            • Do a gentle stretch or a brief walk
            """.trimIndent()
        }

        if (lower.contains("plan my day") || lower.contains("what should i do today") || lower.contains("plan today")) {
            return fallbackPlan(message, currentTasks)
        }

        // 5. Default natural answer for general queries
        return "Got it. How can I help you with that?"
    }
}
