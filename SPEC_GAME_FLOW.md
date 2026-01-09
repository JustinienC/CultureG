# 🎮 SPEC - Flux de Jeu Détaillé

**Date** : Janvier 2026  
**Version** : 2.1  
**Statut** : 📝 En spécification

---

## 📋 Vue d'Ensemble

Ce document décrit le flux complet d'une partie de CultureG. 

**Architecture** :
- **Configuration** : L'application mobile sert uniquement à ajouter/modifier des questions dans la base de données de la Raspberry Pi
- **Jeu** : Le jeu se déroule entièrement sur la Raspberry Pi (questions lues via casque, réponses via interface matérielle)
- **Statistiques** : Les statistiques sont récupérées depuis l'app mobile après chaque session de jeu

---

## 🔄 Diagramme de Flux Global

### Phase Configuration (App Mobile ↔ Raspberry Pi)
```
[App Mobile]                    [Raspberry Pi]                  [BDD]
     |                                |                              |
     |--- ADD_QUESTION ------------>|                              |
     |                                |-- Stocke dans BDD ---------->[SQLite]
     |<-- QUESTION_ADDED ------------|                              |
     |                                |                              |
     |--- UPDATE_QUESTION ---------->|                              |
     |                                |-- Met à jour BDD ----------->[SQLite]
     |<-- QUESTION_UPDATED ----------|                              |
```

### Phase Jeu (Raspberry Pi ↔ Matériel)
```
[Raspberry Pi]                  [Matériel]                      [BDD]
     |                                |                              |
     |-- Bouton pressé (GPIO 25) -->[Bouton]                        |
     |                                |                              |
     |-- Charge questions depuis BDD ------------------------------>[SQLite]
     |                                |                              |
     |-- TTS: Lit question ---------->[Casque]                       |
     |                                |                              |
     |-- Attend réponse (boutons/voix)                              |
     |                                |                              |
     |<-- Réponse utilisateur --------[Interface]                   |
     |                                |                              |
     |-- Compare réponse              |                              |
     |                                |                              |
     |-- Si correcte:                 |                              |
     |   - Son succès --------------->[Casque]                      |
     |   - LED ON ------------------->[LED]                         |
     |                                |                              |
     |-- Si incorrecte:               |                              |
     |   - Vibreur ON --------------->[Vibreur]                     |
     |                                |                              |
     |-- Sauvegarde résultat dans BDD ----------------------------->[SQLite]
     |                                |                              |
     |-- Question suivante (ou fin)    |                              |
```

### Phase Statistiques (App Mobile ↔ Raspberry Pi)
```
[App Mobile]                    [Raspberry Pi]                  [BDD]
     |                                |                              |
     |--- GET_GAME_STATS ------------>|                              |
     |                                |-- Récupère depuis BDD ------->[SQLite]
     |<-- GAME_STATS ----------------|                              |
     |    {sessions, scores, ...}     |                              |
     |                                |                              |
     | [Affiche statistiques]         |                              |
```

---

## 📱 Phase 1 : Configuration (App Mobile)

### 1.1 Gestion des Questions

**L'application mobile sert uniquement à configurer les questions** qui seront stockées dans la base de données de la Raspberry Pi.

#### Ajouter une Question
**Message envoyé (App → Pi)** :
```json
{
  "type": "ADD_QUESTION",
  "data": {
    "id": "q1",
    "question": "Quelle est la capitale de la France ?",
    "correctAnswer": "Paris",
    "category": "Géographie",
    "difficulty": "EASY",
    "timeLimit": 30
  }
}
```

**Réponse (Pi → App)** :
```json
{
  "type": "QUESTION_ADDED",
  "data": {
    "success": true,
    "questionId": "q1"
  }
}
```

#### Modifier une Question
**Message envoyé (App → Pi)** :
```json
{
  "type": "UPDATE_QUESTION",
  "data": {
    "id": "q1",
    "question": "Quelle est la capitale de la France ?",
    "correctAnswer": "Paris",
    "category": "Géographie",
    "difficulty": "MEDIUM",
    "timeLimit": 45
  }
}
```

