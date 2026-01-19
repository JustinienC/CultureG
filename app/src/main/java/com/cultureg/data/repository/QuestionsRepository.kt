package com.cultureg.data.repository

import com.cultureg.data.models.Question
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class QuestionsRepository {
    
    private val _questions = MutableStateFlow<List<Question>>(getDefaultQuestions())
    val questions: StateFlow<List<Question>> = _questions.asStateFlow()
    
    fun addQuestion(question: Question) {
        val currentList = _questions.value.toMutableList()
        currentList.add(question)
        _questions.value = currentList
    }
    
    fun updateQuestion(question: Question) {
        val currentList = _questions.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == question.id }
        if (index != -1) {
            currentList[index] = question
            _questions.value = currentList
        }
    }
    
    fun deleteQuestion(questionId: String) {
        val currentList = _questions.value.toMutableList()
        currentList.removeIf { it.id == questionId }
        _questions.value = currentList
    }
    
    fun getQuestionById(questionId: String): Question? {
        return _questions.value.find { it.id == questionId }
    }
    
    fun searchQuestions(query: String): List<Question> {
        if (query.isBlank()) return _questions.value
        
        return _questions.value.filter { question ->
            question.question.contains(query, ignoreCase = true) ||
            question.category.contains(query, ignoreCase = true) ||
            question.correctAnswer.contains(query, ignoreCase = true)
        }
    }
    
    fun getQuestionsByCategory(category: String): List<Question> {
        if (category == "Toutes") return _questions.value
        return _questions.value.filter { it.category == category }
    }
    
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
        @Volatile
        private var instance: QuestionsRepository? = null
        
        fun getInstance(): QuestionsRepository {
            return instance ?: synchronized(this) {
                instance ?: QuestionsRepository().also { instance = it }
            }
        }
    }
}



