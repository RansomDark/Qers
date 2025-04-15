import requests
from flask import Flask, render_template, redirect, url_for, flash, session, request, jsonify, send_from_directory
from flask_login import LoginManager, UserMixin
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

# Декоратор для проверки аутентификации
def session_required(f):
    @wraps(f)
    def decorated_function(*args, **kwargs):
        # Проверяем, есть ли в сессии данные, указывающие на авторизацию пользователя
        if 'username' not in session:
            flash("Необходима авторизация.", "warning")
            return redirect(url_for('login'))  # Перенаправляем на страницу логина, если пользователь не авторизован
        return f(*args, **kwargs)  # Если авторизация есть, продолжаем выполнение функции
    return decorated_function

@login_manager.user_loader
def load_user(user_id):
    response = requests.get(f"{API_URL}/users/{user_id}")
    if response.status_code == 200:
        user_data = response.json()
        session['token'] = user_data['token']
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
            response = requests.post(f"{API_URL}/register", json=data, timeout=5)  # Таймаут в 5 секунд
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
                session['user_id'] = user_data['user_id']  # Сохраняем в сессию
                session['token'] = user_data['token']
                session['username'] = form.username.data
                return redirect(url_for('main'))  # Перенаправление в профиль
            elif response.status_code == 401:
                error_message = response.json().get('error')
                flash(error_message, "danger")
            else:
                flash("Ошибка входа. Попробуйте позже.", "danger")
        except requests.exceptions.RequestException:
            flash("Ошибка соединения с сервером. Попробуйте позже.", "danger")
    
    return render_template('login.html', form=form)

@app.route('/', methods=['GET'])
def start():
    return redirect(url_for('main'))

@app.route('/main', methods=['GET', 'POST'])
@session_required
def main():
    if request.method == 'POST':
        # Извлекаем данные, полученные от клиента
        data = request.get_json()
        is_pressed = data.get('is_pressed')

        if is_pressed is not None:
            # Отправляем запрос на удалённый API
            try:
                headers = {
                    'Authorization': f'Bearer {session["token"]}'
                }
                response = requests.post(f"{API_URL}/press", json={'username': session['username']}, headers=headers)
                
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

@app.route('/download-agreement')
def download_agreement():
    return send_from_directory('static/docs', 'user_agreement_program.pdf', as_attachment=True)

if __name__ == '__main__':
    app.run(debug=True)