#### Récupérer les Questions
**Message envoyé (App → Pi)** :
```json
{
  "type": "GET_QUESTIONS"
}
```

**Réponse (Pi → App)** :
```json
{
  "type": "QUESTIONS_LIST",
  "data": {
    "questions": [
      {
        "id": "q1",
        "question": "Quelle est la capitale de la France ?",
        "correctAnswer": "Paris",
        "category": "Géographie",
        "difficulty": "EASY",
        "timeLimit": 30
      },
      ...
    ]
  }
}
```

**Note** : Les questions sont stockées dans la base de données SQLite de la Raspberry Pi. L'app mobile ne stocke pas les questions localement.

---

## 🎮 Phase 2 : Démarrage du Jeu (Raspberry Pi)

### 2.1 Déclenchement du Jeu

**Le jeu démarre via le bouton physique GPIO 25** sur la Raspberry Pi.

**Flux** :
1. L'utilisateur appuie sur le bouton (GPIO 25)
2. La Raspberry Pi détecte l'appui via callback
3. Le serveur charge toutes les questions depuis la base de données
4. Le jeu commence automatiquement

**Pas de communication avec l'app mobile pendant le jeu.**

### 2.2 Initialisation de la Partie

**Actions serveur** :
1. Récupère toutes les questions depuis la BDD SQLite
2. Mélange les questions (optionnel)
3. Initialise les statistiques de la partie :
   - `gameId` : ID unique de la session
   - `startTime` : Timestamp de début
   - `questions` : Liste des questions
   - `currentIndex` : Index de la question actuelle (0)
   - `score` : Score actuel (0)
   - `correctAnswers` : Nombre de bonnes réponses (0)
   - `wrongAnswers` : Nombre de mauvaises réponses (0)
4. Sauvegarde la session dans la BDD (table `game_sessions`)

---

## 🎯 Phase 3 : Boucle de Questions (Raspberry Pi)

### 3.1 Lecture de la Question

**Actions Raspberry Pi** :
1. Récupère la question suivante depuis la liste
2. **Lit la question via TTS dans le casque** :
   - Utilise `pyttsx3` pour la synthèse vocale
   - Lit le texte de la question
   - Volume et vitesse configurables
3. Enregistre le timestamp de début de la question
4. Démarre le chronomètre (si `timeLimit` défini)

**Exemple** :
```python
question = current_game['questions'][current_index]
speak_question(question['question'])  # Lit via TTS dans le casque
start_time = time.time()
```

### 3.2 Réception de la Réponse (Reconnaissance Vocale)

**Mécanisme de réponse** :
L'utilisateur répond via **reconnaissance vocale** avec un bouton de contrôle.

**Flux détaillé** :
1. **Démarrage de l'enregistrement** :
   - L'utilisateur appuie sur le **bouton d'enregistrement** (GPIO dédié, ex: GPIO 16)
   - La Raspberry Pi démarre l'enregistrement audio depuis le micro
   - Feedback visuel : LED clignote (enregistrement en cours)
   - Feedback audio : Signal sonore court (optionnel)

2. **Enregistrement** :
   - L'utilisateur dit sa réponse à voix haute dans le micro
   - L'audio est enregistré en temps réel
   - Affichage visuel : LED continue de clignoter

3. **Arrêt de l'enregistrement** :
   - L'utilisateur **rappuie sur le bouton** pour arrêter l'enregistrement
   - La Raspberry Pi arrête l'enregistrement
   - Feedback visuel : LED s'arrête de clignoter
   - Feedback audio : Signal sonore court (optionnel)

4. **Traitement** :
   - Conversion Speech-to-Text de l'audio enregistré
   - Utilisation de `speech_recognition` (Google Speech API, ou autre)
   - Récupération du texte de la réponse
   - Si la reconnaissance échoue : nouvelle tentative ou timeout

