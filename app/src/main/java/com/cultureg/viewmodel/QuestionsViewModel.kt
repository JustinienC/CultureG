package com.cultureg.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cultureg.data.api.ConnectionState
import com.cultureg.data.api.RaspberryPiApi
import com.cultureg.data.models.Difficulty
import com.cultureg.data.models.Question
import com.cultureg.data.repository.QuestionsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class QuestionsViewModel : ViewModel() {
    
    private val repository = QuestionsRepository.getInstance()
    private val api = RaspberryPiApi.getInstance()
    
    private val _uiState = MutableStateFlow(QuestionsUiState())
    val uiState: StateFlow<QuestionsUiState> = _uiState.asStateFlow()
    
    val questions: StateFlow<List<Question>> = repository.questions
    
    private val _filteredQuestions = MutableStateFlow<List<Question>>(emptyList())
    val filteredQuestions: StateFlow<List<Question>> = _filteredQuestions.asStateFlow()
    
    init {
        viewModelScope.launch {
            questions.collect { allQuestions ->
                applyFilters(allQuestions)
            }
        }
        
        viewModelScope.launch {
            api.getConnectionState().collect { state ->
                if (state is ConnectionState.Connected) {
                    loadQuestionsFromServer()
                }
            }
        }
        
        viewModelScope.launch {
            api.observeMessageType("QUESTIONS_LIST").collect { message ->
                handleQuestionsListFromServer(message)
            }
        }
    }
    
    fun searchQuestions(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        applyFilters(questions.value)
    }
    
    fun filterByCategory(category: String) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        applyFilters(questions.value)
    }
    
    private fun applyFilters(allQuestions: List<Question>) {
        var filtered = allQuestions
        
        val category = _uiState.value.selectedCategory
        if (category != "Toutes") {
            filtered = filtered.filter { it.category == category }
        }
        
        val query = _uiState.value.searchQuery
        if (query.isNotBlank()) {
            filtered = filtered.filter { question ->
                question.question.contains(query, ignoreCase = true) ||
                question.category.contains(query, ignoreCase = true)
            }
        }
        
        _filteredQuestions.value = filtered
    }
    
    fun addQuestion(
        question: String,
        correctAnswer: String,
        category: String,
        difficulty: Difficulty,
        timeLimit: Int? = null
    ) {
        if (validateQuestion(question, correctAnswer)) {
            val newQuestion = Question(
                question = question,
                correctAnswer = correctAnswer,
                category = category,
                difficulty = difficulty,
                timeLimit = timeLimit
            )
            repository.addQuestion(newQuestion)
            
            viewModelScope.launch {
                val connectionState = api.getConnectionState().value
                if (connectionState is ConnectionState.Connected) {
                    val success = api.addQuestion(newQuestion)
                    if (success) {
                        Log.d("QuestionsViewModel", "Question envoyée au Raspberry Pi: ${newQuestion.id}")
                        _uiState.value = _uiState.value.copy(
                            showMessage = "Question ajoutée et synchronisée"
                        )
                    } else {
                        Log.w("QuestionsViewModel", "Échec envoi question au Raspberry Pi")
                        _uiState.value = _uiState.value.copy(
                            showMessage = "Question ajoutée localement (non synchronisée)"
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        showMessage = "Question ajoutée localement"
                    )
                }
            }
        }
    }
    
    fun updateQuestion(
        id: String,
        question: String,
        correctAnswer: String,
        category: String,
        difficulty: Difficulty,
        timeLimit: Int? = null
    ) {
        if (validateQuestion(question, correctAnswer)) {
            val updatedQuestion = Question(
                id = id,
                question = question,
                correctAnswer = correctAnswer,
                category = category,
                difficulty = difficulty,
                timeLimit = timeLimit
            )
            repository.updateQuestion(updatedQuestion)
            
            viewModelScope.launch {
                val connectionState = api.getConnectionState().value
                if (connectionState is ConnectionState.Connected) {
                    api.addQuestion(updatedQuestion)
                    _uiState.value = _uiState.value.copy(
                        showMessage = "Question modifiée et synchronisée"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        showMessage = "Question modifiée localement"
                    )
                }
            }
        }
    }
    
    fun deleteQuestion(questionId: String) {
        repository.deleteQuestion(questionId)
        
        viewModelScope.launch {
            val connectionState = api.getConnectionState().value
            if (connectionState is ConnectionState.Connected) {
                api.deleteQuestion(questionId)
                _uiState.value = _uiState.value.copy(
                    showMessage = "Question supprimée et synchronisée"
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    showMessage = "Question supprimée localement"
                )
            }
        }
    }
    
    fun getQuestionById(questionId: String): Question? {
        return repository.getQuestionById(questionId)
    }
    
    private fun validateQuestion(
        question: String,
        correctAnswer: String
    ): Boolean {
        if (question.isBlank()) {
            _uiState.value = _uiState.value.copy(
                showMessage = "La question ne peut pas être vide"
            )
            return false
        }
        
        if (correctAnswer.isBlank()) {
            _uiState.value = _uiState.value.copy(
                showMessage = "La réponse correcte ne peut pas être vide"
            )
            return false
        }
        
        return true
    }
    
    fun clearMessage() {
        _uiState.value = _uiState.value.copy(showMessage = null)
    }
    
    private fun loadQuestionsFromServer() {
        viewModelScope.launch {
            Log.d("QuestionsViewModel", "Chargement des questions depuis le serveur")
            api.getQuestions()
        }
    }
    
    private fun handleQuestionsListFromServer(message: com.cultureg.data.api.WebSocketMessage) {
        viewModelScope.launch {
            try {
                val serverQuestions = api.parseQuestions(message)
                Log.d("QuestionsViewModel", "Questions reçues du serveur: ${serverQuestions.size}")
                
                val localQuestions = repository.questions.value.toMutableList()
                val localIds = localQuestions.map { it.id }.toSet()
                
                serverQuestions.forEach { serverQuestion ->
                    if (serverQuestion.id !in localIds) {
                        repository.addQuestion(serverQuestion)
                        Log.d("QuestionsViewModel", "Question ajoutée depuis serveur: ${serverQuestion.id}")
                    }
                }
            } catch (e: Exception) {
                Log.e("QuestionsViewModel", "Erreur parsing questions serveur: ${e.message}", e)
            }
        }
    }
}

data class QuestionsUiState(
    val searchQuery: String = "",
    val selectedCategory: String = "Toutes",
    val showMessage: String? = null
)



