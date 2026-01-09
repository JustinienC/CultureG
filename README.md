# 🎮 CultureG - Application Mobile

Application mobile Android pour configurer et gérer un jeu de culture générale sur Raspberry Pi.

## 📱 Description

CultureG est une application mobile qui permet de :
- Gérer des questions de culture générale (ajouter, modifier, supprimer)
- Configurer les paramètres du jeu (chronomètre, difficulté, sons/vibrations)
- Communiquer avec le système Raspberry Pi via WiFi/WebSocket
- Consulter les scores et statistiques des parties

## 🏗️ Architecture Système

```
┌─────────────────────────────┐
│   Application Mobile         │
│   (Android - Kotlin)         │
│                              │
│  - Gestion Questions         │
│  - Configuration             │
│  - Statistiques              │
└──────────┬──────────────────┘
           │
           │ WiFi/WebSocket
           │ ou Bluetooth
           │
┌──────────┴──────────────────┐
│   Raspberry Pi               │
│                              │
│  ┌────────────────────┐     │
│  │  Base de Données   │     │
│  │  (Questions/Scores)│     │
│  └────────────────────┘     │
│                              │
│  ┌────────────────────┐     │
│  │   GPIO             │     │
│  │  - LEDs            │     │
│  │  - Vibreur         │     │
│  │  - Haut-Parleur    │     │
│  │  - Boutons         │     │
│  └────────────────────┘     │
└─────────────────────────────┘
```

## 🚀 Installation et Lancement

### Prérequis
- Android Studio (dernière version)
- JDK 17
- Android SDK (API 26+)
- Un appareil Android ou émulateur

### Étapes
1. Cloner ou ouvrir le projet dans Android Studio
2. Laisser Gradle synchroniser les dépendances
3. Lancer l'application sur un émulateur ou device physique

```bash
# Depuis Android Studio
Run > Run 'app'
# ou
Shift + F10
```

## 📁 Structure du Projet

```
com.cultureg/
├── ui/                     # Interface utilisateur (Compose)
│   ├── screens/           # Écrans principaux
│   ├── components/        # Composants réutilisables
│   ├── navigation/        # Navigation
│   └── theme/             # Thème Material3
├── data/                  # Couche données
│   ├── models/            # Modèles de données
│   ├── api/               # Communication Raspberry Pi
│   └── repository/        # Repositories
├── viewmodel/             # ViewModels (MVVM)
└── utils/                 # Utilitaires
```

## 🎨 Écrans Principaux

### 1. 🏠 Accueil
- Vue d'ensemble du système
- Statut de connexion Raspberry Pi
- Accès rapide aux fonctionnalités

### 2. ❓ Questions
- Liste des questions avec recherche
- Ajout de nouvelles questions
- Modification/Suppression

### 3. ⚙️ Configuration
- Mode chronomètre (temps de réponse)
- Nombre de questions par partie
- Activation sons/vibrations
- Niveau de difficulté

### 4. 📊 Statistiques
- Scores récents
- Historique des parties
- Statistiques générales

### 5. 🔌 Connexion
- Configuration connexion Raspberry Pi
- Scan réseau/Bluetooth
- Test de connexion

## 🔧 Technologies Utilisées

- **Kotlin** - Langage principal
- **Jetpack Compose** - UI moderne et déclarative
- **Material Design 3** - Design system
- **Coroutines & Flow** - Programmation asynchrone
- **MVVM** - Architecture
- **OkHttp/WebSocket** - Communication réseau
- **DataStore** - Persistance locale

## 📡 Communication Raspberry Pi

### Protocole
- **Transport** : WebSocket sur WiFi ou Bluetooth
- **Format** : JSON
- **Direction** : Bidirectionnelle

### Messages Types
```json
// Mobile → Raspberry Pi
{
  "type": "ADD_QUESTION",
  "data": {
    "question": "Quelle est la capitale de la France ?",
    "answers": ["Paris", "Lyon", "Marseille", "Toulouse"],
    "correctAnswer": 0
  }
}

// Raspberry Pi → Mobile
{
  "type": "SCORE_UPDATE",
  "data": {
    "score": 15,
    "totalQuestions": 20
  }
}
```

## 👨‍💻 Développement

### Standards de Code
- Kotlin idiomatique
- Conventions de nommage Android
- Comments en français
- Architecture MVVM simple

### Git Commits
Format : `<type>(<scope>): <description>`

Exemples :
```
feat(questions): add question creation form
fix(connection): resolve websocket timeout
docs(readme): update installation steps
```

## 📝 Documentation

- [SPEC.md](SPEC.md) - Spécifications et règles de développement
- [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md) - Structure détaillée
- [MVP_ROADMAP.md](MVP_ROADMAP.md) - Roadmap et planning
- [API_SPEC.md](API_SPEC.md) - Spécification API (à créer)
- [WORKFLOW.md](WORKFLOW.md) - Workflow de développement (à créer)

## 🎓 Projet École

Ce projet a été développé dans le cadre d'un cours à l'N7.
Il s'agit d'un projet éducatif démontrant :
- Développement mobile Android moderne
- Communication IoT (Raspberry Pi)
- Architecture logicielle propre
- Travail avec l'IA (développement assisté)

## 📄 License

Projet école - N7 3A

---

**Statut** : 🚀 En développement
**Version** : 1.0.0
**Dernière mise à jour** : Décembre 2025



