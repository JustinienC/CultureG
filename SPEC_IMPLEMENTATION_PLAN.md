# 🛠️ SPEC - Plan d'Implémentation

**Date** : Janvier 2026  
**Version** : 2.0  
**Statut** : 📝 En spécification

---

## 🎯 Objectif

Planifier l'implémentation du passage du format QCM au format Question-Réponse avec TTS, audio et vibration.

---

## 📋 Phases d'Implémentation

### Phase 1 : Migration du Format de Question ⚡ PRIORITÉ HAUTE

**Objectif** : Changer la structure de données de QCM à Question-Réponse.

#### 1.1 Modèle de Données Android

**Fichier** : `app/src/main/java/com/cultureg/data/models/Question.kt`

**Changements** :
```kotlin
// AVANT
data class Question(
    val id: String,
    val question: String,
    val answers: List<String>,        // ❌ Supprimer
    val correctAnswerIndex: Int,       // ❌ Supprimer
    val category: String,
    val difficulty: Difficulty,
    val createdAt: Long
)

// APRÈS
data class Question(
    val id: String,
    val question: String,
    val correctAnswer: String,         // ✅ Ajouter
    val category: String,
    val difficulty: Difficulty,
    val createdAt: Long,
    val timeLimit: Int? = null         // ✅ Ajouter (optionnel)
)
```

**Méthodes à modifier** :
- ❌ Supprimer `isCorrectAnswer(answerIndex: Int)`
- ❌ Supprimer `getCorrectAnswer()` (remplacé par `correctAnswer`)
- ✅ Ajouter `isCorrectAnswer(answer: String): Boolean` (comparaison textuelle)

**Tâches** :
- [ ] Modifier `Question.kt`
- [ ] Mettre à jour les tests unitaires (si existants)

#### 1.2 Base de Données Raspberry Pi

**Fichier** : `raspberry-pi/server.py` (classe `Database`)

**Changements schéma SQLite** :
```sql
-- AVANT
CREATE TABLE questions (
    id TEXT PRIMARY KEY,
    question TEXT NOT NULL,
    answers TEXT NOT NULL,              -- ❌ Supprimer
    correct_answer_index INTEGER,       -- ❌ Supprimer
    category TEXT,
    difficulty TEXT,
    created_at TIMESTAMP
)

-- APRÈS
CREATE TABLE questions (
    id TEXT PRIMARY KEY,
    question TEXT NOT NULL,
    correct_answer TEXT NOT NULL,       -- ✅ Ajouter
    category TEXT,
    difficulty TEXT,
    time_limit INTEGER,                 -- ✅ Ajouter (optionnel)
    created_at TIMESTAMP
)
```

**Script de migration** :
- [ ] Créer `raspberry-pi/migrate_database.py`
- [ ] Convertir `answers[correctAnswerIndex]` → `correctAnswer`
- [ ] Sauvegarder la base avant migration
- [ ] Tester la migration

**Tâches** :
- [ ] Modifier `init_database()` dans `Database`
- [ ] Modifier `add_question()` pour nouveau format
- [ ] Modifier `get_all_questions()` pour nouveau format
- [ ] Créer et tester le script de migration

#### 1.3 API WebSocket

**Fichier** : `raspberry-pi/server.py` (gestionnaire de messages)

**Messages à modifier** :
- `ADD_QUESTION` : Accepter `correctAnswer` au lieu de `answers` + `correctAnswerIndex`
- `CURRENT_QUESTION` : Envoyer `correctAnswer` (caché côté app) et `timeLimit`
- `ANSWER_QUESTION` : Accepter `answer` (string) au lieu de `answerIndex`

**Tâches** :
- [ ] Modifier handler `ADD_QUESTION`
- [ ] Modifier handler `ANSWER_QUESTION` (comparaison textuelle)
- [ ] Modifier `send_next_question()` pour inclure `timeLimit`
- [ ] Mettre à jour la documentation API

---

### Phase 2 : Interface Utilisateur Android ⚡ PRIORITÉ HAUTE

#### 2.1 Formulaire de Question

**Fichier** : `app/src/main/java/com/cultureg/ui/screens/QuestionFormDialog.kt`

**Changements** :
- ❌ Supprimer les 4 champs `answer1`, `answer2`, `answer3`, `answer4`
- ❌ Supprimer le sélecteur `correctAnswerIndex`
- ✅ Ajouter un seul champ texte pour `correctAnswer`
- ✅ Ajouter un champ optionnel pour `timeLimit`

**Tâches** :
- [ ] Modifier le formulaire (1 champ réponse au lieu de 4)
- [ ] Ajouter champ `timeLimit` (optionnel, avec toggle)
- [ ] Mettre à jour la validation
- [ ] Tester la création/édition de questions

#### 2.2 Affichage des Questions

**Fichier** : `app/src/main/java/com/cultureg/ui/screens/QuestionsScreen.kt`