**Configuration GPIO** :
- **GPIO 25** : Bouton de démarrage du jeu (déjà configuré)
- **GPIO 16** : Bouton d'enregistrement (à ajouter)
- **Micro** : Connecté à l'entrée audio de la Raspberry Pi (USB ou jack)

### 3.3 Validation de la Réponse

**Actions serveur** :
1. Récupère la question actuelle depuis la liste
2. Compare la réponse utilisateur avec `correctAnswer` :
   - Normalisation (minuscules, accents, espaces)
   - Comparaison insensible à la casse/accents
3. Calcule le temps écoulé
4. Met à jour les statistiques
5. Déclenche le feedback matériel
6. Sauvegarde le résultat dans la BDD (table `game_answers`)

### 2.4 Comparaison de Réponse

**Algorithme de comparaison** :
```python
def normalize_answer(text):
    # Convertir en minuscules
    text = text.lower()
    # Supprimer accents
    text = unidecode(text)
    # Supprimer espaces multiples
    text = ' '.join(text.split())
    return text.strip()

def is_answer_correct(user_answer, correct_answer):
    return normalize_answer(user_answer) == normalize_answer(correct_answer)
```

**Tolérances** :
- Insensible à la casse (majuscules/minuscules)
- Insensible aux accents (é = e, à = a, etc.)
- Insensible aux espaces multiples
- Trim automatique des espaces en début/fin

### 2.5 Feedback Matériel

#### Réponse Correcte ✅
1. **Son de succès** :
   - Fichier audio joué dans le casque
   - Durée : ~1-2 secondes
   - Volume : selon configuration
2. **LED** :
   - Allumage pendant 1 seconde
   - Couleur : Vert (si LED RGB) ou simple ON/OFF
3. **Score** :
   - Incrémentation selon difficulté :
     - EASY : +1 point
     - MEDIUM : +2 points
     - HARD : +3 points

#### Réponse Incorrecte ❌
1. **Vibreur** :
   - Activation pendant 0.5-1 seconde
   - Intensité : selon configuration
   - Pattern : 2 vibrations courtes
2. **LED** :
   - Clignotement rouge (si LED RGB) ou OFF
   - Durée : 1 seconde
3. **Score** :
   - Pas d'incrémentation
   - Affichage de la réponse correcte

### 3.4 Feedback Matériel et Sauvegarde

**Actions après validation** :

#### Réponse Correcte ✅
1. **Son de succès** : Joué dans le casque (~1-2 secondes)
2. **LED** : Allumage pendant 1 seconde (GPIO 18)
3. **Score** : Incrémentation selon difficulté
4. **Sauvegarde BDD** :
   ```sql
   INSERT INTO game_answers (game_id, question_id, user_answer, is_correct, time_elapsed)
   VALUES (?, ?, ?, 1, ?)
   ```

#### Réponse Incorrecte ❌
1. **Vibreur** : Activation pendant 0.5-1 seconde (GPIO 23)
2. **LED** : Clignotement ou OFF
3. **Score** : Pas d'incrémentation
4. **Sauvegarde BDD** :
   ```sql
   INSERT INTO game_answers (game_id, question_id, user_answer, is_correct, time_elapsed)
   VALUES (?, ?, ?, 0, ?)
   ```

### 3.5 Passage à la Question Suivante

**Actions** :
1. Attente de 2-3 secondes (pour laisser le temps au feedback)
2. Incrémentation de `currentIndex`
3. Si `currentIndex < len(questions)` :
   - Retour à l'étape 3.1 (lecture question suivante)
4. Sinon :
   - Passage à la Phase 4 (Fin de partie)

---

## ⏱️ Phase 3.5 : Gestion du Chronomètre

### 3.5.1 Démarrage
- Démarre automatiquement quand la question est lue via TTS
- Si `timeLimit` est défini dans la question
- Compte à rebours en secondes

