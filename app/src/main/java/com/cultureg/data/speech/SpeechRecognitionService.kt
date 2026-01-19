package com.cultureg.data.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SpeechRecognitionService(private val context: Context) {
    
    private var speechRecognizer: SpeechRecognizer? = null
    private val recognizerIntent: Intent
    
    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()
    
    private val _recognitionResult = MutableStateFlow<String?>(null)
    val recognitionResult: StateFlow<String?> = _recognitionResult.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _partialResult = MutableStateFlow<String?>(null)
    val partialResult: StateFlow<String?> = _partialResult.asStateFlow()
    
    init {
        recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "fr-FR")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer?.setRecognitionListener(createRecognitionListener())
        } else {
            Log.e(TAG, "SpeechRecognizer non disponible sur cet appareil")
        }
    }
    
    fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _error.value = "Reconnaissance vocale non disponible"
            return
        }
        
        val recognizer = speechRecognizer ?: run {
            _error.value = "SpeechRecognizer non initialisé"
            return
        }
        
        try {
            _isListening.value = true
            _error.value = null
            _recognitionResult.value = null
            _partialResult.value = null
            recognizer.startListening(recognizerIntent)
            Log.d(TAG, "Démarrage de l'enregistrement")
        } catch (e: Exception) {
            Log.e(TAG, "Erreur démarrage enregistrement: ${e.message}", e)
            _error.value = "Erreur: ${e.message}"
            _isListening.value = false
        }
    }
    
    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            _isListening.value = false
            Log.d(TAG, "Arrêt de l'enregistrement")
        } catch (e: Exception) {
            Log.e(TAG, "Erreur arrêt enregistrement: ${e.message}", e)
        }
    }
    
    fun cancelListening() {
        try {
            speechRecognizer?.cancel()
            _isListening.value = false
            _recognitionResult.value = null
            _partialResult.value = null
            Log.d(TAG, "Annulation de l'enregistrement")
        } catch (e: Exception) {
            Log.e(TAG, "Erreur annulation enregistrement: ${e.message}", e)
        }
    }
    
    private fun createRecognitionListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d(TAG, "Prêt pour la parole")
            }
            
            override fun onBeginningOfSpeech() {
                Log.d(TAG, "Début de la parole")
            }
            
            override fun onRmsChanged(rmsdB: Float) {
            }
            
            override fun onBufferReceived(buffer: ByteArray?) {
            }
            
            override fun onEndOfSpeech() {
                Log.d(TAG, "Fin de la parole")
            }
            
            override fun onError(error: Int) {
                val errorMessage = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Erreur audio - Vérifie le microphone"
                    SpeechRecognizer.ERROR_CLIENT -> "Erreur client - Réessaye"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permission microphone refusée - Active-la dans les paramètres"
                    SpeechRecognizer.ERROR_NETWORK -> "Erreur réseau - La reconnaissance vocale nécessite Internet. Vérifie ta connexion."
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Timeout réseau - Vérifie ta connexion Internet"
                    SpeechRecognizer.ERROR_NO_MATCH -> "Aucune correspondance - Réessaye en parlant plus clairement"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Reconnaissance occupée - Attends un instant"
                    SpeechRecognizer.ERROR_SERVER -> "Erreur serveur Google - Réessaye plus tard"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Timeout parole - Parle plus fort ou plus près du micro"
                    else -> "Erreur inconnue: $error"
                }
                Log.e(TAG, "Erreur reconnaissance: $errorMessage (code: $error)")
                _error.value = errorMessage
                _isListening.value = false
            }
            
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val confidence = results?.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)
                
                if (matches != null && matches.isNotEmpty()) {
                    val recognizedText = matches[0]
                    Log.d(TAG, "Texte reconnu: $recognizedText")
                    _recognitionResult.value = recognizedText
                } else {
                    Log.w(TAG, "Aucun résultat de reconnaissance")
                    _error.value = "Aucun texte reconnu"
                }
                _isListening.value = false
            }
            
            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (matches != null && matches.isNotEmpty()) {
                    val partialText = matches[0]
                    Log.d(TAG, "Résultat partiel: $partialText")
                    _partialResult.value = partialText
                }
            }
            
            override fun onEvent(eventType: Int, params: Bundle?) {
                Log.d(TAG, "Événement: $eventType")
            }
        }
    }
    
    fun cleanup() {
        cancelListening()
        speechRecognizer?.destroy()
        speechRecognizer = null
    }
    
    companion object {
        private const val TAG = "SpeechRecognition"
    }
}
