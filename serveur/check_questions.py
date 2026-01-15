#!/usr/bin/env python3
"""
Script pour vérifier le nombre de questions dans la base de données Flask
"""

from database import Database

def check_questions():
    """Affiche toutes les questions dans la base de données"""
    db = Database()
    
    # Récupérer toutes les questions
    conn = db.get_connection()
    cursor = conn.cursor()
    
    cursor.execute('SELECT id, question, correct_answer, category FROM questions')
    questions = cursor.fetchall()
    
    conn.close()
    
    print(f"\n📊 Total de questions dans la base: {len(questions)}\n")
    
    if len(questions) == 0:
        print("⚠️  Aucune question dans la base de données!")
        print("   Utilisez l'API POST /questions pour ajouter des questions")
        return
    
    print("Liste des questions:")
    print("=" * 80)
    for q_id, question, answer, category in questions:
        print(f"ID: {q_id} | Catégorie: {category}")
        print(f"  Q: {question}")
        print(f"  R: {answer}")
        print("-" * 80)
    
    if len(questions) == 1:
        print("\n⚠️  ATTENTION: Il n'y a qu'une seule question dans la base!")
        print("   C'est pourquoi la même question se répète.")
        print("   Ajoutez plus de questions via l'API POST /questions")

if __name__ == '__main__':
    check_questions()