**Changements** :
- ❌ Supprimer l'affichage de la liste des réponses
- ✅ Afficher uniquement la question et la réponse correcte
- ✅ Afficher `timeLimit` si défini

**Tâches** :
- [ ] Modifier `QuestionCard` pour nouveau format
- [ ] Tester l'affichage

#### 2.3 Écran de Jeu (Nouveau)

**Fichier** : `app/src/main/java/com/cultureg/ui/screens/GameScreen.kt` (à créer)

**Fonctionnalités** :
- Affichage de la question
- Champ de saisie texte pour la réponse
- Bouton "Valider"
- Chronomètre visuel (si `timeLimit` défini)
- Affichage du résultat (correct/incorrect)
- Score et progression

**Tâches** :
- [ ] Créer `GameScreen.kt`
- [ ] Implémenter l'interface de jeu
- [ ] Intégrer le chronomètre
- [ ] Connecter à `RaspberryPiApi`
- [ ] Tester le flux complet

---

### Phase 3 : Logique de Validation ⚡ PRIORITÉ HAUTE

#### 3.1 Comparaison Textuelle (Raspberry Pi)

**Fichier** : `raspberry-pi/server.py`

**Fonction à créer** :
```python
def normalize_answer(text: str) -> str:
    """Normalise une réponse pour comparaison"""
    # Minuscules
    text = text.lower()
    # Supprimer accents (utiliser unidecode)
    text = unidecode(text)
    # Supprimer espaces multiples
    text = ' '.join(text.split())
    return text.strip()

def is_answer_correct(user_answer: str, correct_answer: str) -> bool:
    """Compare deux réponses (insensible à casse/accents)"""
    return normalize_answer(user_answer) == normalize_answer(correct_answer)
```

**Tâches** :
- [ ] Installer `unidecode` (pour gestion accents)
- [ ] Implémenter `normalize_answer()`
- [ ] Implémenter `is_answer_correct()`
- [ ] Tester avec différents cas (accents, casse, espaces)
- [ ] Intégrer dans `handle_answer()`

#### 3.2 Validation Côté Android (Optionnel)

**Fichier** : `app/src/main/java/com/cultureg/viewmodel/QuestionsViewModel.kt`

**Changements** :
- Modifier `validateQuestion()` pour valider le nouveau format
- Vérifier que `correctAnswer` n'est pas vide

**Tâches** :
- [ ] Mettre à jour la validation
- [ ] Tester

---

### Phase 4 : Text-To-Speech (TTS) ⚡ PRIORITÉ HAUTE

**Fichier** : `raspberry-pi/server.py`

**Dépendances** :
```bash
pip install pyttsx3
```

**Fonction à créer** :
```python
def speak_question(question_text: str):
    """Lit une question via TTS"""
    try:
        engine = pyttsx3.init()
        engine.setProperty('rate', 150)  # Configurable
        engine.setProperty('volume', 0.8)  # Configurable
        engine.say(question_text)
        engine.runAndWait()
    except Exception as e:
        logger.error(f"Erreur TTS: {e}")
```

**Intégration** :
- Appeler `speak_question()` après l'envoi de `CURRENT_QUESTION`

**Tâches** :
- [ ] Installer `pyttsx3`
- [ ] Implémenter `speak_question()`
- [ ] Ajouter configuration (vitesse, volume)
- [ ] Tester la lecture de questions
- [ ] Intégrer dans le flux de jeu

---

### Phase 5 : Son de Succès ⚡ PRIORITÉ HAUTE

**Fichier** : `raspberry-pi/server.py`

**Dépendances** :
```bash
pip install pygame
```

**Fichier audio** :
- Créer/récupérer `raspberry-pi/audio/success.wav`

**Fonction à créer** :
```python
def play_success_sound():
    """Joue le son de succès"""
    try:
        pygame.mixer.init()
        sound = pygame.mixer.Sound("audio/success.wav")
        sound.set_volume(0.7)  # Configurable
        sound.play()
    except Exception as e:
        logger.error(f"Erreur lecture son: {e}")
```

**Intégration** :
- Appeler `play_success_sound()` quand `is_answer_correct() == True`

**Tâches** :
- [ ] Installer `pygame`
- [ ] Créer/récupérer fichier audio `success.wav`
- [ ] Implémenter `play_success_sound()`
- [ ] Tester la lecture du son
- [ ] Intégrer dans la validation de réponse

---

### Phase 6 : Chronomètre ⚡ PRIORITÉ MOYENNE

#### 6.1 Côté Android

**Fichier** : `app/src/main/java/com/cultureg/ui/screens/GameScreen.kt`

**Fonctionnalités** :
- Compte à rebours visuel
- Envoi automatique de timeout si temps écoulé
- Désactivation si `timeLimit` est `null`

**Tâches** :
- [ ] Implémenter `CountdownTimer` composable
- [ ] Gérer l'expiration du temps
- [ ] Envoyer `ANSWER_QUESTION` avec `timeout: true`
- [ ] Tester

