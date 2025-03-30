function clearErrors() {
    document.querySelectorAll('.error-message').forEach(error => {
        error.textContent = '';
        error.style.display = 'none';
    });
}

clearErrors(); // Очистка всех ошибок перед проверкой

// Функция для проверки пустых полей
function validateLoginForm(event) {
    const usernameInput = document.getElementById('username');
    const passwordInput = document.getElementById('password');
    
    const usernameError = document.getElementById('username-error');
    const passwordError = document.getElementById('password-error');
    
    let isValid = true;

    // Проверка имени пользователя
    if (!usernameInput.value.trim()) {
        usernameError.textContent = 'Имя пользователя не может быть пустым!';
        usernameError.style.display = 'block';
        isValid = false;
    } else {
        usernameError.style.display = 'none';
    }

    // Проверка пароля
    if (!passwordInput.value.trim()) {
        passwordError.textContent = 'Пароль не может быть пустым!';
        passwordError.style.display = 'block';
        isValid = false;
    } else {
        passwordError.style.display = 'none';
    }

    // Если есть ошибки, предотвращаем отправку формы
    if (!isValid) {
        event.preventDefault();
    }
}
