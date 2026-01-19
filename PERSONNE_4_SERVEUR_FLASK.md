# 📚 Documentation - Personne 4 : Serveur Flask + Base de données

## 🎯 Votre Mission

Vous êtes responsable du **serveur Flask** (API REST) et de la **base de données SQLite** qui stocke toutes les questions, joueurs, duels et scores. Votre code est le **cœur métier** du système : c'est vous qui validez les réponses, gérez les questions et stockez les données.

---

## 📁 Fichiers dont vous êtes responsable

- `serveur/webServer.py` - Serveur Flask avec toutes les routes API
- `serveur/database.py` - Classe Database pour gérer SQLite
- `serveur/init_db.py` - Script d'initialisation de la base
- `serveur/check_questions.py` - Script de vérification
- `serveur/qa_database.db` - Base de données SQLite (fichier généré)

---

## 🏗️ Architecture de votre partie

### 1. **Serveur Flask (`webServer.py`)**

Flask est un framework web Python qui expose une **API REST** (Application Programming Interface). Votre serveur écoute sur le port **5000** et répond aux requêtes HTTP.

#### Routes principales :

| Route | Méthode | Description |
|-------|---------|-------------|
| `/questions/random` | GET | Récupère une question aléatoire |
| `/questions` | POST | Ajoute une nouvelle question |
| `/questions` | GET | Récupère questions par catégorie |
| `/answers` | POST | Valide une réponse utilisateur |
| `/players` | POST | Ajoute un joueur |
| `/players/<id>` | DELETE | Supprime un joueur |
| `/players/top` | GET | Récupère le meilleur joueur |
| `/players/<id>/score` | PUT | Met à jour le score d'un joueur |
| `/duels` | POST | Crée un duel |
| `/duels/<id>/players` | POST | Ajoute un joueur à un duel |
| `/duels/<id>` | GET | Récupère les détails d'un duel |

#### Exemple de requête/réponse :

**Ajouter une question :**
```http
POST http://localhost:5000/questions
Content-Type: application/json

{
  "question": "Quelle est la capitale de la France ?",
  "correct_answer": "Paris",
  "category": "Géographie"
}
```

**Réponse :**
```json
{
  "message": "Question added"
}
```

**Récupérer une question aléatoire :**
```http
GET http://localhost:5000/questions/random
```

**Réponse :**
```json
{
  "id": 1,
  "question": "Quelle est la capitale de la France ?",
  "correct_answer": "Paris",
  "category": "Géographie"
}
```

**Valider une réponse :**
```http
POST http://localhost:5000/answers
Content-Type: application/json

{
  "question_id": 1,
  "answer": "paris"
}
```

**Réponse :**
```json
{
  "question_id": 1,
  "correct": true
}
```

---

### 2. **Base de données (`database.py`)**

Vous utilisez **SQLite**, une base de données légère stockée dans un fichier (`qa_database.db`). SQLite est parfait pour ce projet car elle ne nécessite pas de serveur séparé.

#### Tables de la base de données :

**1. Table `questions`**
```sql
CREATE TABLE questions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    question TEXT NOT NULL,
    correct_answer TEXT NOT NULL,
    category TEXT NOT NULL
)
```
- Stocke toutes les questions du jeu
- Chaque question a un ID unique, le texte, la bonne réponse et une catégorie

**2. Table `Players`**
```sql
CREATE TABLE Players (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    score INTEGER DEFAULT 0
)
```
- Stocke les joueurs et leurs scores

**3. Table `Duels`**
```sql
CREATE TABLE Duels (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    duel_name TEXT NOT NULL
)
```
- Stocke les duels (parties entre plusieurs joueurs)

**4. Table `Duel_Players`**
```sql
CREATE TABLE Duel_Players (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    duel_id INTEGER,
    player_id INTEGER,
    FOREIGN KEY (duel_id) REFERENCES Duels (id),
    FOREIGN KEY (player_id) REFERENCES Players (id)
)
```
- Table de liaison entre duels et joueurs (relation many-to-many)

**5. Table `answers`** (créée automatiquement)
```sql
CREATE TABLE answers (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    question_id INTEGER,
    user_answer TEXT,
    is_correct INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
)
```
- Historique de toutes les réponses données (pour statistiques)

---

## 🔑 Concepts clés à maîtriser

### 1. **API REST**

REST (Representational State Transfer) est un style d'architecture pour les APIs web. Les principes :
- **GET** : Récupérer des données (lecture)
- **POST** : Créer des données (écriture)
- **PUT** : Mettre à jour des données
- **DELETE** : Supprimer des données

Votre serveur Flask expose ces opérations via des **routes** (URLs).

### 2. **JSON (JavaScript Object Notation)**

Format d'échange de données entre le client et le serveur. Exemple :
```json
{
  "question": "Quelle est la capitale ?",
  "correct_answer": "Paris"
}
```

### 3. **Validation des réponses**

La fonction `validate_answer()` dans `database.py` est **cruciale** :

```python
def validate_answer(self, question_id, user_answer):
    # 1. Récupère la bonne réponse depuis la BDD
    # 2. Normalise les deux réponses (minuscules, sans accents, sans espaces)
    # 3. Compare les réponses normalisées
    # 4. Enregistre le résultat dans la table answers
    # 5. Retourne True/False
```

**Normalisation** : Pour que "Paris", "paris", "PARIS", "P a r i s" soient tous considérés comme corrects, vous :
- Mettez en minuscules
- Supprimez les accents (é → e, à → a)
- Supprimez les espaces multiples

