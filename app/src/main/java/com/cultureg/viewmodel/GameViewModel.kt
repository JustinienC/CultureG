package com.cultureg.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cultureg.data.api.WebSocketClient
import com.cultureg.data.api.WebSocketMessage
import com.cultureg.data.speech.SpeechRecognitionService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import android.util.Log
import org.json.JSONObject

/**
 * ViewModel pour gérer l'écran de jeu
 */
class GameViewModel(application: Application) : AndroidViewModel(application) {
    
    private val webSocketClient = WebSocketClient.getInstance()
    private val speechService = SpeechRecognitionService(application)
    
    // État de la connexion
    val connectionState = webSocketClient.connectionState
    
    // État du jeu
    private val _gameState = MutableStateFlow<GameState>(GameState.Idle)
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()
    
    // Question actuelle
    private val _currentQuestion = MutableStateFlow<QuestionData?>(null)
    val currentQuestion: StateFlow<QuestionData?> = _currentQuestion.asStateFlow()
    
    // État de l'enregistrement
    val isListening = speechService.isListening
    
    // Résultat partiel de la reconnaissance
    val partialResult = speechService.partialResult
    
    // Erreur
    val error = speechService.error
    
    // Message d'erreur général
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    init {
        // Écouter les messages WebSocket
        viewModelScope.launch {
            webSocketClient.lastMessage.collect { message ->
                message?.let { handleWebSocketMessage(it) }
            }
        }
        
        // Écouter les résultats de reconnaissance vocale
        viewModelScope.launch {
            speechService.recognitionResult.collect { result ->
                result?.let { text ->
                    Log.d(TAG, "Résultat reconnaissance: $text")
                    sendAnswer(text)
                }
            }
        }
        
        // Écouter les erreurs de reconnaissance
        viewModelScope.launch {
            speechService.error.collect { error ->
                error?.let {
                    _errorMessage.value = it
                }
            }
        }
    }
    
    /**
     * Gérer les messages WebSocket
     */
    private fun handleWebSocketMessage(message: WebSocketMessage) {
        when (message.type) {
            "CURRENT_QUESTION" -> {
                try {
                    val data = JSONObject(message.data ?: "{}")
                    val questionData = data.getJSONObject("question")
                    val question = QuestionData(
                        id = questionData.getString("id"),
                        question = questionData.getString("question"),
                        category = questionData.optString("category", "Général"),
                        difficulty = questionData.optString("difficulty", "MEDIUM"),
                        questionNumber = data.optInt("questionNumber", 0),
                        totalQuestions = data.optInt("totalQuestions", 0),
                        currentScore = data.optInt("currentScore", 0)
                    )
                    _currentQuestion.value = question
                    Log.d(TAG, "Question reçue: ${question.question}")
                } catch (e: Exception) {
                    Log.e(TAG, "Erreur parsing CURRENT_QUESTION: ${e.message}", e)
                    _errorMessage.value = "Erreur lors de la réception de la question"
                }
            }
            "ANSWER_RESULT" -> {
                try {
                    val data = JSONObject(message.data ?: "{}")
                    val isCorrect = data.getBoolean("isCorrect")
                    val correctAnswer = data.optString("correctAnswer", "")
                    val userAnswer = data.optString("userAnswer", "")
                    val currentScore = data.optInt("currentScore", 0)
                    
                    _gameState.value = GameState.AnswerProcessed(
                        isCorrect = isCorrect,
                        correctAnswer = correctAnswer,
                        userAnswer = userAnswer,
                        currentScore = currentScore
                    )
                    Log.d(TAG, "Résultat: ${if (isCorrect) "Correct" else "Incorrect"}")
                    
                    // Réinitialiser pour la prochaine question après un délai
                    viewModelScope.launch {
                        kotlinx.coroutines.delay(3000)
                        _gameState.value = GameState.Idle
                        _currentQuestion.value = null
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Erreur parsing ANSWER_RESULT: ${e.message}", e)
                }
            }
            "GAME_ENDED" -> {
                _gameState.value = GameState.Idle
                Log.d(TAG, "Jeu terminé")
            }
        }
    }
    
    /**
     * Démarrer l'enregistrement
     */
    fun startRecording() {
        if (speechService.isListening.value) {
            // Si déjà en train d'enregistrer, arrêter
            stopRecording()
            return
        }
        
        _gameState.value = GameState.Recording
        speechService.startListening()
        Log.d(TAG, "Démarrage de l'enregistrement")
    }
    
    /**
     * Arrêter l'enregistrement et envoyer la réponse
     */
    fun stopRecording() {
        if (!speechService.isListening.value) {
            return
        }
        
        speechService.stopListening()
        _gameState.value = GameState.Idle
        Log.d(TAG, "Arrêt de l'enregistrement")
    }
    
    /**
     * Envoyer la réponse
     */
    private fun sendAnswer(answer: String) {
        if (answer.isBlank()) {
            _errorMessage.value = "Réponse vide"
            return
        }
        
        val success = webSocketClient.sendAnswer(answer)
        if (success) {
            Log.d(TAG, "Réponse envoyée: $answer")
        } else {
            _errorMessage.value = "Erreur lors de l'envoi de la réponse"
        }
    }
    
    /**
     * Effacer le message d'erreur
     */
    fun clearError() {
        _errorMessage.value = null
    }
    
    override fun onCleared() {
        super.onCleared()
        speechService.cleanup()
    }
    
    companion object {
        private const val TAG = "GameViewModel"
    }
}

/**
 * État du jeu
 */
sealed class GameState {
    object Idle : GameState()
    object Recording : GameState()
    data class AnswerProcessed(
        val isCorrect: Boolean,
        val correctAnswer: String,
        val userAnswer: String,
        val currentScore: Int
    ) : GameState()
}

/**
 * Données d'une question
 */
data class QuestionData(
    val id: String,
    val question: String,
    val category: String,
    val difficulty: String,
    val questionNumber: Int,
    val totalQuestions: Int,
    val currentScore: Int
)
