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

class GameViewModel(application: Application) : AndroidViewModel(application) {
    
    private val webSocketClient = WebSocketClient.getInstance()
    private val speechService = SpeechRecognitionService(application)
    
    val connectionState = webSocketClient.connectionState
    
    private val _gameState = MutableStateFlow<GameState>(GameState.Idle)
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()
    
    val isListening = speechService.isListening
    
    val partialResult = speechService.partialResult
    
    val error = speechService.error
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    init {
        viewModelScope.launch {
            speechService.recognitionResult.collect { result ->
                result?.let { text ->
                    Log.d(TAG, "Résultat reconnaissance: $text")
                    sendAnswer(text)
                }
            }
        }
        
        viewModelScope.launch {
            speechService.error.collect { error ->
                error?.let {
                    _errorMessage.value = it
                }
            }
        }
    }
    
    fun startRecording() {
        if (speechService.isListening.value) {
            stopRecording()
            return
        }
        
        _gameState.value = GameState.Recording
        speechService.startListening()
        Log.d(TAG, "Démarrage de l'enregistrement")
    }
    
    fun stopRecording() {
        if (!speechService.isListening.value) {
            return
        }
        
        speechService.stopListening()
        _gameState.value = GameState.Idle
        Log.d(TAG, "Arrêt de l'enregistrement")
    }
    
    private fun sendAnswer(answer: String) {
        if (answer.isBlank()) {
            _errorMessage.value = "Réponse vide"
            return
        }
        
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

sealed class GameState {
    object Idle : GameState()
    object Recording : GameState()
}