#### 6.2 Côté Raspberry Pi

**Fichier** : `raspberry-pi/server.py`

**Fonctionnalités** :
- Détection de timeout côté serveur (sécurité)
- Traitement du timeout comme réponse incorrecte

**Tâches** :
- [ ] Implémenter détection timeout serveur
- [ ] Traiter timeout comme réponse incorrecte
- [ ] Activer vibreur sur timeout
- [ ] Tester

---

### Phase 7 : Reconnaissance Vocale ⚡ PRIORITÉ HAUTE

**Fichier** : `raspberry-pi/server.py`

**Dépendances** :
```bash
pip install SpeechRecognition pyaudio
```

**Fonctionnalités** :
- Configuration du bouton d'enregistrement (GPIO 16)
- Enregistrement audio depuis le micro
- Gestion du bouton (démarrage/arrêt enregistrement)
- Reconnaissance vocale (Speech-to-Text)
- Intégration dans le flux de jeu

**Tâches** :
- [ ] Installer `SpeechRecognition` et `pyaudio`
- [ ] Configurer GPIO 16 pour le bouton d'enregistrement
- [ ] Implémenter fonction `record_answer()`
- [ ] Implémenter gestion du bouton (callback)
- [ ] Intégrer dans `wait_for_answer()`
- [ ] Tester l'enregistrement et la reconnaissance
- [ ] Gérer les erreurs (timeout, reconnaissance échouée)

---

### Phase 8 : Vibration ⏳ PRIORITÉ BASSE (Phase Ultérieure)

**Fichier** : `raspberry-pi/server.py`

**Dépendances** :
- Matériel : Vibreur connecté au GPIO 23
- Code : Déjà préparé dans `GPIOController`

**Fonction à créer** :
```python
def activate_vibrator(duration: float = 0.5, intensity: float = 0.5):
    """Active le vibreur"""
    if not GPIO_AVAILABLE:
        logger.warning("GPIO non disponible - Simulation")
        return
    
    try:
        pwm = GPIO.PWM(VIBRATOR_PIN, 100)
        pwm.start(int(intensity * 100))
        time.sleep(duration)
        pwm.stop()
        GPIO.output(VIBRATOR_PIN, GPIO.LOW)
    except Exception as e:
        logger.error(f"Erreur vibreur: {e}")
```

**Intégration** :
- Appeler `activate_vibrator()` quand `is_answer_correct() == False`
- Appeler `activate_vibrator()` sur timeout

**Tâches** :
- [ ] Connecter le vibreur au GPIO 23
- [ ] Implémenter `activate_vibrator()`
- [ ] Tester différents patterns
- [ ] Intégrer dans la validation
- [ ] Configurer intensité et durée

---

## 📅 Ordre d'Implémentation Recommandé

1. **Phase 1** : Migration format (Android + Raspberry Pi)
2. **Phase 2** : Interface utilisateur Android
3. **Phase 3** : Logique de validation
4. **Phase 4** : TTS
5. **Phase 5** : Son de succès
6. **Phase 6** : Chronomètre
7. **Phase 7** : Reconnaissance vocale (bouton + micro)
8. **Phase 8** : Vibration (plus tard)

---

## 🧪 Tests à Effectuer

### Tests Unitaires
- [ ] Normalisation de réponse (accents, casse, espaces)
- [ ] Comparaison de réponses
- [ ] Validation de questions

### Tests d'Intégration
- [ ] Création de question (app → serveur)
- [ ] Démarrage de partie
- [ ] Réponse correcte (son + LED)
- [ ] Réponse incorrecte (vibration)
- [ ] Timeout (vibration automatique)
- [ ] Fin de partie

### Tests Matériel
- [ ] TTS fonctionne sur casque
- [ ] Son de succès joué correctement
- [ ] LED s'allume sur bonne réponse
- [ ] Vibreur s'active sur mauvaise réponse
- [ ] Chronomètre visuel synchronisé

---

## 📝 Checklist Globale

### Modèle de Données
- [ ] `Question.kt` modifié
- [ ] Schéma SQLite modifié
- [ ] Script de migration créé et testé

### Interface Android
- [ ] Formulaire de question modifié
- [ ] Affichage des questions modifié
- [ ] Écran de jeu créé
- [ ] Chronomètre implémenté

### Serveur Raspberry Pi
- [ ] Validation textuelle implémentée
- [ ] TTS intégré
- [ ] Son de succès intégré
- [ ] Reconnaissance vocale intégrée (bouton + micro)
- [ ] Gestion timeout implémentée
- [ ] Vibration intégrée (phase ultérieure)

### Documentation
- [ ] API WebSocket mise à jour
- [ ] Guide de test mis à jour
- [ ] README mis à jour

---

**Prochaine étape** : Commencer par la Phase 1 (Migration du format de question).

