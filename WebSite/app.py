import requests
from flask import Flask, render_template, redirect, url_for, flash, session, request, jsonify
from flask_login import LoginManager, UserMixin, login_user, logout_user, login_required, current_user
from functools import wraps

from forms import LoginForm, RegisterForm

API_URL = "http://127.0.0.1:5001/"  # URL удаленного API

app = Flask(__name__)
app.config['SECRET_KEY'] = 'key'

login_manager = LoginManager(app)
login_manager.login_view = 'login'

class User(UserMixin):
    def __init__(self, id, username, email, is_pressed):
        self.id = id
        self.username = username
        self.email = email
        self.is_pressed = is_pressed

@login_manager.user_loader
def load_user(user_id):
    response = requests.get(f"{API_URL}/users/{user_id}")
    if response.status_code == 200:
        user_data = response.json()
        return User(user_data["id"], user_data["username"], user_data["email"])
    return None

@app.route('/register', methods=['GET', 'POST'])
def register():
    form = RegisterForm()
    if form.validate_on_submit():
        data = {
            "username": form.username.data,
            "email": form.email.data,
            "password": form.password.data
        }

        try:
            response = requests.post(f"{API_URL}/user", json=data, timeout=5)  # Таймаут в 5 секунд
            if response.status_code == 201:
                
                return redirect(url_for('login'))
            
            elif response.status_code == 409:
                error_message = response.json().get('error')
                flash(error_message, "danger")

            else:

                flash(response.json().get("error", "Ошибка регистрации"), "danger")

        except requests.exceptions.RequestException:
            flash("Ошибка соединения с сервером. Попробуйте позже.", "danger")
    
    return render_template('register.html', form=form)

@app.route('/login', methods=['GET', 'POST'])
def login():
    form = LoginForm()
    if form.validate_on_submit():
        data = {
            "username": form.username.data,
            "password": form.password.data
        }

        # Отправляем запрос к API для авторизации пользователя
        try:
            response = requests.post(f"{API_URL}/login", json=data, timeout=5)

            if response.status_code == 200:
                user_data = response.json()  # Получаем данные пользователя
                session['id'] = user_data['id']  # Сохраняем в сессию
                session['username'] = user_data['username']
                session['is_pressed'] = user_data['is_pressed']

                return redirect(url_for('main'))  # Перенаправление в профиль

            elif response.status_code == 401:
                error_message = response.json().get('error')
                flash(error_message, "danger")

            else:
                flash("Ошибка входа. Попробуйте позже.", "danger")

        except requests.exceptions.RequestException:
            flash("Ошибка соединения с сервером. Попробуйте позже.", "danger")
    
    return render_template('login.html', form=form)

# Декоратор для проверки аутентификации
def login_required(f):
    @wraps(f)
    def decorated_function(*args, **kwargs):
        if 'id' not in session:  # Если пользователь не авторизован
            flash("Вам нужно войти в систему.", "warning")
            return redirect(url_for('login'))  # Перенаправление на login
        return f(*args, **kwargs)
    return decorated_function

@app.route('/', methods=['GET'])
def start():
    return redirect(url_for('main'))

@app.route('/main', methods=['GET', 'POST'])
def main():
    if 'username' not in session:
        flash("Вам нужно войти в систему.", "warning")
        return redirect(url_for('login'))  # Если не авторизован, редирект на логин
    
    if request.method == 'POST':
        # Извлекаем данные, полученные от клиента
        data = request.get_json()
        is_pressed = data.get('is_pressed')

        if is_pressed is not None:
            # Отправляем запрос на удалённый API
            try:
                response = requests.post(f"{API_URL}/press", json={'username': session['username']})
                
                if response.status_code == 200:
                    # Если запрос прошел успешно, сохраняем состояние в сессии
                    session['is_pressed'] = is_pressed
                    return jsonify({'success': True, 'is_pressed': is_pressed})
                else:
                    # В случае ошибки на удалённом API, возвращаем ошибку
                    flash("Ошибка на сервере", "error")
                    return jsonify({'status': 'error', 'message': 'Ошибка на удалённом API'}), 500
            
            except requests.exceptions.RequestException as e:
                # В случае проблемы с подключением, возвращаем ошибку
                flash("Ошибка соединения с сервером", "error")
                return jsonify({'status': 'error', 'message': 'Ошибка соединения с удалённым сервером'}), 500

        else:
            flash("Не указано состояние", "error")
            return jsonify({'status': 'error', 'message': 'Не указано состояние'}), 400
    
    is_pressed = session.get('is_pressed', False)  # Получаем состояние кнопки из сессии
    return render_template('main.html', is_pressed=is_pressed)

@app.route('/logout')
def logout():
    session.clear()  # Очистка сессии
    flash("Вы вышли из системы.", "info")
    return redirect(url_for('login'))  # Перенаправление на login

if __name__ == '__main__':
    app.run(debug=True)
