from flask import request, jsonify, current_app
import mysql.connector
import random
import string

def generate_short_room_id(length=6):
    """Génère un roomId aléatoire."""
    characters = string.ascii_uppercase + string.digits
    return ''.join(random.choice(characters) for _ in range(length))

def create_manual():
    data = request.json
    if not data or 'questions' not in data or not isinstance(data['questions'], list):
        return jsonify({'error': 'Données de quiz invalides'}), 400

    conn = current_app.db_connection.get_connection()
    if conn is None:
        return jsonify({'error': 'Impossible de se connecter à la base de données'}), 500

    cursor = conn.cursor()
    room_id = generate_short_room_id()

    try:
        # Créer le quiz
        cursor.execute("INSERT INTO quizzes (room_id, status) VALUES (%s, 'waiting')", (room_id,))
        quiz_id = cursor.lastrowid

        for q_data in data['questions']:
            question_text = q_data.get('question')
            question_type = q_data.get('type')
            answers = q_data.get('answers', [])
            correct_answers = q_data.get('correctAnswers', [])

            if not question_text or not answers or not correct_answers:
                conn.rollback()
                return jsonify({'error': 'Champs manquants dans une question'}), 400

            # Insérer la question avec type
            cursor.execute(
                "INSERT INTO questions (quiz_id, question_text, question_type) VALUES (%s, %s, %s)",
                (quiz_id, question_text, question_type)
            )
            question_id = cursor.lastrowid

            # Insérer les réponses et récupérer leurs IDs
            answer_ids = {}
            for answer_text in answers:
                cursor.execute(
                    "INSERT INTO answers (question_id, answer_text) VALUES (%s, %s)",
                    (question_id, answer_text)
                )
                answer_ids[answer_text] = cursor.lastrowid

            # Associer les bonnes réponses avec leurs IDs (en évitant les doublons)
            for correct_text in set(correct_answers):  # ← Supprime les doublons
                answer_id = answer_ids.get(correct_text)
                if answer_id:
                    cursor.execute(
                        "INSERT INTO correct_answers (question_id, answer_id) VALUES (%s, %s)",
                        (question_id, answer_id)
                    )


        conn.commit()
        return jsonify({'room_id': room_id, 'message': 'Quiz créé avec succès'}), 201

    except mysql.connector.Error as err:
        conn.rollback()
        return jsonify({'error': f"Erreur lors de l'enregistrement du quiz : {err}"}), 500

    finally:
        if conn and conn.is_connected():
            cursor.close()
            conn.close()