# syntax=docker/dockerfile:1

# ---------- Etapa de build ----------
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /workspace

# Primero solo los archivos de Gradle: si el codigo cambia, esta capa se reutiliza.
# gradle.properties entra aqui porque define jvmargs/paralelismo del build; si queda
# fuera, la capa cacheada se invalida por una razon que no afecta las dependencias.
COPY gradle ./gradle
COPY gradlew settings.gradle build.gradle gradle.properties ./
# Solo el classpath de runtime: src/test no se copia (ver .dockerignore) y no tiene
# sentido descargar las dependencias de test que el build luego no usa.
RUN chmod +x gradlew \
    && ./gradlew --no-daemon --console=plain dependencies --configuration runtimeClasspath > /dev/null

COPY src/main ./src/main
RUN ./gradlew --no-daemon --console=plain bootJar -x test \
    && cp "$(find build/libs -maxdepth 1 -name '*.jar' ! -name '*-plain.jar' | head -1)" app.jar

# ---------- Etapa de ejecucion ----------
# Liberica JRE alpine (multi-arch: amd64 y arm64) pesa ~157 MB y trae wget de
# busybox para el healthcheck. La alpine de Temurin 17 solo existe para amd64.
FROM bellsoft/liberica-openjre-alpine-musl:17

RUN addgroup -S app && adduser -S -G app app

WORKDIR /app
# --chown evita el `chown -R /app` posterior: en una imagen por capas ese chown
# duplicaba el jar de 63 MB en una capa nueva.
COPY --chown=app:app --from=build /workspace/app.jar app.jar

# Perfil de produccion: application-prod.properties lee todo de variables de
# entorno (DB_URL, DB_USER, DB_PASSWORD, JWT_SECRET, CORS_ORIGINS, PORT).
# Sin esto el contenedor arranca con application.properties y trata de conectarse
# a jdbc:postgresql://localhost:5432/easycar con el secreto JWT del repositorio.
# Se puede sobreescribir definiendo SPRING_PROFILES_ACTIVE en Northflank.
ENV SPRING_PROFILES_ACTIVE=prod

# Reporte de arranque: dice en el log que variables de entorno llegaron al
# contenedor (nombre y tamano, nunca el valor) y muestra el DB_URL con la
# contrasena enmascarada. Sirve para distinguir "la variable no llega" de
# "la variable llega pero la conexion falla". Para quitarlo: borrar este RUN y
# volver al ENTRYPOINT de abajo.
RUN printf '%s\n' \
    '#!/bin/sh' \
    'echo "[env] SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-<vacio>}"' \
    'for v in DB_URL DB_USER DB_PASSWORD JWT_SECRET CORS_ORIGINS DB_POOL_MAX; do' \
    '  if printenv "$v" > /dev/null 2>&1; then' \
    '    echo "[env] $v = set ($(printenv "$v" | wc -c | tr -d " ") bytes)"' \
    '  else' \
    '    echo "[env] $v = MISSING"' \
    '  fi' \
    'done' \
    'if [ -n "$DB_URL" ]; then' \
    '  echo "[env] DB_URL = $(echo "$DB_URL" | sed "s#://[^@/]*@#://***@#")"' \
    'fi' \
    'exec java $JAVA_OPTS -jar /app/app.jar' \
    > /usr/local/bin/start.sh \
    && chmod 755 /usr/local/bin/start.sh

USER app
EXPOSE 8080

# Ajustes para contenedores de 512 MB - 1 GB:
#  - MaxRAMPercentage=65: la JVM solo controla el heap; metaspace, code cache,
#    stacks y buffers viven fuera de el y con 75 % el contenedor quedaba al borde
#    del OOM-kill (512 MB -> 384 MB de heap).
#  - InitialRAMPercentage=20: con 50 % la JVM reservaba 256 MB de heap en un
#    contenedor de 512 MB desde el arranque, aunque no hubiera trafico.
#  - MaxMetaspaceSize: acota la unica region que podia crecer sin limite.
#  - SerialGC: menos overhead que G1 en 1 nucleo.
#  - TieredStopAtLevel=1: arranque mas rapido con C1, suficiente para este trafico.
#  - ExitOnOutOfMemoryError: el contenedor reinicia en vez de quedar colgado.
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=65 -XX:InitialRAMPercentage=20 -XX:MaxMetaspaceSize=160m -XX:+ExitOnOutOfMemoryError -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -Xss512k -Djava.awt.headless=true"

HEALTHCHECK --interval=30s --timeout=5s --start-period=120s --retries=3 \
  CMD wget -qO- "http://127.0.0.1:${PORT:-8080}/health/status" > /dev/null || exit 1

# JAVA_OPTS queda como punto de extension: Northflank puede agregar flags sin
# reconstruir la imagen (los flags de JAVA_OPTS se aplican despues de
# JAVA_TOOL_OPTIONS, asi que tienen prioridad).
# start.sh imprime el reporte [env] y luego hace exec de la JVM.
ENTRYPOINT ["/usr/local/bin/start.sh"]
