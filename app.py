from flask import Flask, request, jsonify
import sqlite3
import hashlib
import secrets
import logging

app = Flask(__name__)

logging.basicConfig(filename='app.log', level=logging.DEBUG)

# Создаем базу данных
conn = sqlite3.connect('data.db')
cursor = conn.cursor()
cursor.execute('''
    CREATE TABLE IF NOT EXISTS users (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        password_hash TEXT NOT NULL,
        username TEXT NOT NULL UNIQUE,
        email TEXT NOT NULL UNIQUE,
        is_pressed INTEGER DEFAULT 0,
        hash TEXT NOT NULL
    )
''')
conn.commit()
conn.close()


def generate_random_hash():
    return secrets.token_hex(15)


def hash_password(password):
    return hashlib.sha256(password.encode()).hexdigest()


def verify_password(plain_password, hashed_password):
    return hashlib.sha256(plain_password.encode()).hexdigest() == hashed_password


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
    app.logger.info("Start creating user")

    data = request.get_json()
    random_hash = generate_random_hash()

    if 'username' not in data or 'password' not in data or 'email' not in data:
        return jsonify({'error': 'Username, password, and email are required'}), 400

    username = data['username']
    password = data['password']
    email = data['email']
    app.logger.info(f"User: {username} with email: {email}, checking now")

    conn = sqlite3.connect('data.db')
    cursor = conn.cursor()

    # Проверка существующего пользователя по имени пользователя
    cursor.execute('SELECT COUNT(*) FROM users WHERE username = ?', (username,))
    if cursor.fetchone()[0] > 0:
        conn.close()
        return jsonify({'error': "There is already such a login"}), 400

    # Проверка существующего пользователя по электронной почте
    cursor.execute('SELECT COUNT(*) FROM users WHERE email = ?', (email,))
    if cursor.fetchone()[0] > 0:
        conn.close()
        return jsonify({'error': "There is already such an email"}), 400

    hashed_password = hash_password(password)
    try:
        cursor.execute('''
            INSERT INTO users (password_hash, username, email, is_pressed, hash)
            VALUES (?, ?, ?, ?, ?)
        ''', (hashed_password, data['username'], data['email'], data.get('is_pressed', 0), random_hash))

        conn.commit()
        conn.close()

        app.logger.info("User created successfully")
        return jsonify({'message': 'User created successfully'}), 201
    except Exception as e:
        conn.rollback()
        conn.close()

        app.logger.error("Internal Server Error")
        return jsonify({'error': 'Internal Server Error'}), 500


@app.route('/user/<int:user_id>', methods=['GET'])
def get_user(user_id):
    conn = sqlite3.connect('data.db')
    cursor = conn.cursor()
    cursor.execute('SELECT * FROM users WHERE id = ?', (user_id,))
    user = cursor.fetchone()
    conn.close()

    if user:
        return jsonify({
            'id': user[0],
            'password': user[1],
            'username': user[2],
            'email': user[3],
            'is_pressed': bool(user[4])
        })
    else:
        return jsonify({'message': 'User not found'}), 404


@app.route('/user/<int:user_id>', methods=['PUT'])
def update_user(user_id):
    data = request.get_json()
    app.logger.info(f"Received PUT request for user_id {user_id}: {data}")
    conn = sqlite3.connect('data.db')
    cursor = conn.cursor()

    # Получаем текущие значения пользователя
    cursor.execute('SELECT * FROM users WHERE id = ?', (user_id,))
    user = cursor.fetchone()

    if not user:
        conn.close()
        return jsonify({'error': 'User not found'}), 404

    app.logger.info(f"Before update: {user}")

    # Обновляем значения, если они предоставлены в запросе
    user_data = {
        'password_hash': hash_password(data.get('password')) if 'password' in data else user[1],
        'username': data.get('username', user[2]),
        'email': data.get('email', user[3]),
        'is_pressed': data.get('is_pressed', user[4])
    }

    app.logger.info(f"User data to update: {user_data}")

    update_query = '''
           UPDATE users
           SET password_hash = ?, username = ?, email = ?, is_pressed = ?
           WHERE id = ?
       '''
    app.logger.info(f"SQL Query: {update_query}")

    cursor.execute(update_query, (
    user_data['password_hash'], user_data['username'], user_data['email'], user_data['is_pressed'], user_id))

    conn.commit()
    conn.close()

    app.logger.info(f"After update: {user_data}")

    return jsonify({'message': 'User updated successfully'})


@app.route('/press/', methods=['POST'])
def press_button():
    data = request.get_json()
    if 'username' not in data:
        return jsonify({'error': 'Username is required'}), 400

    username = data['username']
    is_pressed = get_is_pressed(username)

    conn = sqlite3.connect('data.db')
    cursor = conn.cursor()
    cursor.execute('UPDATE users SET is_pressed = ? WHERE username = ?', (not is_pressed, username,))
    conn.commit()
    conn.close()

    return jsonify({'message': 'Button pressed successfully'})


def login_user(username, password):
    conn = sqlite3.connect('data.db')
    cursor = conn.cursor()
    cursor.execute('SELECT * FROM users WHERE username = ?', (username,))
    user = cursor.fetchone()
    conn.close()

    if user and verify_password(password, user[1]):
        return jsonify({
            'id': user[0],
            'username': user[2],
            'email': user[3],
            'is_pressed': bool(user[4])
        })
    else:
        return jsonify({'message': 'Invalid username or password'}), 401


@app.route('/login', methods=['POST'])
def login():
    data = request.get_json()

    if 'username' not in data or 'password' not in data:
        return jsonify({'error': 'Both username and password are required'}), 400

    username = data['username']
    password = data['password']

    return login_user(username, password)


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
    app.run(host='0.0.0.0', port=5001)
