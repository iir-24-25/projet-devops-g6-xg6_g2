from flask import jsonify, request, current_app
import mysql.connector
import random
import string  # Ajout de l'import manquant

def get_quiz_questions(room_id):
    """Endpoint pour récupérer les questions d'un quiz spécifique."""
    conn = current_app.db_connection.get_connection()
    cursor = conn.cursor()
    try:
        cursor.execute("""
            SELECT q.id, q.question_text, a.id AS answer_id, a.answer_text, ca.answer_id AS correct_answer_id
            FROM quizzes qu
            JOIN questions q ON qu.id = q.quiz_id
            JOIN answers a ON q.id = a.question_id
            LEFT JOIN correct_answers ca ON q.id = ca.question_id AND a.id = ca.answer_id
            WHERE qu.room_id = %s
        """, (room_id,))
        results = cursor.fetchall()
        if not results:
            return jsonify({'error': 'Quiz non trouvé ou sans questions'}), 404

        questions = {}
        for q_id, q_text, a_id, a_text, correct_a_id in results:
            if q_id not in questions:
                questions[q_id] = {
                    'id': q_id,
                    'question': q_text,
                    'answers': [],
                    'correctAnswerIds': []
                }
            questions[q_id]['answers'].append({'id': a_id, 'text': a_text})
            if correct_a_id:
                questions[q_id]['correctAnswerIds'].append(correct_a_id)

        # Mélanger les réponses pour chaque question
        for question in questions.values():
            random.shuffle(question['answers'])

        return jsonify(list(questions.values())), 200

    except mysql.connector.Error as err:
        return jsonify({'error': f"Erreur lors de la récupération des questions : {err}"}), 500
    finally:
        if conn and conn.is_connected():
            cursor.close()
            conn.close()

def submit_quiz(room_id):
    """Endpoint pour soumettre les réponses d'un joueur."""
    data = request.json
    if not data or 'answers' not in data or not isinstance(data['answers'], list):
        return jsonify({'error': 'Données de soumission invalides'}), 400

    conn = current_app.db_connection.get_connection()
    if conn is None:
        return jsonify({'error': 'Impossible de se connecter à la base de données'}), 500

    cursor = conn.cursor()
    score = 0
    results = []
    correct_map = {}

    try:
        cursor.execute("""
            SELECT q.id, q.question_text, ca.answer_id, a.answer_text
            FROM quizzes qu
            JOIN questions q ON qu.id = q.quiz_id
            JOIN correct_answers ca ON q.id = ca.question_id
            JOIN answers a ON ca.answer_id = a.id
            WHERE qu.room_id = %s
        """, (room_id,))
        correct_answers_db = cursor.fetchall()

        correct_map = {}
        for q_id, q_text, a_id, a_text in correct_answers_db:  # Récupérer q_text
            if q_id not in correct_map:
                correct_map[q_id] = {
                    'question': q_text,  # Stocker le texte de la question
                    'answers': []
                }
            correct_map[q_id]['answers'].append({'id': a_id, 'text': a_text})

        for response in data['answers']:
            question_id = response.get('questionId')
            user_answer_ids = response.get('answerIds')

            if question_id is None or not isinstance(user_answer_ids, list):
                return jsonify({'error': 'Format de réponse invalide'}), 400

            correct_data = correct_map.get(question_id)
            correct_ids = [ans['id'] for ans in correct_data['answers']] if correct_data else []
            is_correct = set(user_answer_ids) == set(correct_ids)
            if is_correct:
                score += 1

            results.append({
                'questionId': question_id,
                'questionText': correct_data['question'] if correct_data else 'Question non trouvée',  # Ajouter le texte de la question
                'givenAnswers': user_answer_ids,
                'isCorrect': is_correct,
                'correctAnswers': correct_data['answers'] if correct_data else []
            })

        return jsonify({'correctCount': score, 'results': results}), 200

    except mysql.connector.Error as err:
        return jsonify({'error': f"Erreur lors de la soumission du quiz : {err}"}), 500
    finally:
        if conn and conn.is_connected():
            cursor.close()
            conn.close()

def start_quiz_session(room_id):
    """Endpoint pour démarrer une session de quiz pour un utilisateur"""
    conn = current_app.db_connection.get_connection()
    if conn is None:
        return jsonify({'error': 'Impossible de se connecter à la base de données'}), 500

    cursor = conn.cursor()

    try:
        # Vérifier si le quiz existe, son statut et s'il a des questions
        cursor.execute("""
            SELECT q.id, q.status, COUNT(qu.id) as question_count
            FROM quizzes q
            LEFT JOIN questions qu ON q.id = qu.quiz_id
            WHERE q.room_id = %s
            GROUP BY q.id
        """, (room_id,))
        quiz = cursor.fetchone()

        if not quiz:
            return jsonify({'error': 'Quiz non trouvé'}), 404

        quiz_id, status, question_count = quiz

        if status != 'waiting':
            return jsonify({'error': 'Le quiz a déjà commencé ou est terminé'}), 400

        if question_count == 0:
            return jsonify({'error': 'Le quiz ne contient aucune question'}), 400

        # Générer un ID de session unique pour l'utilisateur
        session_id = ''.join(random.choices(string.ascii_letters + string.digits, k=16))

        # Enregistrer la session utilisateur
        cursor.execute("""
            INSERT INTO user_sessions (session_id, quiz_id, joined_at)
            VALUES (%s, %s, NOW())
        """, (session_id, quiz_id))

        conn.commit()

        return jsonify({
            'session_id': session_id,
            'room_id': room_id,
            'message': 'Session démarrée avec succès',
            'status': 'ready',
            'question_count': question_count
        }), 200

    except mysql.connector.Error as err:
        conn.rollback()
        return jsonify({'error': f"Erreur lors du démarrage de la session: {err}"}), 500
    finally:
        if conn and conn.is_connected():
            cursor.close()
            conn.close()

