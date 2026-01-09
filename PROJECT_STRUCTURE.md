# Structure du Projet CultureG Mobile App

## 📁 Arborescence Complète

```
CultureG/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml
│   │   │   ├── java/com/cultureg/
│   │   │   │   │
│   │   │   │   ├── CultureGApplication.kt        # Application principale
│   │   │   │   │
│   │   │   │   ├── ui/                          # Interface Utilisateur
│   │   │   │   │   ├── MainActivity.kt          # Activité principale
│   │   │   │   │   │
│   │   │   │   │   ├── screens/                 # Écrans
│   │   │   │   │   │   ├── HomeScreen.kt        # Écran d'accueil
│   │   │   │   │   │   ├── QuestionsScreen.kt   # Gestion questions
│   │   │   │   │   │   ├── SettingsScreen.kt    # Configuration jeu
│   │   │   │   │   │   ├── StatsScreen.kt       # Statistiques
│   │   │   │   │   │   └── ConnectionScreen.kt  # Connexion Raspberry Pi
│   │   │   │   │   │
│   │   │   │   │   ├── components/              # Composants réutilisables
│   │   │   │   │   │   ├── QuestionCard.kt
│   │   │   │   │   │   ├── SettingItem.kt
│   │   │   │   │   │   ├── ConnectionStatus.kt
│   │   │   │   │   │   └── StatCard.kt
│   │   │   │   │   │
│   │   │   │   │   ├── navigation/              # Navigation
│   │   │   │   │   │   └── NavGraph.kt
│   │   │   │   │   │
│   │   │   │   │   └── theme/                   # Thème
│   │   │   │   │       ├── Theme.kt
│   │   │   │   │       ├── Color.kt
│   │   │   │   │       └── Type.kt
│   │   │   │   │
│   │   │   │   ├── data/                        # Couche Données
│   │   │   │   │   │
│   │   │   │   │   ├── models/                  # Modèles de données
│   │   │   │   │   │   ├── Question.kt          # Modèle Question
│   │   │   │   │   │   ├── GameSettings.kt      # Paramètres jeu
│   │   │   │   │   │   ├── Score.kt             # Score
│   │   │   │   │   │   └── ConnectionStatus.kt  # Statut connexion
│   │   │   │   │   │
│   │   │   │   │   ├── api/                     # Communication Raspberry Pi
│   │   │   │   │   │   ├── RaspberryPiApi.kt    # Interface API
│   │   │   │   │   │   ├── WebSocketClient.kt   # Client WebSocket
│   │   │   │   │   │   ├── BluetoothClient.kt   # Client Bluetooth
│   │   │   │   │   │   └── ApiModels.kt         # Modèles API
│   │   │   │   │   │
│   │   │   │   │   └── repository/              # Repositories
│   │   │   │   │       ├── QuestionsRepository.kt
│   │   │   │   │       ├── SettingsRepository.kt
│   │   │   │   │       └── StatsRepository.kt
│   │   │   │   │
│   │   │   │   ├── viewmodel/                   # ViewModels
│   │   │   │   │   ├── QuestionsViewModel.kt
│   │   │   │   │   ├── SettingsViewModel.kt
│   │   │   │   │   ├── StatsViewModel.kt
│   │   │   │   │   └── ConnectionViewModel.kt
│   │   │   │   │
│   │   │   │   └── utils/                       # Utilitaires
│   │   │   │       ├── Constants.kt             # Constantes
│   │   │   │       ├── NetworkUtils.kt          # Utilitaires réseau
│   │   │   │       └── Extensions.kt            # Extensions Kotlin
│   │   │   │
│   │   │   └── res/                             # Ressources
│   │   │       ├── drawable/                    # Images et icônes
│   │   │       ├── values/
│   │   │       │   ├── strings.xml              # Textes français
│   │   │       │   ├── colors.xml
│   │   │       │   └── themes.xml
│   │   │       └── mipmap/                      # Icône app
│   │   │
│   │   └── test/                                # Tests unitaires (optionnel)
│   │       └── java/com/cultureg/
│   │
│   └── build.gradle.kts                         # Configuration Gradle App
│
├── docs/                                        # Documentation
│   └── screenshots/                             # Screenshots
│
├── SPEC.md                                      # Spécifications (ce fichier)
├── PROJECT_STRUCTURE.md                         # Structure projet (ce fichier)
├── MVP_ROADMAP.md                              # Roadmap MVP
├── ARCHITECTURE.md                             # Architecture détaillée
├── API_SPEC.md                                 # Spécification API
├── WORKFLOW.md                                 # Workflow développement
├── README.md                                   # Documentation principale
│
├── build.gradle.kts                            # Configuration Gradle racine
├── settings.gradle.kts                         # Settings Gradle
├── gradle.properties                           # Propriétés Gradle
├── gradlew                                     # Gradle Wrapper Unix
├── gradlew.bat                                 # Gradle Wrapper Windows
│
└── .gitignore                                  # Git ignore
```

