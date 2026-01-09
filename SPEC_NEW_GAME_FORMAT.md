# 📋 SPEC - Nouveau Format de Jeu Question-Réponse

**Date** : Janvier 2026  
**Version** : 2.0  
**Statut** : 📝 En spécification

---

## 🎯 Objectif

Transformer le système de QCM (Question à Choix Multiples) en un système **Question-Réponse** simple où :
- Une question est posée
- L'utilisateur répond avec une réponse textuelle libre
- Une seule réponse correcte est attendue

---

## 📝 Nouveau Format de Question

### Structure de données

**Ancien format (QCM)** :
```kotlin
data class Question(
    val id: String,
    val question: String,
    val answers: List<String>,        // ❌ Supprimé
    val correctAnswerIndex: Int,       // ❌ Supprimé
    val category: String,
    val difficulty: Difficulty,
    val createdAt: Long
)
```

**Nouveau format (Question-Réponse)** :
```kotlin
data class Question(
    val id: String,
    val question: String,
    val correctAnswer: String,         // ✅ Nouvelle propriété
    val category: String,
    val difficulty: Difficulty,
    val createdAt: Long,
    val timeLimit: Int? = null         // ✅ Optionnel : temps limite en secondes
)
```

### Exemple

**Avant (QCM)** :
```json
{
  "id": "q1",
  "question": "Quelle est la capitale de la France ?",
  "answers": ["Paris", "Lyon", "Marseille", "Toulouse"],
  "correctAnswerIndex": 0,
  "category": "Géographie",
  "difficulty": "EASY"
}
```

**Après (Question-Réponse)** :
```json
{
  "id": "q1",
  "question": "Quelle est la capitale de la France ?",
  "correctAnswer": "Paris",
  "category": "Géographie",
  "difficulty": "EASY",
  "timeLimit": 30
}
```

---

## 🎮 Flux de Jeu

### 1. Initialisation
1. L'utilisateur lance le jeu depuis l'application mobile
2. L'app envoie `START_GAME` avec la liste des questions sélectionnées
3. Le serveur Raspberry Pi reçoit la requête et initialise la partie

### 2. Pose de la Question
1. **Raspberry Pi** :
   - Récupère la question suivante
   - Utilise **TTS (Text-To-Speech)** pour lire la question à travers le casque
   - Envoie `CURRENT_QUESTION` à l'app mobile avec :
     - Le texte de la question
     - Le temps limite (si activé)
     - Le numéro de la question
2. **Application Mobile** :
   - Affiche la question à l'écran
   - Affiche un champ de saisie pour la réponse
   - Démarre le chronomètre (si `timeLimit` est défini)

### 3. Saisie de la Réponse
1. L'utilisateur saisit sa réponse dans le champ texte
2. L'utilisateur valide sa réponse (bouton "Valider")
3. L'app envoie `ANSWER_QUESTION` avec la réponse textuelle

### 4. Vérification de la Réponse
1. **Raspberry Pi** :
   - Compare la réponse utilisateur avec `correctAnswer` (insensible à la casse, accents, espaces)
   - Détermine si la réponse est correcte ou incorrecte
   - **Si correcte** :
     - Joue un **son de succès** dans les écouteurs
     - Allume la LED (si disponible)
     - Incrémente le score
   - **Si incorrecte** :
     - Active le **vibreur** (à implémenter)
     - Affiche la réponse correcte
     - N'incrémente pas le score
   - Envoie `ANSWER_RESULT` à l'app avec le résultat

### 5. Passage à la Question Suivante
1. Après un délai (2-3 secondes), le serveur envoie la question suivante
2. Répète les étapes 2-4 jusqu'à épuisement des questions

### 6. Fin de Partie
1. Le serveur envoie `GAME_ENDED` avec :
   - Score final
   - Nombre de bonnes/mauvaises réponses
   - Statistiques détaillées
2. L'app affiche l'écran de résultats

---

## ⏱️ Mode Chronomètre

### Fonctionnement
- Chaque question peut avoir un `timeLimit` (en secondes)
- Si `timeLimit` est défini :
  - Un compte à rebours démarre automatiquement
  - Si le temps expire avant la validation :
    - La réponse est considérée comme incorrecte
    - Le serveur passe automatiquement à la question suivante
    - Le vibreur s'active (timeout = mauvaise réponse)

### Configuration
- Peut être activé/désactivé globalement dans les paramètres
- Peut être défini par question individuellement
- Valeur par défaut : 30 secondes (configurable)

---

## 🔊 Système Audio

### Text-To-Speech (TTS)
- **Raspberry Pi** utilise un moteur TTS pour lire les questions
- Bibliothèque Python recommandée : `pyttsx3` ou `gTTS`
- La question est lue automatiquement quand elle est envoyée

### Son de Succès
- Fichier audio court (ex: "ding.wav", "success.mp3")
- Joué automatiquement quand la réponse est correcte
- Volume configurable dans les paramètres

---

## 📳 Système de Vibration

### Implémentation Future
- Composant vibreur connecté au GPIO de la Raspberry Pi
- Activé automatiquement pour :
  - Réponses incorrectes
  - Timeout (temps écoulé)
- Durée et intensité configurables

**Note** : À implémenter dans une phase ultérieure

---

## 🔄 Migration des Données

### Conversion QCM → Question-Réponse
Pour les questions existantes :
1. Extraire `answers[correctAnswerIndex]` → `correctAnswer`
2. Supprimer `answers` et `correctAnswerIndex`
3. Ajouter `timeLimit` si nécessaire

### Script de Migration
Un script Python sera fourni pour migrer la base de données existante.

---

## 📊 Impact sur les Composants

### Application Android
- ✅ Modifier `Question.kt` (nouveau format)
- ✅ Modifier `QuestionFormDialog.kt` (champ texte au lieu de 4 choix)
- ✅ Modifier `QuestionsScreen.kt` (affichage simplifié)
- ✅ Modifier `QuestionsViewModel.kt` (validation textuelle)
- ✅ Créer écran de jeu avec champ de saisie
- ✅ Implémenter chronomètre visuel

### Serveur Raspberry Pi
- ✅ Modifier schéma SQLite (nouvelle structure)
- ✅ Modifier `server.py` (gestion réponse textuelle)
- ✅ Implémenter TTS pour lecture des questions
- ✅ Implémenter lecture audio pour succès
- ✅ Modifier logique de validation (comparaison textuelle)
- ⏳ Implémenter vibreur (phase ultérieure)

---

## ✅ Critères de Validation

- [ ] Les questions peuvent être créées avec une seule réponse textuelle
- [ ] Les questions sont lues via TTS sur le casque
- [ ] Les réponses textuelles sont acceptées et validées
- [ ] Le son de succès est joué pour les bonnes réponses
- [ ] Le chronomètre fonctionne correctement
- [ ] Le timeout est géré (réponse incorrecte automatique)
- [ ] La migration des données existantes fonctionne

---

**Prochaine étape** : Voir `SPEC_IMPLEMENTATION_PLAN.md` pour le plan d'implémentation détaillé.

