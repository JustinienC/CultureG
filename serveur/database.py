import sqlite3

class Database:
    def __init__(self, db_name='qa_database.db'):
        self.db_name = db_name
        self.create_tables()

    def get_connection(self):
        return sqlite3.connect(self.db_name, check_same_thread=False)

    def create_tables(self):
        conn = self.get_connection()
        cursor = conn.cursor()

        cursor.execute('''
            CREATE TABLE IF NOT EXISTS questions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                question TEXT NOT NULL,
                correct_answer TEXT NOT NULL,
                category TEXT NOT NULL
            )
        ''')

        cursor.execute(''' 
            CREATE TABLE IF NOT EXISTS Players (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                score INTEGER DEFAULT 0
            )
        ''')

        cursor.execute(''' 
            CREATE TABLE IF NOT EXISTS Duels (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                duel_name TEXT NOT NULL
            )
        ''')

        cursor.execute(''' 
            CREATE TABLE IF NOT EXISTS Duel_Players (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                duel_id INTEGER,
                player_id INTEGER,
                FOREIGN KEY (duel_id) REFERENCES Duels (id),
                FOREIGN KEY (player_id) REFERENCES Players (id)
            )
        ''')

        
        conn.commit()
        conn.close()

    # Add question
    def add_question(self, question, correct_answer, category):
        conn = self.get_connection()
        cursor = conn.cursor()

        cursor.execute(
            'INSERT INTO questions (question, correct_answer, category) VALUES (?, ?, ?)',
            (question, correct_answer, category)
        )

        conn.commit()
        conn.close()

    # Get questions
    def get_random_question(self):
        conn = self.get_connection()
        cursor = conn.cursor()

        cursor.execute(
            'SELECT id, question, category FROM questions ORDER BY RANDOM() LIMIT 1'
        )
        result = cursor.fetchone()

        conn.close()
        return dict(result) if result else None

    # Get questions by category
    def get_questions_by_category(self, category):
        conn = self.get_connection()
        cursor = conn.cursor()

        cursor.execute(
            'SELECT id, question FROM questions WHERE category = ?',
            (category,)
        )
        results = cursor.fetchall()

        conn.close()
        return dict(results) if results else None

    # Validate an answer
    def validate_answer(self, question_id, user_answer):
        conn = self.get_connection()
        cursor = conn.cursor()

        cursor.execute(
            'SELECT correct_answer FROM questions WHERE id = ?',
            (question_id,)
        )
        correct = cursor.fetchone()

        is_correct = correct and correct[0] == user_answer

        cursor.execute(
            'INSERT INTO answers (question_id, user_answer, is_correct) VALUES (?, ?, ?)',
            (question_id, user_answer, is_correct)
        )

        conn.commit()
        conn.close()

        return is_correct
    
    # Add a new player
    def add_player(self, name):
        conn = self.get_connection()
        cursor = conn.cursor()

        cursor.execute(
            'INSERT INTO players (name) VALUES (?)',
            (name,)
        )

        conn.commit()
        conn.close()

    # Delete player by ID
    def delete_player(self, player_id):
        conn = self.get_connection()
        cursor = conn.cursor()

        cursor.execute(
            'DELETE FROM players WHERE id = ?',
            (player_id,)
        )

        conn.commit()
        conn.close()

    # Update player score
    def update_player_score(self, player_id, score):
        conn = self.get_connection()
        cursor = conn.cursor()

        cursor.execute(
            'UPDATE players SET score = ? WHERE id = ?',
            (score, player_id)
        )

        conn.commit()
        conn.close()

    # Delete all players
    def delete_all_players(self):
        conn = self.get_connection()
        cursor = conn.cursor()

        cursor.execute(
            'DELETE FROM players'
        )

        conn.commit()
        conn.close()

    # add duel
    def add_duel(self, duel_name):
        conn = self.get_connection()
        cursor = conn.cursor()

        cursor.execute(
            'INSERT INTO duels (duel_name) VALUES (?)',
            (duel_name,)
        )

        conn.commit()
        conn.close()

    # get duel details
    def get_duel(self, duel_id):
        conn = self.get_connection()
        cursor = conn.cursor()

        cursor.execute(
            'SELECT id, duel_name FROM duels WHERE id = ?',
            (duel_id,)
        )
        result = cursor.fetchone()

        #get top player in the duel
        cursor.execute(
            '''SELECT p.id, p.name, p.score FROM players p
               JOIN duel_players dp ON p.id = dp.player_id
               WHERE dp.duel_id = ?
               ORDER BY p.score DESC LIMIT 1''',
            (duel_id,)
        )
        top_player = cursor.fetchone()

        if result:
            result = dict(result)
            if top_player:
                result['top_player'] = dict(top_player)
        else:
            result = None

        conn.close()
        return dict(result) if result else None

    # add player to duel
    def add_player_to_duel(self, duel_id, player_id):
        conn = self.get_connection()
        cursor = conn.cursor()
        cursor.execute(
            'INSERT INTO duel_players (duel_id, player_id) VALUES (?, ?)',
            (duel_id, player_id)
        )

        conn.commit()
        conn.close()

    

    # Get the player with the highest score
    def get_top_player(self):
        conn = self.get_connection()
        cursor = conn.cursor()

        cursor.execute(
            'SELECT id, name, score FROM players ORDER BY score DESC LIMIT 1'
        )
        result = cursor.fetchone()

        conn.close()
        return dict(result) if result else None
