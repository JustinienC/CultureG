# SPEC - Guide de Développement CultureG App

> **Ce fichier sert de référence centrale pour l'IA pendant tout le développement de l'app mobile CultureG.**
> Il contient les règles, processus, et rappels automatiques à suivre à chaque étape.

---

## 🎯 Mission Principale

Développer **CultureG Mobile App** - Application de configuration pour un jeu de culture générale sur Raspberry Pi avec :
- **Fonctionnalité critique** : Configuration du système (questions, paramètres, scores)
- **Communication** : WiFi/Websocket ou Bluetooth avec Raspberry Pi
- **Interface** : Simple, intuitive, moderne (Jetpack Compose)
- **Contraintes** : Projet école, pas de production, test sur Android Studio

---

## 📋 Description du Système Complet

### Système Raspberry Pi (Hardware - non développé ici)
- Raspberry Pi avec écran (affichage questions/scores)
- Base de données locale (questions/réponses/scores)
- GPIO connecté à :
  - LEDs (retour visuel)
  - Vibreur (mauvaise réponse)
  - Haut-parleur (bonne réponse)
- Boutons de démarrage
- Micro (optionnel)
- Batterie

### Application Mobile (À développer)
- **Configuration** : Modifier paramètres du jeu
- **Gestion Questions** : Ajouter/Modifier/Supprimer questions
- **Mode Chronomètre** : Configurer temps de réponse
- **Statistiques** : Voir scores et historique
- **Communication** : Envoyer configs vers Raspberry Pi

---

## 📝 Processus Automatique à Chaque Action

### ✅ Avant CHAQUE génération de code

1. **Vérifier le contexte**
   - [ ] Consulter SPEC.md pour les décisions
   - [ ] Consulter MVP_ROADMAP.md pour l'étape actuelle
   - [ ] Vérifier scope du MVP

2. **Confirmer les contraintes**
   - [ ] Projet école (pas de complexité excessive)
   - [ ] Compatibilité Android 8.0+ (API 26+)
   - [ ] Communication Raspberry Pi (WiFi/BT)
   - [ ] Design simple et moderne
   - [ ] Langue : français

3. **Planifier**
   - [ ] Identifier les dépendances nécessaires
   - [ ] Lister les fichiers à créer/modifier
   - [ ] Prévoir les tests si nécessaire

### ✅ Pendant la génération de code

1. **Standards de code**
   - [ ] Kotlin idiomatique
   - [ ] Coroutines + Flow pour async
   - [ ] Jetpack Compose pour UI
   - [ ] MVVM simple
   - [ ] Comments en français
   - [ ] KDoc pour fonctions publiques

2. **Conventions de nommage**
   - [ ] Classes : `PascalCase`
   - [ ] Fonctions : `camelCase`
   - [ ] Constants : `UPPER_SNAKE_CASE`
   - [ ] Packages : `lowercase`

3. **Structure fichiers**
   - [ ] Suivre PROJECT_STRUCTURE.md
   - [ ] Garder la structure simple

### ✅ Après CHAQUE génération de code

1. **Documentation**
   - [ ] Mettre à jour WORKFLOW.md
   - [ ] Documenter le code généré
   - [ ] Noter les problèmes rencontrés

2. **Tests**
   - [ ] Vérifier que ça compile
   - [ ] Tester sur Android Studio
   - [ ] Vérifier les imports et dépendances

3. **Git**
   - [ ] Suggérer un commit avec message conventionnel
   - [ ] Format : `<type>(<scope>): <description>`

4. **Checklist**
   - [ ] Mettre à jour les checkboxes dans MVP_ROADMAP.md
   - [ ] Identifier les tâches suivantes

---

## 📁 Fichiers de Référence

### Priorité 1 (Consulter SYSTÉMATIQUEMENT)
1. **SPEC.md** (ce fichier) - Processus et règles
2. **PROJECT_STRUCTURE.md** - Structure technique
3. **MVP_ROADMAP.md** - Planning et tâches

### Priorité 2 (Selon besoin)
4. **ARCHITECTURE.md** - Architecture détaillée
5. **API_SPEC.md** - Spécification API Raspberry Pi
6. **WORKFLOW.md** - Documentation du développement

