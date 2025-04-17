import logging
from flask import Blueprint, request, jsonify
from auth import generate_token, token_required
from utils import hash_password, verify_password, generate_random_hash
from databases import get_db_connection
from models import get_user_by_username, get_user_by_id

# Настройка логирования в файл
logging.basicConfig(filename='app.log', level=logging.DEBUG,
                    format='%(asctime)s - %(levelname)s - %(message)s')

# Создание Blueprint для организации маршрутов
routes = Blueprint('routes', __name__)


# Регистрация нового пользователя
@routes.route('/register', methods=['POST'])
def register():
    """
    @brief Регистрация нового пользователя.

    Принимает данные пользователя (username, password, email), проверяет их на наличие
    и создает нового пользователя в базе данных.

    @param username Строка, имя пользователя.
    @param password Строка, пароль пользователя.
    @param email Строка, email пользователя.

    @return JSON-ответ с сообщением об успехе или ошибке.
    """
    data = request.get_json()
    username = data.get('username')
    password = data.get('password')
    email = data.get('email')

    # Проверка обязательных полей
    if not all([username, password, email]):
        logging.warning("Username, password or email not found")
        return jsonify({'error': 'Username, password, and email are required'}), 400

    hashed = hash_password(password)
    user_hash = generate_random_hash()

    # Работа с БД: проверка уникальности username и email, добавление пользователя
    with get_db_connection() as conn:
        cursor = conn.cursor()
        if get_user_by_username(username):
            logging.warning(f"Имя пользователя уже существует: {username}")
            return jsonify({'error': 'Username already exists'}), 409

        cursor.execute('SELECT COUNT(*) FROM users WHERE email = ?', (email,))
        if cursor.fetchone()[0] > 0:
            logging.warning(f"Email уже зарегистрирован: {email}")
            return jsonify({'error': 'Email already registered'}), 409

        cursor.execute(''' 
            INSERT INTO users (password_hash, username, email, is_pressed, hash)
            VALUES (?, ?, ?, ?, ?)
        ''', (hashed, username, email, 0, user_hash))
        conn.commit()

    logging.info(f"User registered successfully: {username}")
    return jsonify({'message': 'User registered successfully'}), 201


# Вход пользователя
@routes.route('/login', methods=['POST'])
def login():
    """
    @brief Вход пользователя в систему.

    Принимает данные пользователя для входа (username, password), проверяет их
    и возвращает токен, если данные верные.

    @param username Строка, имя пользователя.
    @param password Строка, пароль пользователя.

    @return JSON-ответ с токеном пользователя или ошибкой.
    """
    data = request.get_json()
    username = data.get('username')
    password = data.get('password')

    # Проверка обязательных полей
    if not all([username, password]):
        logging.warning("Username and password are required")
        return jsonify({'error': 'Username and password are required'}), 400

    user = get_user_by_username(username)

    # Проверка пароля, генерация токенов
    if user and verify_password(password, user[1]):
        logging.info(f"Login success, creds: {user}")
        token = generate_token(user[0])
        return jsonify({
            'token': token,
            'user_id': user[0],
            'is_pressed': user[4]
        })

    logging.warning(f"Error login: Invalid credentials for {username}")
    return jsonify({'error': 'Invalid credentials'}), 401


# Получение информации о пользователе по ID
@routes.route('/user/<int:user_id>', methods=['GET'])
@token_required
def get_user(user_id):
    """
    @brief Получение информации о пользователе по его ID.

    Возвращает данные пользователя, такие как username, email и состояние кнопки.

    @param user_id Целое число, ID пользователя.

    @return JSON-ответ с данными пользователя или ошибкой, если пользователь не найден.
    """
    user = get_user_by_id(user_id)
    if not user:
        logging.warning(f"User {user_id} not found")
        return jsonify({'error': 'User not found'}), 404

    logging.info(f"User credits: {user_id}")
    return jsonify({
        'id': user[0],
        'username': user[2],
        'email': user[3],
        'is_pressed': bool(user[4])
    })


# Обновление информации пользователя
@routes.route('/user/<int:user_id>', methods=['PUT'])
@token_required
def update_user(user_id):
    """
    @brief Обновление информации о пользователе.

    Принимает данные для обновления пользователя и обновляет его в базе данных.

    @param user_id Целое число, ID пользователя.
    @param username Строка, новое имя пользователя (опционально).
    @param email Строка, новый email пользователя (опционально).
    @param password Строка, новый пароль (опционально).
    @param is_pressed Целое число, новое состояние кнопки (0 или 1).

    @return JSON-ответ с результатом обновления или ошибкой.
    """
    data = request.get_json()
    user = get_user_by_id(user_id)
    if not user:
        logging.warning(f"Пользователь с ID {user_id} не найден")
        return jsonify({'error': 'User not found'}), 404

    logging.info(f"Refresh user data: {user_id}")

    # Подготовка новых данных (если не переданы — оставляем старые)
    updated = {
        'username': data.get('username', user[2]),
        'email': data.get('email', user[3]),
        'password_hash': hash_password(data['password']) if 'password' in data else user[1],
        'is_pressed': data.get('is_pressed', user[4])
    }

    # Обновление в БД
    with get_db_connection() as conn:
        cursor = conn.cursor()
        cursor.execute('''
            UPDATE users SET password_hash = ?, username = ?, email = ?, is_pressed = ?
            WHERE id = ?
        ''', (updated['password_hash'], updated['username'], updated['email'], updated['is_pressed'], user_id))
        conn.commit()

    logging.info(f"User data update success {user_id}")
    return jsonify({'message': 'User updated'})


# Переключение состояния кнопки пользователя (is_pressed)
@routes.route('/press', methods=['POST'])
@token_required
def toggle_button():
    """
    @brief Переключение состояния кнопки пользователя.

    Изменяет состояние кнопки пользователя с 0 на 1 или наоборот.

    @param username Строка, имя пользователя.

    @return JSON-ответ с новым состоянием кнопки пользователя.
    """
    data = request.get_json()
    username = data.get('username')
    user = get_user_by_username(username)
    if not user:
        logging.warning(f"User {username} not found")
        return jsonify({'error': 'User not found'}), 404

    new_state = 0 if user[4] else 1

    # Обновление состояния is_pressed
    with get_db_connection() as conn:
        cursor = conn.cursor()
        cursor.execute('UPDATE users SET is_pressed = ? WHERE username = ?', (new_state, username))
        conn.commit()

    logging.info(f"Button {username} in state: {bool(new_state)}")
    return jsonify({'message': 'Button toggled', 'new_state': bool(new_state)})


# Проверка текущего состояния кнопки пользователя
@routes.route('/is_pressed', methods=['GET'])
@token_required
def check_pressed():
    """
    @brief Проверка текущего состояния кнопки пользователя.

    Возвращает текущее состояние кнопки пользователя.

    @param username Строка, имя пользователя.

    @return JSON-ответ с состоянием кнопки пользователя.
    """
    username = request.args.get('username')
    user = get_user_by_username(username)
    if not user:
        logging.warning(f"Param is_pressed: user {username} not found")
        return jsonify({'error': 'User not found'}), 404

    logging.info(f"Check state {username}: {bool(user[4])}")
    return jsonify({'is_pressed': bool(user[4])})
