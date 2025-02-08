# Используем официальный образ OpenJDK 21 для сборки
FROM eclipse-temurin:21-jdk AS builder

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем файлы проекта в контейнер
COPY . .

# Сборка приложения с Gradle
RUN ./gradlew build -x test

# Используем официальный образ OpenJDK 21 для запуска
FROM eclipse-temurin:21-jre

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем скомпилированный JAR-файл из предыдущего этапа
COPY --from=builder /app/build/libs/*.jar app.jar

# Открываем порт, на котором работает приложение
EXPOSE 8080

# Запускаем приложение
ENTRYPOINT ["java", "-jar", "app.jar"]