### 3.5.2 Expiration du Temps
**Si le temps expire** :
1. Le serveur détecte le timeout (pas de réponse reçue)
2. La réponse est automatiquement considérée comme incorrecte
3. Le vibreur s'active (timeout = mauvaise réponse)
4. Sauvegarde dans la BDD avec `is_correct = 0` et `timeout = 1`
5. Passage à la question suivante après 2-3 secondes

### 3.5.3 Annulation du Chronomètre
- Si l'utilisateur répond avant l'expiration, le chronomètre s'arrête
- Le temps écoulé est enregistré dans `time_elapsed` (BDD)

---

## 🏁 Phase 4 : Fin de Partie (Raspberry Pi)

### 4.1 Calcul des Statistiques

**Après la dernière question** :
1. Le serveur calcule les statistiques finales :
   - Score final
   - Nombre de bonnes/mauvaises réponses
   - Temps moyen par question
   - Temps total de la partie
   - Liste détaillée de toutes les questions et réponses
2. Met à jour la session dans la BDD :
   ```sql
   UPDATE game_sessions 
   SET end_time = ?, final_score = ?, correct_answers = ?, wrong_answers = ?
   WHERE game_id = ?
   ```
3. **Envoie automatiquement les statistiques à tous les clients WebSocket connectés** (si l'app mobile est connectée)

### 4.2 Envoi Automatique des Statistiques

**Message envoyé automatiquement (Pi → App)** :
```json
{
  "type": "GAME_ENDED",
  "data": {
    "gameId": "game_12345",
    "startTime": "2026-01-15T10:30:00",
    "endTime": "2026-01-15T10:45:00",
    "totalQuestions": 10,
    "correctAnswers": 8,
    "wrongAnswers": 2,
    "finalScore": 8,
    "totalTime": 900.0,
    "averageTime": 90.0,
    "questions": [
      {
        "questionId": "q1",
        "question": "Quelle est la capitale de la France ?",
        "userAnswer": "Paris",
        "correctAnswer": "Paris",
        "isCorrect": true,
        "timeElapsed": 12.5,
        "timeout": false
      },
      ...
    ]
  }
}
```

**Note** : Si aucune app mobile n'est connectée, les statistiques restent dans la BDD et peuvent être récupérées plus tard via `GET_GAME_STATS`.

### 4.3 Retour à l'État IDLE

1. Le jeu retourne à l'état `IDLE`
2. Attente d'un nouvel appui sur le bouton GPIO 25 pour une nouvelle partie
3. Les statistiques sont disponibles dans la BDD pour consultation ultérieure

---

## 📊 Phase 5 : Récupération des Statistiques (App Mobile)

### 5.1 Réception Automatique (Fin de Partie)

**Si l'app mobile est connectée** :
- Les statistiques sont **automatiquement reçues** via le message `GAME_ENDED` (voir Phase 4.2)
- L'app peut afficher les statistiques immédiatement
- Les statistiques sont aussi sauvegardées localement dans l'app (optionnel)

### 5.2 Demande Manuelle de Statistiques (Historique)

**L'utilisateur ouvre l'écran "Statistiques" dans l'app mobile pour voir l'historique.**

**Message envoyé (App → Pi)** :
```json
{
  "type": "GET_GAME_STATS",
  "data": {
    "limit": 10,  // Nombre de sessions à récupérer (optionnel)
    "fromDate": "2026-01-01",  // Date de début (optionnel)
    "toDate": "2026-01-31"  // Date de fin (optionnel)
  }
}
```

**Message reçu (App)** :
```json
{
  "type": "GAME_STATS",
  "data": {
    "sessions": [
      {
        "gameId": "game_12345",
        "startTime": "2026-01-15T10:30:00",
        "endTime": "2026-01-15T10:45:00",
        "totalQuestions": 10,
        "correctAnswers": 8,
        "wrongAnswers": 2,
        "finalScore": 8,
        "totalTime": 900.0,
        "averageTime": 90.0,
        "questions": [
          {
            "questionId": "q1",
            "question": "Quelle est la capitale de la France ?",
            "userAnswer": "Paris",
            "correctAnswer": "Paris",
            "isCorrect": true,
            "timeElapsed": 12.5,
            "timeout": false
          },
          ...
        ]
      },
      ...
    ],
    "summary": {
      "totalSessions": 25,
      "totalQuestions": 250,
      "totalCorrect": 200,
      "totalWrong": 50,
      "averageScore": 8.0,
      "bestScore": 10
    }
  }
}
```

### 5.3 Affichage dans l'App Mobile

**Écran Statistiques** :
- **Session récente** (si reçue automatiquement) :
  - Affichage immédiat des résultats de la dernière partie
  - Score, pourcentage, temps, détails
- **Historique** (via `GET_GAME_STATS`) :
  - Liste des sessions de jeu
  - Pour chaque session :
    - Date/heure
    - Score (X/10)
    - Pourcentage de réussite
    - Temps total
- **Résumé global** :
  - Nombre total de parties
  - Score moyen
  - Meilleur score
  - Graphique de progression (optionnel)
- **Détails d'une session** :
  - Liste des questions posées
  - Réponses données
  - Bonnes/mauvaises réponses
  - Temps par question

---

## 🔧 Gestion des Erreurs

### Erreurs Possibles

1. **Aucune question dans la BDD** :
   - Message d'erreur si tentative de démarrage sans questions
   - L'utilisateur doit d'abord ajouter des questions via l'app mobile

2. **Erreur TTS** :
   - Si TTS échoue : log de l'erreur, passage à la question suivante
   - Pas d'interruption du jeu

3. **Erreur matériel** :
   - Si son échoue : pas de son, mais LED fonctionne
   - Si vibreur échoue : pas de vibration, mais jeu continue
   - Si LED échoue : pas de LED, mais son/vibreur fonctionnent

4. **Erreur reconnaissance vocale** :
   - Si la reconnaissance échoue : nouvelle tentative
   - Si échec répété : timeout après `timeLimit` secondes

5. **Erreur BDD** :
   - Si sauvegarde échoue : log de l'erreur, jeu continue
   - Les statistiques peuvent être perdues, mais le jeu continue

---

## 📊 États du Jeu

### États Serveur (Raspberry Pi)
- `IDLE` : Aucune partie en cours, attente du bouton
- `INITIALIZING` : Initialisation de la partie (chargement questions)
- `QUESTION_ACTIVE` : Question en cours (TTS en cours ou terminé)
- `WAITING_ANSWER` : En attente de réponse (reconnaissance vocale active)
- `PROCESSING_ANSWER` : Traitement de la réponse (comparaison)
- `SHOWING_RESULT` : Affichage du résultat (feedback matériel)
- `GAME_ENDED` : Partie terminée, retour à `IDLE`

### États App Mobile
- `MENU` : Menu principal
- `QUESTIONS` : Gestion des questions (ajout/modification)
- `CONNECTING` : Connexion au serveur
- `STATS` : Affichage des statistiques
- `SETTINGS` : Paramètres du système

**Note** : L'app mobile n'a pas d'état "PLAYING" car le jeu se déroule entièrement sur la Raspberry Pi.

---

## ✅ Clarifications Confirmées

1. **Réponse utilisateur** : ✅
   - L'utilisateur appuie sur un bouton (GPIO dédié) pour démarrer l'enregistrement
   - Il dit sa réponse dans le micro
   - Il rappuie sur le bouton pour arrêter l'enregistrement
   - Reconnaissance vocale (Speech-to-Text) convertit l'audio en texte

2. **Statistiques** : ✅
   - Récupérées **automatiquement** à la fin de chaque partie
   - La Raspberry Pi envoie `GAME_ENDED` avec toutes les statistiques
   - L'app mobile peut aussi demander l'historique via `GET_GAME_STATS`

3. **Démarrage du jeu** : ✅
   - Via le **bouton GPIO 25** sur la Raspberry Pi uniquement
   - Pas de démarrage via l'app mobile

---

**Prochaine étape** : Voir `SPEC_IMPLEMENTATION_PLAN.md` pour le plan d'implémentation.

