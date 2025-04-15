from flask import request, jsonify
from functools import wraps
import jwt
import datetime
from config import Config


# Генерация JWT токенов
def generate_token(user_id):
    """
    Генерирует access token для пользователя с долгим сроком действия
    """
    secret = Config.SECRET_KEY  # Используем секретный ключ для access токена
    exp = datetime.datetime.utcnow() + datetime.timedelta(minutes=Config.ACCESS_TOKEN_EXPIRES)

    payload = {'user_id': user_id, 'exp': exp}
    return jwt.encode(payload, secret, algorithm='HS256')


# Декоратор для проверки наличия и валидности токена
def token_required(f):
    @wraps(f)
    def decorated(*args, **kwargs):
        token = None
        # Извлекаем токен из заголовка Authorization
        header = request.headers.get('Authorization', '')

        if header.startswith("Bearer "):
            token = header.split(" ")[1]

        if not token:
            return jsonify({'error': 'Token missing'}), 401

        try:
            # Проверяем токен с использованием секрета для access токенов
            payload = jwt.decode(token, Config.SECRET_KEY, algorithms=['HS256'])
            request.user_id = payload['user_id']
        except jwt.ExpiredSignatureError:
            return jsonify({'error': 'Token expired'}), 401
        except jwt.InvalidTokenError:
            return jsonify({'error': 'Invalid token'}), 401

        return f(*args, **kwargs)

    return decorated
