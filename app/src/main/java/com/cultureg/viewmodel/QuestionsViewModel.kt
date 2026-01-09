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

/**
 * ViewModel pour la gestion des questions
 */
class QuestionsViewModel : ViewModel() {
    
    private val repository = QuestionsRepository.getInstance()
    private val api = RaspberryPiApi.getInstance()
    
    // État de l'UI
    private val _uiState = MutableStateFlow(QuestionsUiState())
    val uiState: StateFlow<QuestionsUiState> = _uiState.asStateFlow()
    
    // Questions
    val questions: StateFlow<List<Question>> = repository.questions
    
    // Questions filtrées
    private val _filteredQuestions = MutableStateFlow<List<Question>>(emptyList())
    val filteredQuestions: StateFlow<List<Question>> = _filteredQuestions.asStateFlow()
    
    init {
        // Observer les questions et appliquer les filtres
        viewModelScope.launch {
            questions.collect { allQuestions ->
                applyFilters(allQuestions)
            }
        }
        
        // Charger les questions du Raspberry Pi au démarrage si connecté
        viewModelScope.launch {
            api.getConnectionState().collect { state ->
                if (state is ConnectionState.Connected) {
                    loadQuestionsFromServer()
                }
            }
        }
        
        // Observer les messages du serveur
        viewModelScope.launch {
            api.observeMessageType("QUESTIONS_LIST").collect { message ->
                handleQuestionsListFromServer(message)
            }
        }
    }
    
    /**
     * Recherche de questions
     */
    fun searchQuestions(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        applyFilters(questions.value)
    }
    
    /**
     * Filtre par catégorie
     */
    fun filterByCategory(category: String) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        applyFilters(questions.value)
    }
    
    /**
     * Applique les filtres
     */
    private fun applyFilters(allQuestions: List<Question>) {
        var filtered = allQuestions
        
        // Filtre par catégorie
        val category = _uiState.value.selectedCategory
        if (category != "Toutes") {
            filtered = filtered.filter { it.category == category }
        }
        
        // Filtre par recherche
        val query = _uiState.value.searchQuery
        if (query.isNotBlank()) {
            filtered = filtered.filter { question ->
                question.question.contains(query, ignoreCase = true) ||
                question.category.contains(query, ignoreCase = true)
            }
        }
        
        _filteredQuestions.value = filtered
    }
    
    /**
     * Ajoute une nouvelle question
     */
    fun addQuestion(
        question: String,
        answers: List<String>,
        correctAnswerIndex: Int,
        category: String,
        difficulty: Difficulty
    ) {
        if (validateQuestion(question, answers, correctAnswerIndex)) {
            val newQuestion = Question(
                question = question,
                answers = answers,
                correctAnswerIndex = correctAnswerIndex,
                category = category,
                difficulty = difficulty
            )
            // Ajouter localement
            repository.addQuestion(newQuestion)
            
            // Envoyer au Raspberry Pi si connecté
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
    
    /**
     * Met à jour une question
     */
    fun updateQuestion(
        id: String,
        question: String,
        answers: List<String>,
        correctAnswerIndex: Int,
        category: String,
        difficulty: Difficulty
    ) {
        if (validateQuestion(question, answers, correctAnswerIndex)) {
            val updatedQuestion = Question(
                id = id,
                question = question,
                answers = answers,
                correctAnswerIndex = correctAnswerIndex,
                category = category,
                difficulty = difficulty
            )
            // Mettre à jour localement
            repository.updateQuestion(updatedQuestion)
            
            // Envoyer au Raspberry Pi si connecté
            viewModelScope.launch {
                val connectionState = api.getConnectionState().value
                if (connectionState is ConnectionState.Connected) {
                    api.addQuestion(updatedQuestion) // Le serveur fait INSERT OR REPLACE
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
    
    /**
     * Supprime une question
     */
    fun deleteQuestion(questionId: String) {
        // Supprimer localement
        repository.deleteQuestion(questionId)
        
        // Supprimer du Raspberry Pi si connecté
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
    
    /**
     * Obtient une question par ID
     */
    fun getQuestionById(questionId: String): Question? {
        return repository.getQuestionById(questionId)
    }
    
    /**
     * Valide une question
     */
    private fun validateQuestion(
        question: String,
        answers: List<String>,
        correctAnswerIndex: Int
    ): Boolean {
        if (question.isBlank()) {
            _uiState.value = _uiState.value.copy(
                showMessage = "La question ne peut pas être vide"
            )
            return false
        }
        
        if (answers.size < 2) {
            _uiState.value = _uiState.value.copy(
                showMessage = "Il faut au moins 2 réponses"
            )
            return false
        }
        
        if (answers.any { it.isBlank() }) {
            _uiState.value = _uiState.value.copy(
                showMessage = "Les réponses ne peuvent pas être vides"
            )
            return false
        }
        
        if (correctAnswerIndex !in answers.indices) {
            _uiState.value = _uiState.value.copy(
                showMessage = "L'index de la réponse correcte est invalide"
            )
            return false
        }
        
        return true
    }
    
    /**
     * Efface le message
     */
    fun clearMessage() {
        _uiState.value = _uiState.value.copy(showMessage = null)
    }
    
    /**
     * Charger les questions depuis le serveur
     */
    private fun loadQuestionsFromServer() {
        viewModelScope.launch {
            Log.d("QuestionsViewModel", "Chargement des questions depuis le serveur")
            api.getQuestions()
        }
    }
    
    /**
     * Traiter la liste de questions reçue du serveur
     */
    private fun handleQuestionsListFromServer(message: com.cultureg.data.api.WebSocketMessage) {
        viewModelScope.launch {
            try {
                val serverQuestions = api.parseQuestions(message)
                Log.d("QuestionsViewModel", "Questions reçues du serveur: ${serverQuestions.size}")
                
                // Fusionner avec les questions locales (éviter les doublons)
                val localQuestions = repository.questions.value.toMutableList()
                val localIds = localQuestions.map { it.id }.toSet()
                
                // Ajouter seulement les questions qui n'existent pas localement
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

/**
 * État de l'UI pour l'écran Questions
 */
data class QuestionsUiState(
    val searchQuery: String = "",
    val selectedCategory: String = "Toutes",
    val showMessage: String? = null
)



