# --- Estágio 1: Build ---
# Usa uma imagem com Maven e JDK para compilar o código
FROM maven:3.9.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copia o arquivo de configuração do Maven e baixa as dependências
# Isso é feito em uma camada separada para agilizar os builds, pois as dependências
# só serão baixadas novamente se o pom.xml mudar.
COPY pom.xml .
RUN mvn dependency:go-offline

# Copia o código-fonte e faz o build da aplicação
COPY src ./src
RUN mvn clean package -DskipTests

# --- Estágio 2: Runtime ---
# Usa uma imagem menor e mais segura apenas com o JRE para rodar a aplicação
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copia o arquivo .jar gerado no estágio de build
COPY --from=build /app/target/*.jar app.jar

# Expõe a porta que sua aplicação vai usar (importante para o Render)
EXPOSE 8080

# Comando para executar a aplicação, utilizando a porta definida pelo Render
ENTRYPOINT ["java", "-jar", "app.jar", "--server.port=${PORT}"]
