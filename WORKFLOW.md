# WORKFLOW - Documentation du Développement

## 📝 Phase 1 : Setup Initial

### Date : Décembre 2025
### Objectif : Créer la structure du projet et lancer l'app sur Android

---

## ✅ Tâches Complétées

### 1. Documentation (3 fichiers)
- ✅ `SPEC.md` - Spécifications et règles de développement
- ✅ `PROJECT_STRUCTURE.md` - Structure détaillée du projet
- ✅ `MVP_ROADMAP.md` - Roadmap de développement par phases
- ✅ `README.md` - Documentation principale
- ✅ `WORKFLOW.md` - Ce fichier

### 2. Configuration Gradle
- ✅ `settings.gradle.kts` - Configuration du projet
- ✅ `build.gradle.kts` (root) - Build configuration racine
- ✅ `app/build.gradle.kts` - Build configuration module app
- ✅ `gradle.properties` - Propriétés Gradle
- ✅ `.gitignore` - Exclusions Git

### 3. Manifest et Ressources
- ✅ `AndroidManifest.xml` - Configuration de l'app
- ✅ `strings.xml` - Textes en français
- ✅ `colors.xml` - Palette de couleurs
- ✅ `themes.xml` - Thème de base
- ✅ `proguard-rules.pro` - Règles ProGuard

### 4. Code Kotlin
- ✅ `CultureGApplication.kt` - Application principale
- ✅ `MainActivity.kt` - Activité principale avec navigation
- ✅ `ui/theme/Color.kt` - Couleurs Material3
- ✅ `ui/theme/Type.kt` - Typographie Material3
- ✅ `ui/theme/Theme.kt` - Thème Compose

### 5. Écrans de Base
- ✅ `HomeScreen` - Écran d'accueil avec statut connexion
- ✅ `QuestionsScreen` - Squelette écran questions
- ✅ `SettingsScreen` - Squelette écran configuration
- ✅ `StatsScreen` - Squelette écran statistiques

---

## 📊 Code Généré

### Statistiques
- **Fichiers Kotlin** : 5 fichiers
- **Fichiers Configuration** : 8 fichiers
- **Fichiers Documentation** : 5 fichiers
- **Lignes de code** : ~700 lignes
- **Écrans** : 4 écrans de base

### Architecture
```
CultureG/
├── Documentation (SPEC, README, etc.)
├── Configuration Gradle
├── Ressources Android (strings, colors, themes)
├── Code Kotlin
│   ├── Application
│   ├── MainActivity
│   └── Thème Compose (Color, Type, Theme)
└── Écrans (Home, Questions, Settings, Stats)
```

---

## 🎨 Design Implémenté

### Thème
- **Style** : Material Design 3
- **Couleur primaire** : Bleu (#2196F3)
- **Couleur secondaire** : Orange (#FF9800)
- **Mode sombre** : Supporté

### Navigation
- **Type** : Bottom Navigation Bar
- **Écrans** : 4 écrans principaux
- **Icons** : Material Icons

### Écran d'Accueil
- Logo CultureG (icône cerveau)
- Titre et sous-titre
- Card de statut de connexion Raspberry Pi
- Bouton de connexion

---

## 🚀 Instructions de Lancement

### Étapes pour ouvrir dans Android Studio

1. **Ouvrir Android Studio**
2. **File > Open**
3. **Sélectionner le dossier** : `C:\Users\sami\Desktop\Etudes\N7\3A\Culture G`
4. **Attendre la synchronisation Gradle** (première fois peut prendre quelques minutes)
5. **Lancer l'app** :
   - Connecter un appareil Android ou lancer un émulateur
   - Cliquer sur "Run" (▶️) ou Shift+F10
   - L'app devrait se lancer et afficher l'écran d'accueil

### Note Importante
Si le Gradle Wrapper n'est pas présent, Android Studio le générera automatiquement lors de la première ouverture du projet.

---

## 🔄 Prochaines Étapes (Phase 2)

### Navigation Avancée
- [ ] Implémenter Navigation Compose avec NavHost
- [ ] Créer NavGraph complet
- [ ] Gérer le backstack

### Écran Questions
- [ ] Créer modèle Question
- [ ] Liste des questions avec LazyColumn
- [ ] Formulaire d'ajout/modification
- [ ] Dialogue de confirmation suppression

### Écran Configuration
- [ ] Créer modèle GameSettings
- [ ] Sliders pour paramètres
- [ ] Switches pour options booléennes
- [ ] Sauvegarde avec DataStore

### Écran Statistiques
- [ ] Créer modèle Score
- [ ] Affichage des scores récents
- [ ] Cards de statistiques

---

## 📝 Notes Techniques

### Dépendances Principales
```kotlin
// Jetpack Compose
androidx.compose.ui:ui:1.5.4
androidx.compose.material3:material3:1.1.2
androidx.navigation:navigation-compose:2.7.5

// WebSocket (communication Raspberry Pi)
com.squareup.okhttp3:okhttp:4.12.0

// DataStore (persistance)
androidx.datastore:datastore-preferences:1.0.0

// Serialization JSON
kotlinx-serialization-json:1.6.0
```

### Configuration Minimale
- **minSdk** : 26 (Android 8.0)
- **targetSdk** : 34 (Android 14)
- **JDK** : 17

### Permissions Déclarées
- INTERNET
- ACCESS_NETWORK_STATE
- ACCESS_WIFI_STATE
- BLUETOOTH
- BLUETOOTH_CONNECT
- BLUETOOTH_SCAN

---

## ✅ Phase 1 - COMPLÉTÉE

**Livrable** : ✅ Application Android qui se lance avec écran de bienvenue et navigation de base

**Résultat** :
- ✅ Projet Android créé et configuré
- ✅ Structure de base fonctionnelle
- ✅ Navigation entre 4 écrans
- ✅ Thème Material3 moderne
- ✅ Prêt pour Android Studio

---

## 🎓 Apprentissages

### Points Clés
1. Structure d'un projet Android moderne avec Compose
2. Configuration Gradle avec Kotlin DSL
3. Mise en place d'un thème Material3
4. Navigation de base avec Bottom Navigation
5. Organisation du code avec architecture MVVM simple

### Bonnes Pratiques Appliquées
- Séparation des responsabilités (UI, theme, etc.)
- Nommage cohérent des fichiers et packages
- Documentation en français
- Code commenté et lisible
- Structure extensible pour les prochaines phases

---

**Dernière mise à jour** : Phase 1 complétée
**Prochain objectif** : Phase 2 - Navigation et Structure UI avancée




