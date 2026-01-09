package com.cultureg.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.cultureg.data.models.Difficulty
import com.cultureg.data.models.Question
import com.cultureg.data.models.QuestionCategories

/**
 * Dialogue pour ajouter ou modifier une question (format Question-Réponse)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionFormDialog(
    question: Question? = null, // null = mode ajout, non-null = mode édition
    onDismiss: () -> Unit,
    onSave: (question: String, correctAnswer: String, category: String, difficulty: Difficulty, timeLimit: Int?) -> Unit
) {
    // État du formulaire
    var questionText by remember { mutableStateOf(question?.question ?: "") }
    var correctAnswer by remember { mutableStateOf(question?.correctAnswer ?: "") }
    var selectedCategory by remember { mutableStateOf(question?.category ?: "Général") }
    var selectedDifficulty by remember { mutableStateOf(question?.difficulty ?: Difficulty.MEDIUM) }
    var timeLimitEnabled by remember { mutableStateOf(question?.timeLimit != null) }
    var timeLimit by remember { mutableIntStateOf(question?.timeLimit ?: 30) }
    var showCategoryMenu by remember { mutableStateOf(false) }
    var showDifficultyMenu by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // En-tête
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (question == null) "Nouvelle Question" else "Modifier Question",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = "Fermer",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
                
                // Formulaire scrollable
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Message d'erreur
                    errorMessage?.let { error ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                text = error,
                                modifier = Modifier.padding(12.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    
                    // Question
                    OutlinedTextField(
                        value = questionText,
                        onValueChange = { questionText = it },
                        label = { Text("Question") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4,
                        supportingText = { Text("${questionText.length}/200") }
                    )
                    
                    // Réponse correcte
                    OutlinedTextField(
                        value = correctAnswer,
                        onValueChange = { correctAnswer = it },
                        label = { Text("Réponse correcte") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        supportingText = { Text("La réponse attendue (insensible à la casse et aux accents)") }
                    )
                    
                    // Catégorie
                    ExposedDropdownMenuBox(
                        expanded = showCategoryMenu,
                        onExpandedChange = { showCategoryMenu = it }
                    ) {
                        OutlinedTextField(
                            value = selectedCategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Catégorie") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryMenu)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = showCategoryMenu,
                            onDismissRequest = { showCategoryMenu = false }
                        ) {
                            QuestionCategories.ALL.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category) },
                                    onClick = {
                                        selectedCategory = category
                                        showCategoryMenu = false
                                    }
                                )
                            }
                        }
                    }
                    
                    // Difficulté
                    ExposedDropdownMenuBox(
                        expanded = showDifficultyMenu,
                        onExpandedChange = { showDifficultyMenu = it }
                    ) {
                        OutlinedTextField(
                            value = selectedDifficulty.displayName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Difficulté") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = showDifficultyMenu)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = showDifficultyMenu,
                            onDismissRequest = { showDifficultyMenu = false }
                        ) {
                            Difficulty.values().forEach { difficulty ->
                                DropdownMenuItem(
                                    text = { Text(difficulty.displayName) },
                                    onClick = {
                                        selectedDifficulty = difficulty
                                        showDifficultyMenu = false
                                    }
                                )
                            }
                        }
                    }
                    
                    Divider()
                    
                    // Chronomètre
                    Text(
                        text = "Chronomètre",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = timeLimitEnabled,
                            onCheckedChange = { timeLimitEnabled = it }
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Activer le chronomètre",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    if (timeLimitEnabled) {
                        OutlinedTextField(
                            value = timeLimit.toString(),
                            onValueChange = { 
                                val value = it.toIntOrNull()
                                if (value != null && value > 0) {
                                    timeLimit = value
                                }
                            },
                            label = { Text("Temps limite (secondes)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = timeLimitEnabled,
                            supportingText = { Text("Temps maximum pour répondre à cette question") }
                        )
                    }
                    
                    // Info
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Info,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Format Question-Réponse : une seule réponse textuelle attendue",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                
                // Boutons d'action
                Divider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Annuler")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            // Validation
                            when {
                                questionText.isBlank() -> {
                                    errorMessage = "La question ne peut pas être vide"
                                }
                                correctAnswer.isBlank() -> {
                                    errorMessage = "La réponse correcte ne peut pas être vide"
                                }
                                timeLimitEnabled && timeLimit <= 0 -> {
                                    errorMessage = "Le temps limite doit être supérieur à 0"
                                }
                                else -> {
                                    onSave(
                                        questionText,
                                        correctAnswer,
                                        selectedCategory,
                                        selectedDifficulty,
                                        if (timeLimitEnabled) timeLimit else null
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (question == null) "Ajouter" else "Modifier")
                    }
                }
            }
        }
    }
}
