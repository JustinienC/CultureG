# 🚀 Prochaines Étapes - CultureG

## ✅ Étape Actuelle : CONNEXION RÉUSSIE !

Félicitations ! L'app Android est maintenant connectée au serveur Raspberry Pi. 🎉

---

## 📋 Prochaines Étapes

### 🔄 Étape 1 : Synchroniser les Questions (PRIORITAIRE)

**Objectif** : Envoyer les questions depuis l'app vers le Raspberry Pi et les récupérer.

#### À implémenter :

1. **Modifier QuestionsViewModel** pour envoyer les questions au Raspberry Pi
2. **Récupérer les questions** du Raspberry Pi au démarrage
3. **Synchronisation bidirectionnelle** : App ↔ Raspberry Pi

#### Code à ajouter :

Dans `QuestionsViewModel.kt` :
- Observer la connexion
- Si connecté → envoyer les questions au Raspberry Pi quand elles sont ajoutées
- Charger les questions du Raspberry Pi au démarrage

---

### 🎮 Étape 2 : Tester le Flux de Jeu

**Objectif** : Démarrer une partie depuis l'app et jouer.

#### Fonctionnalités à tester :

1. **Démarrer un jeu** depuis l'app
2. **Recevoir les questions** du Raspberry Pi
3. **Répondre aux questions**
4. **Recevoir les scores** en temps réel
5. **Voir le résultat final**

---

### ⚙️ Étape 3 : Synchroniser les Paramètres

**Objectif** : Envoyer les paramètres du jeu au Raspberry Pi.

#### À implémenter :

1. **Créer SettingsViewModel** (si pas encore fait)
2. **Envoyer les paramètres** au Raspberry Pi quand ils changent
3. **Récupérer les paramètres** du Raspberry Pi au démarrage

---

### 📊 Étape 4 : Afficher les Statistiques

**Objectif** : Récupérer et afficher les scores depuis le Raspberry Pi.

#### À implémenter :

1. **Créer StatsViewModel**
2. **Récupérer les scores** du Raspberry Pi
3. **Afficher les statistiques** dans l'écran Stats

---

## 🧪 Tests à Faire Maintenant

### Test 1 : Ping/Pong

1. Dans l'app, va dans "Connexion"
2. Clique sur "Tester la connexion (Ping)"
3. **Vérifie côté serveur** : Tu devrais voir dans les logs :
   ```
   INFO - Message reçu: PING
   ```

### Test 2 : Ajouter une Question (Manuel)

Pour l'instant, les questions sont stockées localement dans l'app. Pour tester la synchronisation :

1. Va dans l'onglet "Questions"
2. Ajoute une nouvelle question
3. **Vérifie côté serveur** : Le serveur devrait recevoir le message `ADD_QUESTION`

**Note** : Pour que ça fonctionne automatiquement, il faut modifier `QuestionsViewModel` pour envoyer au Raspberry Pi.

---

## 💻 Code à Implémenter

### Synchronisation Automatique des Questions

Je peux t'aider à implémenter la synchronisation automatique. Voici ce qu'il faut faire :

1. **Modifier QuestionsViewModel** pour :
   - Observer l'état de connexion
   - Envoyer les questions au Raspberry Pi quand elles sont ajoutées/modifiées/supprimées
   - Charger les questions du Raspberry Pi au démarrage

2. **Modifier QuestionsRepository** pour :
   - Sauvegarder aussi dans le Raspberry Pi (pas seulement localement)

---

## 🎯 Ce que tu peux faire MAINTENANT

### Option 1 : Tester manuellement

1. **Dans l'app** : Va dans "Questions" et ajoute une question
2. **Côté serveur** : Regarde les logs pour voir si le message arrive
3. **Teste le ping** : Clique sur "Tester la connexion"

### Option 2 : Implémenter la synchronisation

Je peux t'aider à coder la synchronisation automatique des questions entre l'app et le Raspberry Pi.

### Option 3 : Tester le jeu complet

Une fois la synchronisation faite, tu pourras :
- Démarrer un jeu depuis l'app
- Recevoir les questions
- Répondre
- Voir les scores

---

## 📝 Checklist des Prochaines Étapes

- [ ] **Tester ping/pong** depuis l'app
- [ ] **Implémenter synchronisation questions** (App → Raspberry Pi)
- [ ] **Implémenter chargement questions** (Raspberry Pi → App)
- [ ] **Tester ajout question** avec synchronisation
- [ ] **Implémenter démarrage jeu** depuis l'app
- [ ] **Implémenter affichage scores** depuis Raspberry Pi
- [ ] **Tester le flux complet** : Ajout question → Jeu → Scores

---

## 🚀 Veux-tu que je code la synchronisation maintenant ?

Je peux t'aider à :
1. ✅ Modifier `QuestionsViewModel` pour synchroniser automatiquement
2. ✅ Charger les questions du Raspberry Pi au démarrage
3. ✅ Gérer les conflits (questions locales vs distantes)

**Dis-moi ce que tu veux faire en premier !** 🎯

