# MVP ROADMAP - CultureG Mobile App

## 🎯 Objectif MVP

Créer une application mobile Android fonctionnelle pour configurer et gérer un jeu de culture générale sur Raspberry Pi.

**Délai** : À définir (projet école)
**Plateforme** : Android (testable sur Android Studio)
**Language** : Kotlin + Jetpack Compose

---

## 📅 Phases de Développement

### ✅ PHASE 1 : Setup Initial (COMPLÉTÉE)

#### Objectif
Mettre en place la structure du projet et lancer l'app sur Android Studio

#### Tâches
- [x] Créer fichiers SPEC.md
- [x] Créer PROJECT_STRUCTURE.md
- [x] Créer MVP_ROADMAP.md
- [x] Créer README.md
- [x] Créer ARCHITECTURE.md
- [x] Créer WORKFLOW.md
- [x] Créer structure projet Android
- [x] Configurer Gradle
- [x] Créer CultureGApplication
- [x] Créer MainActivity avec navigation
- [x] Implémenter thème Material3 (Color, Type, Theme)
- [x] Créer 4 écrans de base (Home, Questions, Settings, Stats)
- [ ] Tester lancement sur émulateur/device

#### Livrable Phase 1
✅ Application Android qui se lance avec écran de bienvenue et navigation

---

### 🔲 PHASE 2 : Navigation et Structure UI

#### Objectif
Créer la structure de navigation et les écrans principaux vides

#### Tâches
- [ ] Créer Navigation Graph
- [ ] Créer HomeScreen (squelette)
- [ ] Créer QuestionsScreen (squelette)
- [ ] Créer SettingsScreen (squelette)
- [ ] Créer StatsScreen (squelette)
- [ ] Créer ConnectionScreen (squelette)
- [ ] Implémenter Bottom Navigation
- [ ] Configurer thème Material3

#### Livrable Phase 2
✅ Navigation fonctionnelle entre tous les écrans

---

### 🔲 PHASE 3 : Gestion des Questions

#### Objectif
Implémenter la fonctionnalité de gestion des questions (CRUD)

#### Tâches
- [ ] Créer modèle Question
- [ ] Créer QuestionsRepository
- [ ] Créer QuestionsViewModel
- [ ] Interface ajout question
- [ ] Interface modification question
- [ ] Interface suppression question
- [ ] Liste des questions avec recherche
- [ ] Stockage local (DataStore ou fichier)

#### Livrable Phase 3
✅ Gestion complète des questions localement

---

### 🔲 PHASE 4 : Configuration du Jeu

#### Objectif
Implémenter l'écran de configuration des paramètres du jeu

#### Tâches
- [ ] Créer modèle GameSettings
- [ ] Créer SettingsRepository
- [ ] Créer SettingsViewModel
- [ ] Interface paramètres :
  - [ ] Mode chronomètre (temps)
  - [ ] Nombre de questions par partie
  - [ ] Difficulté
  - [ ] Sons activés/désactivés
  - [ ] Vibrations activées/désactivées
- [ ] Sauvegarde des paramètres

#### Livrable Phase 4
✅ Configuration complète des paramètres du jeu

---

### 🔲 PHASE 5 : Communication Raspberry Pi

#### Objectif
Établir la communication avec la Raspberry Pi (WiFi/WebSocket ou Bluetooth)

#### Tâches
- [ ] Créer API_SPEC.md (définir protocole)
- [ ] Implémenter WebSocketClient
- [ ] Implémenter connexion/déconnexion
- [ ] Envoi des questions vers Raspberry Pi
- [ ] Envoi des paramètres vers Raspberry Pi
- [ ] Réception des scores depuis Raspberry Pi
- [ ] Gestion des erreurs de connexion
- [ ] Interface ConnectionScreen
- [ ] Indicateur de statut de connexion

#### Livrable Phase 5
✅ Communication bidirectionnelle fonctionnelle avec Raspberry Pi

---

### 🔲 PHASE 6 : Statistiques et Scores

#### Objectif
Afficher les statistiques et scores des parties

#### Tâches
- [ ] Créer modèle Score
- [ ] Créer StatsRepository
- [ ] Créer StatsViewModel
- [ ] Affichage scores récents
- [ ] Affichage statistiques générales
- [ ] Graphiques (optionnel)
- [ ] Export des données (optionnel)

#### Livrable Phase 6
✅ Affichage complet des statistiques

---

### 🔲 PHASE 7 : Polish et Tests

#### Objectif
Finaliser l'application et tester toutes les fonctionnalités

#### Tâches
- [ ] Améliorer l'UI/UX
- [ ] Ajouter animations
- [ ] Gérer tous les cas d'erreur
- [ ] Tester sur device physique
- [ ] Optimiser performance
- [ ] Ajouter icône app
- [ ] Créer README complet
- [ ] Préparer démo

#### Livrable Phase 7
✅ Application complète, testée et prête pour présentation

---

## 📊 Métriques du Projet

### Écrans
- Total prévu : 5 écrans principaux
- Complétés : 0/5

### Fonctionnalités
- [ ] Gestion questions (CRUD)
- [ ] Configuration jeu
- [ ] Communication Raspberry Pi
- [ ] Affichage statistiques
- [ ] Mode chronomètre

### Code
- Lignes Kotlin : 0
- Fichiers créés : 3 (docs)
- Composables créés : 0

---

## 🎓 Critères de Succès

### Fonctionnalités Essentielles
1. ✅ App se lance sur Android
2. ⬜ Navigation fluide entre écrans
3. ⬜ Ajout/Modification/Suppression de questions
4. ⬜ Configuration des paramètres du jeu
5. ⬜ Communication avec Raspberry Pi
6. ⬜ Affichage des scores

### Qualité
- Code propre et bien structuré
- Interface intuitive et moderne
- Gestion des erreurs
- Documentation complète

### Présentation
- Demo fonctionnelle
- Documentation claire
- Code commenté
- Screenshots

---

## 🚀 Prochaines Étapes

### Maintenant (Phase 1)
1. Créer structure projet Android
2. Configurer Gradle avec dépendances
3. Créer MainActivity basique
4. Lancer sur Android Studio

### Ensuite (Phase 2)
1. Mettre en place navigation
2. Créer squelettes des écrans
3. Implémenter thème

---

**Dernière mise à jour** : Phase 1 en cours
**Statut** : 🚀 DÉMARRAGE


