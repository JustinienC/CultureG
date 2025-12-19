from flask import Flask, jsonify, request
from database import Database

app = Flask(__name__)
db = Database()


#get one random question
@app.route('/questions/random', methods=['GET'])
def get_random_question():
    question = db.get_random_question()
    if not question:
        return jsonify({'error': 'No question found'}), 404
    return jsonify(question), 200

#add question
@app.route('/questions', methods=['POST'])
def add_question():
    data = request.get_json()

    if not data:
        return jsonify({'error': 'Invalid JSON'}), 400

    question = data.get('question')
    correct_answer = data.get('correct_answer')
    category = data.get('category')

    if not all([question, correct_answer, category]):
        return jsonify({'error': 'Missing fields'}), 400

    db.add_question(question, correct_answer, category)
    return jsonify({'message': 'Question added'}), 201



#add question with category
@app.route('/questions', methods=['GET'])
def get_questions_by_category():
    category = request.args.get('category')

    if not category:
        return jsonify({'error': 'Category is required'}), 400

    questions = db.get_questions_by_category(category)
    return jsonify(questions), 200


#submit answer
@app.route('/answers', methods=['POST'])
def submit_answer():
    data = request.get_json()

    question_id = data.get('question_id')
    user_answer = data.get('answer')

    if not all([question_id, user_answer]):
        return jsonify({'error': 'Missing fields'}), 400

    is_correct = db.validate_answer(question_id, user_answer)

    return jsonify({
        'question_id': question_id,
        'correct': is_correct
    }), 200

#add player
@app.route('/players', methods=['POST'])
def add_player():
    data = request.get_json()

    name = data.get('name')

    if not name:
        return jsonify({'error': 'Name is required'}), 400

    db.add_player(name)
    return jsonify({'message': 'Player added'}), 201

# delete all players
@app.route('/players/<int:player_id>', methods=['DELETE'])
def delete_player(player_id):
    db.delete_player(player_id)
    return jsonify({'message': 'Player deleted'}), 200

# delete all players
@app.route('/players', methods=['DELETE'])
def delete_all_players():
    db.delete_all_players()
    return jsonify({'message': 'All players deleted'}), 200

# get top player
@app.route('/players/top', methods=['GET'])
def get_top_player():
    top_player = db.get_top_player()
    return jsonify(top_player), 200

# update player score
@app.route('/players/<int:player_id>/score', methods=['PUT'])
def update_player_score(player_id):
    data = request.get_json()

    score = data.get('score')

    if score is None:
        return jsonify({'error': 'Score is required'}), 400

    db.update_player_score(player_id, score)
    return jsonify({'message': 'Player score updated'}), 200

# add duel
@app.route('/duels', methods=['POST'])
def add_duel():
    data = request.get_json()

    duel_name = data.get('duel_name')

    if not duel_name:
        return jsonify({'error': 'Duel name is required'}), 400

    db.add_duel(duel_name)
    return jsonify({'message': 'Duel added'}), 201

# add player to duel
@app.route('/duels/<int:duel_id>/players', methods=['POST'])
def add_player_to_duel(duel_id):
    data = request.get_json()

    player_id = data.get('player_id')

    if player_id is None:
        return jsonify({'error': 'Player ID is required'}), 400

    db.add_player_to_duel(duel_id, player_id)
    return jsonify({'message': 'Player added to duel'}), 201

# get duel details
@app.route('/duels/<int:duel_id>', methods=['GET'])
def get_duel(duel_id):
    duel = db.get_duel(duel_id)
    if not duel:
        return jsonify({'error': 'Duel not found'}), 404
    return jsonify(duel), 200

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)
