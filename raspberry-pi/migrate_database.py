#!/usr/bin/env python3
"""
Script de migration de la base de données
Convertit le format QCM (ancien) vers le format Question-Réponse (nouveau)
"""

import sqlite3
import json
import sys
import os
from datetime import datetime

DB_FILE = "cultureg.db"
BACKUP_FILE = f"cultureg_backup_{datetime.now().strftime('%Y%m%d_%H%M%S')}.db"

def backup_database():
    """Créer une sauvegarde de la base de données"""
    if os.path.exists(DB_FILE):
        import shutil
        shutil.copy2(DB_FILE, BACKUP_FILE)
        print(f"✅ Sauvegarde créée: {BACKUP_FILE}")
        return True
    return False

def check_old_structure(conn):
    """Vérifier si l'ancienne structure existe"""
    c = conn.cursor()
    try:
        c.execute("PRAGMA table_info(questions)")
        columns = [col[1] for col in c.fetchall()]
        
        # Vérifier si les anciennes colonnes existent
        has_answers = 'answers' in columns
        has_correct_answer_index = 'correct_answer_index' in columns
        has_correct_answer = 'correct_answer' in columns
        
        if has_answers and has_correct_answer_index and not has_correct_answer:
            return True  # Ancienne structure
        elif has_correct_answer and not has_answers:
            return False  # Nouvelle structure déjà en place
        else:
            print("⚠️  Structure inconnue ou mixte")
            return None
    except Exception as e:
        print(f"❌ Erreur lors de la vérification: {e}")
        return None

def migrate_questions(conn):
    """Migrer les questions de l'ancien format vers le nouveau"""
    c = conn.cursor()
    
    # Récupérer toutes les questions de l'ancienne structure
    try:
        c.execute('SELECT id, question, answers, correct_answer_index, category, difficulty, created_at FROM questions')
        old_questions = c.fetchall()
    except sqlite3.OperationalError as e:
        print(f"❌ Erreur lors de la récupération: {e}")
        return False
    
    if not old_questions:
        print("ℹ️  Aucune question à migrer")
        return True
    
    print(f"📦 {len(old_questions)} question(s) à migrer")
    
    # Créer la nouvelle table
    c.execute('''
        CREATE TABLE IF NOT EXISTS questions_new (
            id TEXT PRIMARY KEY,
            question TEXT NOT NULL,
            correct_answer TEXT NOT NULL,
            category TEXT,
            difficulty TEXT,
            time_limit INTEGER,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
    ''')
    
    # Migrer les données
    migrated = 0
    for row in old_questions:
        question_id, question_text, answers_json, correct_index, category, difficulty, created_at = row
        
        try:
            # Parser les réponses JSON
            answers = json.loads(answers_json)
            
            # Extraire la réponse correcte
            if 0 <= correct_index < len(answers):
                correct_answer = answers[correct_index]
            else:
                print(f"⚠️  Index invalide pour question {question_id}, utilisation de la première réponse")
                correct_answer = answers[0] if answers else ""
            
            # Insérer dans la nouvelle table
            c.execute('''
                INSERT INTO questions_new 
                (id, question, correct_answer, category, difficulty, time_limit, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?)
            ''', (question_id, question_text, correct_answer, category, difficulty, None, created_at))
            
            migrated += 1
        except Exception as e:
            print(f"⚠️  Erreur migration question {question_id}: {e}")
    
    # Remplacer l'ancienne table par la nouvelle
    c.execute('DROP TABLE questions')
    c.execute('ALTER TABLE questions_new RENAME TO questions')
    
    conn.commit()
    print(f"✅ {migrated} question(s) migrée(s) avec succès")
    return True

def main():
    """Fonction principale"""
    print("🔄 Migration de la base de données CultureG")
    print("=" * 50)
    
    if not os.path.exists(DB_FILE):
        print(f"❌ Fichier {DB_FILE} introuvable")
        sys.exit(1)
    
    # Sauvegarde
    if not backup_database():
        print("⚠️  Impossible de créer la sauvegarde")
        response = input("Continuer quand même ? (o/N): ")
        if response.lower() != 'o':
            print("❌ Migration annulée")
            sys.exit(1)
    
    # Connexion à la base
    conn = sqlite3.connect(DB_FILE)
    
    # Vérifier la structure
    structure = check_old_structure(conn)
    
    if structure is None:
        print("❌ Structure inconnue, migration annulée")
        conn.close()
        sys.exit(1)
    
    if not structure:
        print("✅ La base de données est déjà au nouveau format")
        conn.close()
        sys.exit(0)
    
    # Confirmation
    print("\n⚠️  ATTENTION: Cette opération va modifier la structure de la base de données")
    print(f"📁 Sauvegarde: {BACKUP_FILE}")
    response = input("\nContinuer la migration ? (o/N): ")
    if response.lower() != 'o':
        print("❌ Migration annulée")
        conn.close()
        sys.exit(0)
    
    # Migration
    print("\n🔄 Migration en cours...")
    if migrate_questions(conn):
        print("\n✅ Migration terminée avec succès!")
        print(f"📁 Sauvegarde conservée: {BACKUP_FILE}")
    else:
        print("\n❌ Erreur lors de la migration")
        print(f"📁 Vous pouvez restaurer depuis: {BACKUP_FILE}")
        conn.close()
        sys.exit(1)
    
    conn.close()

if __name__ == "__main__":
    main()

