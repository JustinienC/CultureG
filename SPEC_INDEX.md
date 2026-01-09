# 📚 Index des Fichiers SPEC

**Date** : Janvier 2026  
**Version** : 2.0

---

## 📋 Vue d'Ensemble

Ce document liste tous les fichiers de spécification créés pour la migration du format QCM vers Question-Réponse.

---

## 📄 Fichiers SPEC

### 1. `SPEC_NEW_GAME_FORMAT.md`
**Description** : Définit le nouveau format de question (Question-Réponse au lieu de QCM)

**Contenu** :
- Structure de données (avant/après)
- Exemples de questions
- Flux de jeu simplifié
- Mode chronomètre
- Impact sur les composants

**À lire en premier** ✅

---

### 2. `SPEC_GAME_FLOW.md`
**Description** : Décrit le flux complet d'une partie de jeu en détail

**Contenu** :
- Diagramme de flux
- Phase 1 : Initialisation
- Phase 2 : Boucle de questions
- Phase 3 : Gestion du chronomètre
- Phase 4 : Fin de partie
- Gestion des erreurs
- États du jeu

**À lire pour comprendre le fonctionnement complet** ✅

---

### 3. `SPEC_AUDIO_VIBRATION.md`
**Description** : Spécifie l'implémentation du système audio (TTS + sons) et de vibration

**Contenu** :
- Text-To-Speech (TTS) avec `pyttsx3`
- Son de succès avec `pygame`
- Système de vibration (phase ultérieure)
- Configuration et paramètres
- Gestion des erreurs

**À lire pour implémenter l'audio** ✅

---

### 4. `SPEC_IMPLEMENTATION_PLAN.md`
**Description** : Plan détaillé d'implémentation par phases

**Contenu** :
- Phase 1 : Migration du format (priorité haute)
- Phase 2 : Interface utilisateur Android (priorité haute)
- Phase 3 : Logique de validation (priorité haute)
- Phase 4 : TTS (priorité haute)
- Phase 5 : Son de succès (priorité haute)
- Phase 6 : Chronomètre (priorité moyenne)
- Phase 7 : Vibration (priorité basse - phase ultérieure)
- Checklist globale
- Ordre d'implémentation recommandé

**À lire pour commencer le développement** ✅

---

## 🎯 Ordre de Lecture Recommandé

1. **`SPEC_NEW_GAME_FORMAT.md`** → Comprendre le nouveau format
2. **`SPEC_GAME_FLOW.md`** → Comprendre le flux de jeu
3. **`SPEC_AUDIO_VIBRATION.md`** → Comprendre l'audio/vibration
4. **`SPEC_IMPLEMENTATION_PLAN.md`** → Commencer l'implémentation

---

## 📝 Résumé des Changements

### Format de Question
- ❌ **Avant** : QCM avec 4 réponses + index correct
- ✅ **Après** : Question-Réponse avec 1 réponse textuelle

### Flux de Jeu
- ✅ Question lue via TTS dans le casque
- ✅ Réponse saisie dans un champ texte
- ✅ Son de succès pour bonne réponse
- ✅ Vibration pour mauvaise réponse (phase ultérieure)
- ✅ Chronomètre optionnel

### Composants à Modifier
- **Android** : `Question.kt`, `QuestionFormDialog.kt`, `QuestionsScreen.kt`, nouveau `GameScreen.kt`
- **Raspberry Pi** : Schéma SQLite, `server.py`, TTS, audio, vibration

---

## ✅ Prochaines Étapes

1. Lire tous les fichiers SPEC
2. Valider les spécifications
3. Commencer l'implémentation selon `SPEC_IMPLEMENTATION_PLAN.md`
4. Suivre l'ordre des phases (1 → 2 → 3 → 4 → 5 → 6 → 7)

---

**Tous les fichiers SPEC sont prêts. Vous pouvez commencer l'implémentation !** 🚀

