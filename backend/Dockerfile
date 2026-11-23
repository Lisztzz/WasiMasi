# Web Services de WasiMasi para Render (o cualquier servicio con Docker).
# Etapa 1: compila el JAR con Maven y Java 21.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q -DskipTests package

# Etapa 2: imagen liviana solo con el JRE y el JAR.
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --uid 1001 wasimasi
COPY --from=build /app/target/wasimasi-api-*.jar app.jar
USER wasimasi
# Render asigna el puerto en la variable PORT; la app la lee en application.yml.
# Ajustes para el plan gratuito de Render (512 MB): probado con ~350 MB en uso tras un flujo completo.
ENV JAVA_TOOL_OPTIONS="-Xmx280m -XX:MaxMetaspaceSize=160m -XX:ReservedCodeCacheSize=64m -Xss512k -XX:+UseSerialGC -XX:TieredStopAtLevel=1"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
