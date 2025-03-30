document.addEventListener("DOMContentLoaded", function() {
    const leverBox = document.getElementById('toggleLever');
    const isPressed = leverBox.getAttribute("data-is-pressed") === "true"; // Получаем начальное состояние

    // Устанавливаем начальное состояние рубильника
    if (isPressed) {
        leverBox.classList.add("active");
    }

    // Переключение состояния по клику
    leverBox.addEventListener("click", function() {
        this.classList.toggle("active");

        const newState = leverBox.classList.contains('active') ? 1 : 0;

        // Отправка состояния на сервер
        fetch('/main', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ is_pressed: newState })
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Ошибка HTTP: ' + response.status);
            }
            return response.json();
        })
        .then(data => {
            console.log('Успех:', data);
        })
        .catch((error) => {
            console.error('Ошибка:', error);
        });
    });
});