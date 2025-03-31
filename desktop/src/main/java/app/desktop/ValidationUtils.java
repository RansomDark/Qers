package app.desktop;

public class ValidationUtils {
    public static String validatePassword(String password) {
        // Минимальная длина пароля
        int minLength = 8;

        // Проверка наличия строчных букв
        boolean containsLowerCase = password.matches(".*[a-z].*");

        // Проверка наличия прописных букв
        boolean containsUpperCase = password.matches(".*[A-Z].*");

        // Проверка наличия цифр
        boolean containsDigit = password.matches(".*\\d.*");

        // Проверка наличия специальных символов
        boolean containsSpecialChar = password.matches(".*[!@#$%^&*()-_=+{};:,<.>/?\\[\\]\\\\].*");

        // Проверка минимальной длины и наличия всех критериев
        if (password.length() < minLength) {
            return "Пароль должен содержать не менее " + minLength + " символов";
        } else if (!containsLowerCase) {
            return "Пароль должен содержать строчные буквы";
        } else if (!containsUpperCase) {
            return "Пароль должен содержать заглавные буквы";
        } else if (!containsDigit) {
            return "Пароль должен содержать цифры";
        } else if (!containsSpecialChar) {
            return "Пароль должен содержать специальные символы";
        }

        // Пароль прошел все проверки
        return null;
    }

    public static boolean isValidEmail(String email) {
        return email.matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
    }
}
