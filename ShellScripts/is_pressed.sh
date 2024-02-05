#!/bin/bash

# Запрашиваем у пользователя ввод логина
read -p "Введите логин пользователя: " username

# Отправляем запрос на сервер, чтобы проверить параметр is_pressed
response=$(curl -s http://localhost:5000/check_is_pressed?username=$username)

# Проверяем ответ и выводим соответствующее сообщение
if [[ $response == *"User is not pressed"* ]]; then
    echo "Кнопка не нажата."
elif [[ $response == *"User is pressed"* ]]; then
    echo "Кнопка нажата."
else
    echo "Не удалось получить информацию о состоянии кнопки."
fi
