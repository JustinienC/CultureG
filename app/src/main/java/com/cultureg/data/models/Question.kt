package com.cultureg.data.models

import java.util.UUID

/**
 * Modèle de données pour une question de culture générale
 */
data class Question(
    val id: String = UUID.randomUUID().toString(),
    val question: String,
    val answers: List<String>,
    val correctAnswerIndex: Int,
    val category: String = "Général",
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * Vérifie si une réponse est correcte
     */
    fun isCorrectAnswer(answerIndex: Int): Boolean {
        return answerIndex == correctAnswerIndex
    }
    
    /**
     * Obtient la réponse correcte
     */
    fun getCorrectAnswer(): String {
        return answers.getOrNull(correctAnswerIndex) ?: ""
    }
}

/**
 * Niveaux de difficulté
 */
enum class Difficulty(val displayName: String) {
    EASY("Facile"),
    MEDIUM("Moyen"),
    HARD("Difficile")
}

/**
 * Catégories de questions
 */
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



