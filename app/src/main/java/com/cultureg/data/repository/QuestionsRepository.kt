package com.cultureg.data.repository

import com.cultureg.data.models.Question
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository pour gérer les questions
 * Pour l'instant stockage en mémoire, à remplacer par DataStore ou base de données plus tard
 */
class QuestionsRepository {
    
    // Liste des questions en mémoire
    private val _questions = MutableStateFlow<List<Question>>(getDefaultQuestions())
    val questions: StateFlow<List<Question>> = _questions.asStateFlow()
    
    /**
     * Ajoute une nouvelle question
     */
    fun addQuestion(question: Question) {
        val currentList = _questions.value.toMutableList()
        currentList.add(question)
        _questions.value = currentList
    }
    
    /**
     * Met à jour une question existante
     */
    fun updateQuestion(question: Question) {
        val currentList = _questions.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == question.id }
        if (index != -1) {
            currentList[index] = question
            _questions.value = currentList
        }
    }
    
    /**
     * Supprime une question
     */
    fun deleteQuestion(questionId: String) {
        val currentList = _questions.value.toMutableList()
        currentList.removeIf { it.id == questionId }
        _questions.value = currentList
    }
    
    /**
     * Obtient une question par son ID
     */
    fun getQuestionById(questionId: String): Question? {
        return _questions.value.find { it.id == questionId }
    }
    
    /**
     * Recherche des questions par texte
     */
    fun searchQuestions(query: String): List<Question> {
        if (query.isBlank()) return _questions.value
        
        return _questions.value.filter { question ->
            question.question.contains(query, ignoreCase = true) ||
            question.category.contains(query, ignoreCase = true) ||
            question.correctAnswer.contains(query, ignoreCase = true)
        }
    }
    
    /**
     * Filtre par catégorie
     */
    fun getQuestionsByCategory(category: String): List<Question> {
        if (category == "Toutes") return _questions.value
        return _questions.value.filter { it.category == category }
    }
    
    /**
     * Questions par défaut pour tester (format Question-Réponse)
     */
    private fun getDefaultQuestions(): List<Question> {
        return listOf(
            Question(
                question = "Quelle est la capitale de la France ?",
                correctAnswer = "Paris",
                category = "Géographie",
                difficulty = com.cultureg.data.models.Difficulty.EASY,
                timeLimit = 30
            ),
            Question(
                question = "Qui a peint la Joconde ?",
                correctAnswer = "Léonard de Vinci",
                category = "Arts",
                difficulty = com.cultureg.data.models.Difficulty.MEDIUM,
                timeLimit = 45
            ),
            Question(
                question = "En quelle année a eu lieu la Révolution française ?",
                correctAnswer = "1789",
                category = "Histoire",
                difficulty = com.cultureg.data.models.Difficulty.MEDIUM,
                timeLimit = 30
            )
        )
    }
    
    companion object {
        // Singleton pour simplifier (à remplacer par injection de dépendances plus tard)
        @Volatile
        private var instance: QuestionsRepository? = null
        
        fun getInstance(): QuestionsRepository {
            return instance ?: synchronized(this) {
                instance ?: QuestionsRepository().also { instance = it }
            }
        }
    }
}



