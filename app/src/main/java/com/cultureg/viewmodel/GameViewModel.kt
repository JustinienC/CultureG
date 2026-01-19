package com.cultureg.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cultureg.data.api.ConnectionState
import com.cultureg.data.api.WebSocketClient
import com.cultureg.data.speech.SpeechRecognitionService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import android.util.Log

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
        
        // Vérifier que le WebSocket est connecté
        val connectionState = webSocketClient.connectionState.value
        if (connectionState !is ConnectionState.Connected) {
            _errorMessage.value = "Non connecté au serveur. État: $connectionState"
            Log.e(TAG, "Tentative d'envoi sans connexion: $connectionState")
            return
        }
        
        val success = webSocketClient.sendAnswer(answer)
        if (success) {
            Log.d(TAG, "Réponse envoyée: $answer")
            _errorMessage.value = null
        } else {
            _errorMessage.value = "Erreur lors de l'envoi de la réponse. Vérifie la connexion."
            Log.e(TAG, "Échec envoi réponse: $answer")
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
}
