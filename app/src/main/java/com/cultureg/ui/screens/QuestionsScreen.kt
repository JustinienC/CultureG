package com.cultureg.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cultureg.data.models.Difficulty
import com.cultureg.data.models.Question
import com.cultureg.data.models.QuestionCategories
import com.cultureg.viewmodel.QuestionsViewModel

/**
 * Écran principal de gestion des questions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionsScreen(
    viewModel: QuestionsViewModel = viewModel()
) {
    val filteredQuestions by viewModel.filteredQuestions.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var questionToEdit by remember { mutableStateOf<Question?>(null) }
    var showDeleteDialog by remember { mutableStateOf<Question?>(null) }
    var showFilterDialog by remember { mutableStateOf(false) }
    
    // Afficher les messages
    LaunchedEffect(uiState.showMessage) {
        uiState.showMessage?.let {
            // Message affiché
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text("Questions (${filteredQuestions.size})") 
                },
                actions = {
                    // Bouton filtre
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(Icons.Filled.FilterList, contentDescription = "Filtrer")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Ajouter question")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Barre de recherche
            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = { viewModel.searchQuestions(it) }
            )
            
            // Filtre actif
            if (uiState.selectedCategory != "Toutes") {
                FilterChip(
                    category = uiState.selectedCategory,
                    onRemove = { viewModel.filterByCategory("Toutes") }
                )
            }
            
            // Liste des questions
            if (filteredQuestions.isEmpty()) {
                EmptyState(
                    onAddClick = { showAddDialog = true }
                )
            } else {
                QuestionsList(
                    questions = filteredQuestions,
                    onEditClick = { questionToEdit = it },
                    onDeleteClick = { showDeleteDialog = it }
                )
            }
        }
    }
    
    // Dialogue d'ajout
    if (showAddDialog) {
        QuestionFormDialog(
            onDismiss = { showAddDialog = false },
            onSave = { question, correctAnswer, category, difficulty, timeLimit ->
                viewModel.addQuestion(question, correctAnswer, category, difficulty, timeLimit)
                showAddDialog = false
            }
        )
    }
    
    // Dialogue de modification
    questionToEdit?.let { question ->
        QuestionFormDialog(
            question = question,
            onDismiss = { questionToEdit = null },
            onSave = { questionText, correctAnswer, category, difficulty, timeLimit ->
                viewModel.updateQuestion(
                    question.id,
                    questionText,
                    correctAnswer,
                    category,
                    difficulty,
                    timeLimit
                )
                questionToEdit = null
            }
        )
    }
    
    // Dialogue de suppression
    showDeleteDialog?.let { question ->
        DeleteConfirmDialog(
            question = question,
            onDismiss = { showDeleteDialog = null },
            onConfirm = {
                viewModel.deleteQuestion(question.id)
                showDeleteDialog = null
            }
        )
    }
    
    // Dialogue de filtre
    if (showFilterDialog) {
        FilterDialog(
            selectedCategory = uiState.selectedCategory,
            onDismiss = { showFilterDialog = false },
            onCategorySelected = { 
                viewModel.filterByCategory(it)
                showFilterDialog = false
            }
        )
    }
    
    // Snackbar pour messages
    uiState.showMessage?.let { message ->
        LaunchedEffect(message) {
            kotlinx.coroutines.delay(2000)
            viewModel.clearMessage()
        }
    }
}

/**
 * Barre de recherche
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        placeholder = { Text("Rechercher une question...") },
        leadingIcon = { 
            Icon(Icons.Filled.Search, contentDescription = "Rechercher") 
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Filled.Clear, contentDescription = "Effacer")
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
    )
}

/**
 * Chip de filtre actif
 */
@Composable
fun FilterChip(
    category: String,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.FilterList,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = category,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Retirer filtre",
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Liste des questions
 */
@Composable
fun QuestionsList(
    questions: List<Question>,
    onEditClick: (Question) -> Unit,
    onDeleteClick: (Question) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(questions) { question ->
            QuestionCard(
                question = question,
                onEditClick = { onEditClick(question) },
                onDeleteClick = { onDeleteClick(question) }
            )
        }
    }
}

/**
 * Card d'une question
 */
@Composable
fun QuestionCard(
    question: Question,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // En-tête avec catégorie et difficulté
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row {
                    // Badge catégorie
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = question.category,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    // Badge difficulté
                    Surface(
                        color = when (question.difficulty) {
                            Difficulty.EASY -> MaterialTheme.colorScheme.tertiaryContainer
                            Difficulty.MEDIUM -> MaterialTheme.colorScheme.secondaryContainer
                            Difficulty.HARD -> MaterialTheme.colorScheme.errorContainer
                        },
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = question.difficulty.displayName,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
                
                // Actions
                Row {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            Icons.Filled.Edit,
                            contentDescription = "Modifier",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Supprimer",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Question
            Text(
                text = question.question,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Réponse correcte
            Row(
                modifier = Modifier.padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = question.correctAnswer,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Temps limite (si défini)
            question.timeLimit?.let { timeLimit ->
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Timer,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Temps limite: ${timeLimit}s",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

/**
 * État vide
 */
@Composable
fun EmptyState(
    onAddClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.QuestionMark,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Aucune question",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Ajoutez votre première question",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onAddClick) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Ajouter une question")
        }
    }
}

/**
 * Dialogue de confirmation de suppression
 */
@Composable
fun DeleteConfirmDialog(
    question: Question,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Supprimer la question ?") },
        text = { 
            Text("Êtes-vous sûr de vouloir supprimer cette question ? Cette action est irréversible.") 
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Supprimer", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

/**
 * Dialogue de filtre par catégorie
 */
@Composable
fun FilterDialog(
    selectedCategory: String,
    onDismiss: () -> Unit,
    onCategorySelected: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filtrer par catégorie") },
        text = {
            Column {
                (listOf("Toutes") + QuestionCategories.ALL).forEach { category ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCategorySelected(category) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = category == selectedCategory,
                            onClick = { onCategorySelected(category) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(category)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer")
            }
        }
    )
}

