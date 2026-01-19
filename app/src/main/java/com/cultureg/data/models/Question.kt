package com.cultureg.data.models

import java.util.UUID

data class Question(
    val id: String = UUID.randomUUID().toString(),
    val question: String,
    val correctAnswer: String,
    val category: String = "Général",
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val timeLimit: Int? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun isCorrectAnswer(answer: String): Boolean {
        return normalizeAnswer(answer) == normalizeAnswer(correctAnswer)
    }
    
    private fun normalizeAnswer(text: String): String {
        return text.lowercase()
            .replace(Regex("[àáâãäå]"), "a")
            .replace(Regex("[èéêë]"), "e")
            .replace(Regex("[ìíîï]"), "i")
            .replace(Regex("[òóôõö]"), "o")
            .replace(Regex("[ùúûü]"), "u")
            .replace(Regex("[ç]"), "c")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}

enum class Difficulty(val displayName: String) {
    EASY("Facile"),
    MEDIUM("Moyen"),
    HARD("Difficile")
}

object QuestionCategories {
    val ALL = listOf(
        "Général",
        "Histoire",
        "Géographie",
        "Sciences",
        "Sports",
        "Culture",
        "Arts",
        "Littérature",
        "Cinéma",
        "Musique"
    )
}



