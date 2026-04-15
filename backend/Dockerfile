# ==========================================
# ESTÁGIO 1: Build (Compilação)
# ==========================================
# Usa uma imagem oficial do Maven com Java 17
FROM maven:3.9.6-eclipse-temurin-25 AS build
WORKDIR /app

# Copia o arquivo de dependências e baixa o que for necessário
COPY pom.xml .
# Copia o código-fonte do CASIS
COPY src ./src

# Compila o projeto ignorando os testes para ser mais rápido
RUN mvn clean package -DskipTests

# ==========================================
# ESTÁGIO 2: Run (Execução)
# ==========================================
# Usa uma imagem muito mais leve apenas com o ambiente de execução do Java
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

# Pega o arquivo .jar que foi gerado no Estágio 1 e joga para este estágio final
COPY --from=build /app/target/*.jar app.jar

# Expõe a porta que o Spring Boot usa
EXPOSE 8080

# Comando que o servidor vai rodar para iniciar a API
ENTRYPOINT ["java", "-jar", "app.jar"]