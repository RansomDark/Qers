function clearErrors() {
    document.querySelectorAll('.error-message').forEach(error => {
        error.textContent = '';
        error.style.display = 'none';
    });
}

clearErrors(); // Очистка всех ошибок перед проверкой

// Функция для проверки email с использованием регулярного выражения
function validateEmail() {
    const emailInput = document.getElementById('email');
    const emailValue = emailInput.value;
    const emailError = document.getElementById('email-error');

    clearErrors(); 
    
    // Проверка на пустой email
    if (!emailValue) {
        emailError.textContent = 'Email не может быть пустым!';
        emailError.style.display = 'block';
        return false;  // не отправляем форму
    }
    
    // Регулярное выражение для проверки формата email
    const emailRegex = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,6}$/;
    
    // Если email невалидный, показываем ошибку
    if (!emailRegex.test(emailValue)) {
        emailError.textContent = 'Введите корректный email!';
        emailError.style.display = 'block';
        return false;  // не отправляем форму
    }
    
    // Если email валиден, скрываем ошибку
    emailError.style.display = 'none';
    return true;  // отправляем форму
}

// Функция для проверки пароля
function validatePassword() {
    const passwordInput = document.getElementById('password');
    const passwordValue = passwordInput.value;
    const passwordError = document.getElementById('password-error');
    
    // Проверка на пустой пароль
    if (!passwordValue) {
        passwordError.textContent = 'Пароль не может быть пустым!';
        passwordError.style.display = 'block';
        return false;
    }
    
    // Если пароль введен, скрываем ошибку
    passwordError.style.display = 'none';
    return true;
}

// Функция для проверки поля имени
function validateUsername() {
    const usernameInput = document.getElementById('username');
    const usernameValue = usernameInput.value;
    const usernameError = document.getElementById('username-error');
    
    // Проверка на пустое имя
    if (!usernameValue) {
        usernameError.textContent = 'Имя не может быть пустым!';
        usernameError.style.display = 'block';
        return false;
    }
    
    // Если имя введено, скрываем ошибку
    usernameError.style.display = 'none';
    return true;
}

// Функция для проверки совпадения пароля и подтверждения пароля
function validateConfirmPassword() {
    const passwordInput = document.getElementById('password');
    const confirmPasswordInput = document.getElementById('confirm_password');
    const confirmPasswordError = document.getElementById('confirm-password-error');
    
    // Проверка на пустое подтверждение пароля
    if (!confirmPasswordInput.value) {
        confirmPasswordError.textContent = 'Подтверждение пароля не может быть пустым!';
        confirmPasswordError.style.display = 'block';
        return false;
    }
    
    // Если пароли не совпадают, показываем ошибку
    if (passwordInput.value !== confirmPasswordInput.value) {
        confirmPasswordError.textContent = 'Пароли не совпадают!';
        confirmPasswordError.style.display = 'block';
        return false;
    }
    
    // Если пароли совпадают, скрываем ошибку
    confirmPasswordError.style.display = 'none';
    return true;
}

// Функция для обработки отправки формы
function handleFormSubmit(event) {
    // Проверяем имя, email, пароль и совпадение паролей
    if (!validateUsername() || !validateEmail() || !validatePassword() || !validateConfirmPassword()) {
        event.preventDefault();  // предотвращаем отправку формы, если есть ошибка
    }
}
