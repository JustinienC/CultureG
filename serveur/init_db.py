#!/usr/bin/env python3
"""
Script d'initialisation de la base de données
Crée les tables et ajoute des questions d'exemple
"""

import sys
import os

# Ajouter le répertoire src au chemin Python
sys.path.insert(0, os.path.join(os.path.dirname(__file__), 'src'))

from database import Database

def init_database():
    """Initialise la base de données avec des questions d'exemple"""
    
    # Créer la base de données
    #delete database file if exists
    if os.path.exists('questions_answers.db'):
        os.remove('questions_answers.db')

        
    db = Database(db_name='questions_answers.db')
    print("✅ Base de données créée avec succès!")
    
    # Ajouter des questions d'exemple
    questions = [
        ("Quelle est la capitale de la France?", "Paris"),
        ("Quel est le plus grand océan?", "Océan Pacifique"),
        ("Combien de continents y a-t-il?", "7"),
        ("Quel est le plus haut sommet du monde?", "Mont Everest"),
        ("En quelle année l'homme a marché sur la lune?", "1969"),
    ]
    
    for question, answer in questions:
        db.add_question(question, answer)
        print(f"✅ Question ajoutée: {question}")
    
    # Vérifier les données
    all_questions = db.get_questions()
    print(f"\n📊 Total de questions: {len(all_questions)}")
    
    db.close()
    print("\n🎉 Initialisation terminée!")

if __name__ == '__main__':
    init_database()
