package com.cultureg.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cultureg.data.api.ConnectionState
import com.cultureg.viewmodel.GameViewModel
import com.cultureg.viewmodel.GameState
import com.cultureg.viewmodel.QuestionData

/**
 * Écran de jeu avec bouton d'enregistrement
 */
@Composable
fun GameScreen(
    viewModel: GameViewModel = viewModel()
) {
    val connectionState by viewModel.connectionState.collectAsState()
    val gameState by viewModel.gameState.collectAsState()
    val currentQuestion by viewModel.currentQuestion.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val partialResult by viewModel.partialResult.collectAsState()
    val error by viewModel.errorMessage.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // En-tête avec état de connexion
        ConnectionStatusCard(connectionState)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Question actuelle (si disponible)
        currentQuestion?.let { question ->
            QuestionCard(question)
        } ?: run {
            Text(
                text = "En attente d'une question...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Résultat partiel de la reconnaissance
        partialResult?.let { text ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Text(
                    text = text,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Bouton d'enregistrement
        RecordButton(
            isListening = isListening,
            gameState = gameState,
            onStartClick = { viewModel.startRecording() },
            onStopClick = { viewModel.stopRecording() }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Message d'erreur
        error?.let { errorMsg ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = errorMsg,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = { viewModel.clearError() }) {
                        Icon(Icons.Filled.Close, contentDescription = "Fermer")
                    }
                }
            }
        }
        
        // Résultat de la réponse
        when (val state = gameState) {
            is GameState.AnswerProcessed -> {
                Spacer(modifier = Modifier.height(8.dp))
                AnswerResultCard(state)
            }
            else -> {}
        }
    }
}

/**
 * Carte d'état de connexion
 */
@Composable
fun ConnectionStatusCard(connectionState: ConnectionState) {
    val (statusText, statusColor, statusIcon) = when (connectionState) {
        is ConnectionState.Connected -> Triple("Connecté", Color(0xFF4CAF50), Icons.Filled.CheckCircle)
        is ConnectionState.Connecting -> Triple("Connexion...", Color(0xFFFF9800), Icons.Filled.Sync)
        is ConnectionState.Error -> Triple("Erreur: ${connectionState.message}", Color(0xFFF44336), Icons.Filled.Error)
        else -> Triple("Déconnecté", Color(0xFF9E9E9E), Icons.Filled.Cancel)
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                statusIcon,
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/**
 * Carte de question
 */
@Composable
fun QuestionCard(question: QuestionData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Question ${question.questionNumber}/${question.totalQuestions}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "Score: ${question.currentScore}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = question.question,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Bouton d'enregistrement
 */
@Composable
fun RecordButton(
    isListening: Boolean,
    gameState: GameState,
    onStartClick: () -> Unit,
    onStopClick: () -> Unit
) {
    val (buttonText, buttonColor, enabled) = when {
        gameState is GameState.WaitingForQuestion -> Triple(
            "En attente...",
            MaterialTheme.colorScheme.surfaceVariant,
            false
        )
        isListening -> Triple(
            "Arrêter",
            MaterialTheme.colorScheme.error,
            true
        )
        gameState is GameState.ProcessingAnswer || gameState is GameState.AnswerSent -> Triple(
            "Traitement...",
            MaterialTheme.colorScheme.surfaceVariant,
            false
        )
        else -> Triple(
            "Appuyer pour répondre",
            MaterialTheme.colorScheme.primary,
            true
        )
    }
    
    Button(
        onClick = {
            if (isListening) {
                onStopClick()
            } else {
                onStartClick()
            }
        },
        enabled = enabled,
        modifier = Modifier
            .size(120.dp)
            .padding(16.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonColor,
            disabledContainerColor = buttonColor.copy(alpha = 0.6f)
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isListening) {
                Icon(
                    Icons.Filled.Stop,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp)
                )
            } else {
                Icon(
                    Icons.Filled.Mic,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = buttonText,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Carte de résultat de réponse
 */
@Composable
fun AnswerResultCard(result: GameState.AnswerProcessed) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (result.isCorrect) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (result.isCorrect) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                    contentDescription = null,
                    tint = if (result.isCorrect) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (result.isCorrect) "Correct !" else "Incorrect",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            if (!result.isCorrect) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "La bonne réponse était: ${result.correctAnswer}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Votre réponse: ${result.userAnswer}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}
