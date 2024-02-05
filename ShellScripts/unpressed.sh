#!/bin/bash

# Запрашиваем у пользователя ввод логина
read -p "Введите логин пользователя: " username

id=$(curl http://localhost:5000/get_user_id_by_username?username=$username)

# Отправляем PUT-запрос на сервер для изменения параметра is_pressed
curl -X PUT -H "Content-Type: application/json" -d '{"is_pressed": false}' http://localhost:5000/user/$id

echo "Состояние кнопки для пользователя $username успешно изменено на false."
