#!/bin/bash

# Запрашиваем у пользователя ввод пароля и логина
read -p "Введите логин: " username
read -p "Введите пароль: " password

# Отправляем запрос на создание пользователя
curl -X POST -H "Content-Type: application/json" -d "{\"password\": \"$password\", \"username\": \"$username\"}" http://localhost:5000/user