## 📦 Dépendances Principales

### build.gradle.kts (Module App)

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.cultureg"
    compileSdk = 34
    
    defaultConfig {
        applicationId = "com.cultureg"
        minSdk = 26 // Android 8.0
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }
    
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }
    
    buildFeatures {
        compose = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }
}

dependencies {
    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.10")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // AndroidX Core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.1")
    
    // Jetpack Compose
    val composeVersion = "1.5.4"
    implementation("androidx.compose.ui:ui:$composeVersion")
    implementation("androidx.compose.material3:material3:1.1.2")
    implementation("androidx.compose.ui:ui-tooling-preview:$composeVersion")
    implementation("androidx.compose.material:material-icons-extended:$composeVersion")
    implementation("androidx.navigation:navigation-compose:2.7.5")
    debugImplementation("androidx.compose.ui:ui-tooling:$composeVersion")
    
    // WebSocket
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    
    // Bluetooth (si nécessaire)
    // implementation("androidx.bluetooth:bluetooth:1.0.0-alpha01")
    
    // Serialization JSON
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    
    // DataStore (pour sauvegarder préférences)
    implementation("androidx.datastore:datastore-preferences:1.0.0")
}
```

## 🔑 Fichiers de Configuration Importants

### AndroidManifest.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- Permissions -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
    <uses-permission android:name="android.permission.BLUETOOTH" />
    <uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
    <uses-permission android:name="android.permission.BLUETOOTH_SCAN" />

    <application
        android:name=".CultureGApplication"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.CultureG"
        android:usesCleartextTraffic="true">
        
        <!-- Main Activity -->
        <activity
            android:name=".ui.MainActivity"
            android:exported="true"
            android:theme="@style/Theme.CultureG">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
        
    </application>

</manifest>
```

## 📱 Écrans Principaux

### 1. HomeScreen
- Vue d'ensemble du système
- Statut de connexion Raspberry Pi
- Accès rapide aux fonctionnalités

### 2. QuestionsScreen
- Liste des questions
- Ajouter/Modifier/Supprimer questions
- Catégories de questions

### 3. SettingsScreen
- Paramètres du jeu
- Mode chronomètre (temps)
- Effets sonores/vibrations
- Difficulté

### 4. StatsScreen
- Statistiques de jeu
- Scores récents
- Historique des parties

### 5. ConnectionScreen
- Configuration connexion Raspberry Pi
- Scan WiFi/Bluetooth
- Test de connexion

## 🎨 Design Guidelines

### Couleurs
- Primary : Bleu moderne (#2196F3)
- Secondary : Orange (#FF9800)
- Background : Blanc/Gris clair
- Surface : Blanc

### Typographie
- Titre : Material3 Display/Headline
- Corps : Material3 Body
- Boutons : Material3 Label

### Composants
- Cards pour afficher les informations
- FAB pour actions principales (ajouter question)
- Bottom Navigation pour navigation principale
- Top AppBar avec titre et actions

---

Cette structure simple et claire permet de développer rapidement une app fonctionnelle et maintenable !

