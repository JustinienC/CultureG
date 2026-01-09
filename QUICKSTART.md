# 🚀 Guide de Démarrage Rapide - CultureG

## ✅ Prérequis

### Logiciels Nécessaires
1. **Android Studio** (version recommandée : Hedgehog 2023.1.1 ou plus récent)
   - Télécharger : https://developer.android.com/studio
   
2. **JDK 17** (inclus avec Android Studio)

3. **Android SDK** (API Level 26+)

## 📱 Lancer l'Application

### Méthode 1 : Ouvrir dans Android Studio (Recommandé)

1. **Ouvrir Android Studio**

2. **Importer le Projet**
   - File > Open
   - Naviguer vers : `C:\Users\sami\Desktop\Etudes\N7\3A\Culture G`
   - Cliquer sur "OK"

3. **Attendre la Synchronisation Gradle**
   - Android Studio va automatiquement :
     - Télécharger Gradle si nécessaire
     - Synchroniser les dépendances
     - Indexer le projet
   - ⏱️ Première synchronisation : 2-5 minutes

4. **Configurer un Device**
   
   **Option A : Émulateur (plus facile)**
   - Tools > Device Manager
   - Create Device
   - Sélectionner un modèle (ex: Pixel 6)
   - Choisir API Level 34 (Android 14)
   - Finish
   - Lancer l'émulateur (▶️)

   **Option B : Device Physique**
   - Activer "Mode Développeur" sur votre Android :
     - Paramètres > À propos du téléphone
     - Taper 7 fois sur "Numéro de build"
   - Activer "Débogage USB"
   - Connecter via USB
   - Accepter l'autorisation de débogage

5. **Lancer l'Application**
   - Cliquer sur le bouton ▶️ (Run 'app')
   - Ou : Shift + F10 (Windows/Linux)
   - Ou : Control + R (Mac)

6. **Première Compilation**
   - ⏱️ Durée : 1-3 minutes
   - L'app devrait se lancer automatiquement sur votre device/émulateur

## 🎉 Résultat Attendu

Une fois lancée, l'application devrait afficher :

1. **Écran d'Accueil** avec :
   - Logo CultureG (icône cerveau)
   - Titre "CultureG - Jeu de Culture Générale"
   - Card de statut de connexion (Déconnecté)
   - Bouton "Connecter au Raspberry Pi"

2. **Navigation en bas** avec 4 onglets :
   - 🏠 Accueil
   - ❓ Questions
   - ⚙️ Config
   - 📊 Stats

3. **Thème** :
   - Couleur primaire bleue (#2196F3)
   - Design Material3 moderne
   - Navigation fluide

## 🐛 Problèmes Courants

### 1. Gradle Sync Failed

**Solution** :
```
File > Invalidate Caches > Invalidate and Restart
```

### 2. SDK Not Found

**Solution** :
```
File > Project Structure > SDK Location
Vérifier que Android SDK est installé
```

### 3. Device Not Found

**Solution** :
- Vérifier que l'émulateur est lancé
- Ou que le device physique est bien connecté et autorisé

### 4. Build Failed - Missing Dependencies

**Solution** :
```
Tools > SDK Manager > SDK Tools
Installer :
- Android SDK Build-Tools
- Android SDK Platform-Tools
- Android Emulator
```

### 5. Erreur Kotlin Version

**Solution** :
Le projet utilise Kotlin 1.9.10 et Gradle 8.2.
Si erreur, vérifier `build.gradle.kts` :
```kotlin
plugins {
    id("org.jetbrains.kotlin.android") version "1.9.10"
}
```

## 📦 Structure du Projet dans Android Studio

```
CultureG
├── app/
│   ├── manifests/
│   │   └── AndroidManifest.xml
│   ├── java/
│   │   └── com.cultureg/
│   │       ├── CultureGApplication.kt
│   │       └── ui/
│   │           ├── MainActivity.kt
│   │           └── theme/
│   │               ├── Color.kt
│   │               ├── Type.kt
│   │               └── Theme.kt
│   └── res/
│       ├── values/
│       │   ├── strings.xml
│       │   ├── colors.xml
│       │   └── themes.xml
│       └── mipmap/
│
├── build.gradle.kts (Project)
└── app/build.gradle.kts (Module)
```

## 🔧 Configuration Recommandée Android Studio

### 1. Activer Auto-Import
```
File > Settings > Editor > General > Auto Import
☑ Add unambiguous imports on the fly
☑ Optimize imports on the fly
```

### 2. Formattage Code
```
File > Settings > Editor > Code Style > Kotlin
Scheme : Android Kotlin Style Guide
```

### 3. Gradle JVM
```
File > Settings > Build, Execution, Deployment > Build Tools > Gradle
Gradle JVM : Project JDK (17)
```

## 🎨 Tester les Fonctionnalités

### Navigation
1. Lancer l'app
2. Taper sur les différents onglets en bas
3. Vérifier que chaque écran s'affiche

### Écran Accueil
- Vérifier l'affichage du logo
- Vérifier le statut "Déconnecté"
- Taper sur "Connecter au Raspberry Pi" (rien ne se passe pour l'instant)

### Thème
- Activer/Désactiver le mode sombre du device
- L'app devrait s'adapter automatiquement

## 📝 Prochaines Étapes

Une fois l'app lancée avec succès :

1. **Phase 2** : Navigation avancée
2. **Phase 3** : Gestion des questions
3. **Phase 4** : Configuration du jeu
4. **Phase 5** : Communication Raspberry Pi

## 🆘 Besoin d'Aide ?

### Documentation Officielle
- Android Studio : https://developer.android.com/studio/intro
- Jetpack Compose : https://developer.android.com/jetpack/compose
- Kotlin : https://kotlinlang.org/docs/home.html

### Fichiers du Projet
- `SPEC.md` - Spécifications complètes
- `ARCHITECTURE.md` - Architecture détaillée
- `PROJECT_STRUCTURE.md` - Structure du code
- `MVP_ROADMAP.md` - Planning de développement
- `WORKFLOW.md` - Documentation du développement

---

**Bon développement ! 🚀**