---

## 🚨 Règles Strictes

### Simplicité
- ✅ **Toujours** privilégier la solution la plus simple
- ✅ **Pas de over-engineering** (c'est un projet école)
- ✅ **Focus** sur les fonctionnalités essentielles

### Performance
- ✅ Interface fluide et réactive
- ✅ Communication stable avec Raspberry Pi
- ✅ Gestion des erreurs réseau

### Scope MVP
- ✅ Configuration des paramètres du jeu
- ✅ Gestion des questions (CRUD)
- ✅ Communication Raspberry Pi (WiFi ou BT)
- ✅ Interface simple et moderne
- ❌ Pas de features complexes non essentielles

---

## 📅 État Actuel du Projet

### Phase en Cours
**PHASE 1 - SETUP INITIAL** ✅ COMPLÉTÉE

#### Tâches Phase 1
- [x] Création fichiers SPEC
- [x] Création README.md
- [x] Création ARCHITECTURE.md
- [x] Création WORKFLOW.md
- [x] Structure projet Android
- [x] Configuration Gradle
- [x] Thème Material3 complet
- [x] MainActivity avec navigation
- [x] 4 écrans de base
- [ ] Première exécution sur Android Studio (à tester)

**PHASE 2 - EN ATTENTE** 🔲
- Navigation avancée avec NavHost
- Écrans détaillés

---

## 🎨 Standards Spécifiques au Projet

### Design
- **Couleurs** : Moderne (Material Design 3)
- **Style** : Simple, épuré, fonctionnel
- **Langue UI** : Français uniquement
- **Composants** : Jetpack Compose

### Nommage Packages
```
com.cultureg/
├── ui/                     # Interface utilisateur
│   ├── screens/           # Écrans principaux
│   ├── components/        # Composants réutilisables
│   └── theme/             # Thème et styles
├── data/                  # Couche données
│   ├── models/            # Modèles de données
│   ├── repository/        # Repositories
│   └── api/               # Communication Raspberry Pi
├── viewmodel/             # ViewModels
└── utils/                 # Utilitaires
```

### Commits
Format : `<type>(<scope>): <description>`

Exemples :
```
feat(ui): add questions management screen
feat(api): implement websocket connection
fix(network): resolve connection timeout
docs(readme): update setup instructions
```

---

## 📊 Métriques à Suivre

### Code
- Écrans créés : [compteur]
- Fonctionnalités implémentées : [liste]

### Temps
- Temps par phase : [heures]
- Temps total : [heures]

---

## 🎓 Points pour le Projet École

### À Mettre en Avant
1. **Fonctionnalités** : App complète et fonctionnelle
2. **Design** : Interface moderne avec Compose
3. **Communication** : Intégration Raspberry Pi
4. **Code** : Propre et bien structuré
5. **Documentation** : Claire et complète

---

## 🚨 Rappels Importants

### Chaque Fois que je Génère du Code

**JE DOIS** :
1. 📝 Documenter dans WORKFLOW.md
2. ✅ Cocher les tâches dans MVP_ROADMAP.md
3. 🔄 Mettre à jour SPEC.md "État Actuel"
4. 📋 Suggérer un commit message

**JE NE DOIS PAS** :
- ❌ Générer du code trop complexe
- ❌ Oublier de documenter
- ❌ Ignorer les contraintes du projet

---

## 📝 Notes pour Moi (IA)

### Ce Document est Mon Guide

- **Consulter AVANT chaque action**
- **Suivre les processus définis**
- **Documenter systématiquement**
- **Rester simple et efficace**

### Si Doute

1. Consulter SPEC.md (ce fichier)
2. Consulter PROJECT_STRUCTURE.md
3. Demander confirmation à l'utilisateur si vraiment incertain

### Garder en Tête

- 🎯 **Priorité : App fonctionnelle et simple**
- 📝 **Documentation = Important**
- 🎓 **Projet école = Pas de over-engineering**
- 📱 **Test sur Android Studio régulièrement**

---

**Dernière mise à jour** : Phase 1 - Setup Initial
**Statut** : 🚀 EN COURS


