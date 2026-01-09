package com.cultureg.data.models

import java.util.UUID

/**
 * Modèle de données pour une question de culture générale
 * Format Question-Réponse (une seule réponse textuelle attendue)
 */
data class Question(
    val id: String = UUID.randomUUID().toString(),
    val question: String,
    val correctAnswer: String,
    val category: String = "Général",
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val timeLimit: Int? = null,  // Temps limite en secondes (optionnel)
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * Vérifie si une réponse textuelle est correcte
     * Comparaison insensible à la casse, accents et espaces
     */
    fun isCorrectAnswer(answer: String): Boolean {
        return normalizeAnswer(answer) == normalizeAnswer(correctAnswer)
    }
    
    /**
     * Normalise une réponse pour comparaison
     * - Convertit en minuscules
     * - Supprime les accents (approximation)
     * - Supprime les espaces multiples
     * - Trim les espaces en début/fin
     */
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



