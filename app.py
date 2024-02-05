from flask import Flask, request, jsonify
import sqlite3
import hashlib
import secrets

app = Flask(__name__)

# Создаем базу данных
conn = sqlite3.connect('data.db')
cursor = conn.cursor()
cursor.execute('''
    CREATE TABLE IF NOT EXISTS users (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        password TEXT NOT NULL,
        username TEXT NOT NULL UNIQUE,
        is_pressed INTEGER DEFAULT 0,
        hash TEXT NOT NULL
    )
''')
conn.commit()
conn.close()


def generate_random_hash():
    return secrets.token_hex(15)


@app.route('/get_user_id_by_username', methods=['GET'])
def get_user_id_by_username():
    requested_username = request.args.get('username')

    if not requested_username:
        return jsonify({'error': 'Username is required'}), 400

    with sqlite3.connect('data.db') as conn:
        cursor = conn.cursor()
        cursor.execute('SELECT id FROM users WHERE username = ?', (requested_username,))
        result = cursor.fetchone()

    if result:
        return str(result[0])
    else:
        return jsonify({'message': 'User not found'}), 404


@app.route('/user', methods=['POST'])
def create_user():
    data = request.get_json()
    random_hash = generate_random_hash()

    conn = sqlite3.connect('data.db')
    cursor = conn.cursor()
    cursor.execute('''
        INSERT INTO users (password , username, is_pressed, hash) VALUES (?, ?, ?, ?)
    ''', (data['password'], data['username'], data.get('is_pressed', 0), random_hash))

    conn.commit()
    conn.close()
    return jsonify({'message': 'User created successfully'}), 201


@app.route('/user/<int:user_id>', methods=['GET'])
def get_user(user_id):
    conn = sqlite3.connect('data.db')
    cursor = conn.cursor()
    cursor.execute('SELECT * FROM users WHERE id = ?', (user_id,))
    user = cursor.fetchone()
    conn.close()

    if user:
        return jsonify({'id': user[0], 'password': user[1], 'username': user[2], 'is_pressed': bool(user[3])})
    else:
        return jsonify({'message': 'User not found'}), 404


@app.route('/user/<int:user_id>', methods=['PUT'])
def update_user(user_id):
    data = request.get_json()
    conn = sqlite3.connect('data.db')
    cursor = conn.cursor()

    # Получаем текущие значения пользователя
    cursor.execute('SELECT * FROM users WHERE id = ?', (user_id,))
    user = cursor.fetchone()

    if not user:
        conn.close()
        return jsonify({'error': 'User not found'}), 404

    # Обновляем значения, если они предоставлены в запросе
    user_data = {
        'password': data.get('password', user[1]),
        'username': data.get('username', user[2]),
        'is_pressed': data.get('is_pressed', user[3])
    }

    cursor.execute('''
        UPDATE users
        SET password = ?, username = ?, is_pressed = ?
        WHERE id = ?
    ''', (user_data['password'], user_data['username'], user_data['is_pressed'], user_id))

    conn.commit()
    conn.close()
    return jsonify({'message': 'User updated successfully'})


def get_is_pressed(username):
    with sqlite3.connect('data.db') as conn:
        cursor = conn.cursor()
        cursor.execute('SELECT is_pressed FROM users WHERE username = ?', (username,))
        result = cursor.fetchone()
    return bool(result[0]) if result else None


@app.route('/check_is_pressed', methods=['GET'])
def check_is_pressed():
    username = request.args.get('username')

    if not username:
        return jsonify({'error': 'Username is required'}), 400

    is_pressed = get_is_pressed(username)

    if is_pressed is None:
        return jsonify({'error': 'User not found'}), 404
    elif is_pressed:
        return jsonify({'message': 'User is pressed'}), 200
    else:
        return jsonify({'message': 'User is not pressed'}), 200


if __name__ == '__main__':
    app.run(debug=True)