**Bibliothèques utilisées :**
- `unicodedata` : Pour supprimer les accents
- `re` : Pour les expressions régulières (suppression espaces)

### 4. **SQLite**

- Base de données **fichier** (pas de serveur)
- Utilise le langage **SQL** pour les requêtes
- Connexion via `sqlite3.connect('qa_database.db')`
- Toujours fermer la connexion après utilisation

**Exemple de requête :**
```python
conn = sqlite3.connect('qa_database.db')
cursor = conn.cursor()
cursor.execute('SELECT * FROM questions WHERE id = ?', (question_id,))
result = cursor.fetchone()
conn.close()
```

---

## 🔄 Flux de données

### Quand la Raspberry Pi pose une question :

1. **Raspberry Pi** → `GET /questions/random` → **Flask**
2. **Flask** → Interroge SQLite → Récupère une question aléatoire
3. **Flask** → Retourne JSON → **Raspberry Pi**

### Quand l'utilisateur répond :

1. **Raspberry Pi** → `POST /answers` avec `question_id` et `answer` → **Flask**
2. **Flask** → Appelle `validate_answer()` dans `database.py`
3. **Database** → Normalise et compare les réponses
4. **Database** → Enregistre dans la table `answers`
5. **Flask** → Retourne `{"correct": true/false}` → **Raspberry Pi**

### Quand l'application Android ajoute une question :

1. **Android** → Envoie via WebSocket → **Raspberry Pi**
2. **Raspberry Pi** → `POST /questions` → **Flask**
3. **Flask** → Appelle `add_question()` dans `database.py`
4. **Database** → Insère dans SQLite
5. **Flask** → Retourne confirmation → **Raspberry Pi** → **Android**

---

## 🛠️ Technologies utilisées

- **Flask** : Framework web Python (`pip install flask`)
- **SQLite3** : Base de données (intégré à Python)
- **JSON** : Format d'échange (intégré à Python)
- **unicodedata** : Normalisation de texte (intégré)
- **re** : Expressions régulières (intégré)

---

## 📝 Questions fréquentes du professeur

### "Comment fonctionne votre validation de réponses ?"

**Réponse :** 
"J'utilise une normalisation de texte pour rendre la comparaison insensible à la casse, aux accents et aux espaces. Je convertis les deux réponses en minuscules, je supprime les accents avec `unicodedata`, et je normalise les espaces avec des expressions régulières. Ainsi, 'Paris', 'paris', 'P a r i s' sont tous considérés comme corrects."

### "Pourquoi utiliser SQLite plutôt qu'une base de données classique ?"

**Réponse :**
"SQLite est une base de données fichier, parfaite pour ce projet car elle ne nécessite pas de serveur séparé. Elle est légère, rapide, et suffisante pour stocker les questions et scores. Le fichier `qa_database.db` contient toutes les données et peut être facilement sauvegardé ou déplacé."

### "Comment garantissez-vous l'intégrité des données ?"

**Réponse :**
"J'utilise des contraintes SQL (PRIMARY KEY, NOT NULL) et des transactions (commit/rollback). Chaque opération vérifie que les champs requis sont présents avant l'insertion. De plus, j'utilise des requêtes paramétrées pour éviter les injections SQL."

### "Quelle est la différence entre votre base et celle de la Raspberry Pi ?"

**Réponse :**
"Ma base de données Flask est la **source de vérité** pour les questions. La Raspberry Pi a sa propre base locale pour le cache, mais elle récupère toujours les questions depuis mon serveur Flask via l'API REST. Quand une question est ajoutée depuis l'application Android, elle est d'abord sauvegardée localement sur la Raspberry Pi, puis synchronisée avec mon serveur Flask."

### "Comment gérez-vous les erreurs ?"

**Réponse :**
"Chaque route Flask retourne des codes HTTP appropriés : 200 (succès), 201 (créé), 400 (erreur client), 404 (non trouvé). Les erreurs de base de données sont capturées avec des try/except, et je retourne des messages d'erreur JSON explicites au client."

### "Pourquoi utiliser Flask plutôt qu'un autre framework ?"

**Réponse :**
"Flask est léger, simple à utiliser, et parfait pour une API REST. Il ne nécessite pas de configuration complexe et permet de créer rapidement des endpoints. Pour ce projet, Flask est suffisant et ne nécessite pas les fonctionnalités plus avancées de Django."

---

## 🚀 Pour démarrer votre serveur

```bash
cd serveur
python webServer.py
```

Le serveur démarre sur `http://localhost:5000` (ou `http://0.0.0.0:5000` pour être accessible depuis le réseau).

---

## 📊 Points importants à retenir

1. **Vous êtes la source de vérité** : Toutes les questions viennent de votre base Flask
2. **Validation intelligente** : Votre normalisation permet une comparaison flexible
3. **API REST standard** : Votre API suit les conventions REST (GET/POST/PUT/DELETE)
4. **Historique complet** : Toutes les réponses sont enregistrées dans la table `answers`
5. **Gestion des joueurs et duels** : Vous gérez aussi les scores et les parties multi-joueurs

---

## 🔗 Interactions avec les autres parties

- **Personne 2 (WebSocket)** : Reçoit les questions depuis votre API
- **Personne 5 (Logique métier)** : Appelle votre API pour récupérer questions et valider réponses
- **Personne 3 (Android)** : Ajoute des questions via WebSocket → Raspberry Pi → Votre API

---

**Vous êtes le cœur métier du système ! 🎯**
