# ==========================================
# ESTÁGIO 1: Build (Compilação)
# ==========================================
# Usamos uma tag genérica do Maven (3.9) que mapeia automaticamente para a versão compatível com Java 25
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# ==========================================
# ESTÁGIO 2: Run (Execução Segura)
# ==========================================
# Usando a imagem da Microsoft OpenJDK 25 Distroless
FROM mcr.microsoft.com/openjdk/jdk:25-distroless
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# Distroless não tem shell, então passamos o comando de forma direta
ENTRYPOINT ["java", "-Djava.net.preferIPv4Stack=true", "-jar", "app.jar"]