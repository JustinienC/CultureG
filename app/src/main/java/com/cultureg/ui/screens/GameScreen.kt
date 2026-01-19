package com.cultureg.ui.screens

import android.Manifest
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cultureg.data.api.ConnectionState
import com.cultureg.viewmodel.GameViewModel

/**
 * Écran de jeu avec bouton d'enregistrement
 */
@Composable
fun GameScreen(
    viewModel: GameViewModel = viewModel()
) {
    val context = LocalContext.current
    val connectionState by viewModel.connectionState.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val partialResult by viewModel.partialResult.collectAsState()
    val error by viewModel.errorMessage.collectAsState()
    
    // Gestion de la permission microphone
    val hasAudioPermission = remember {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startRecording()
        } else {
            viewModel.clearError()
            // L'erreur sera gérée par SpeechRecognitionService
        }
    }
    
    fun requestPermissionAndStartRecording() {
        if (hasAudioPermission) {
            viewModel.startRecording()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
    
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
            onStartClick = { requestPermissionAndStartRecording() },
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
 * Bouton d'enregistrement
 */
@Composable
fun RecordButton(
    isListening: Boolean,
    onStartClick: () -> Unit,
    onStopClick: () -> Unit
) {
    val (buttonText, buttonColor) = if (isListening) {
        Pair(
            "Arrêter",
            MaterialTheme.colorScheme.error
        )
    } else {
        Pair(
            "Enregistrer",
            MaterialTheme.colorScheme.primary
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
        enabled = true,
        modifier = Modifier
            .size(120.dp)
            .padding(16.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonColor
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

