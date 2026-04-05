# Receta: levantar MySQL con Docker Compose para el backend

Este stack sustituye la imagen personalizada de `docker/mysql/Dockerfile`: aquí se usa la imagen oficial `mysql:8.0` y toda la configuración vive en `compose/docker-compose.yml`.

## Requisitos

- Docker Desktop (o Docker Engine) con **Docker Compose v2**.
- El backend Spring usa el perfil **`docker`** y espera MySQL en **`localhost:3307`** (ver `application-docker.properties`).

## 1. Preparar secretos (solo en tu máquina)

En la carpeta `compose/`:

1. Copia el ejemplo de variables:
   ```bash
   copy .env.example .env
   ```
   (En PowerShell o CMD desde `compose`. En macOS/Linux: `cp .env.example .env`.)

2. Edita **`.env`** y pon contraseñas fuertes:
   - `MYSQL_ROOT_PASSWORD`: usuario root de MySQL.
   - `MYSQL_PASSWORD`: contraseña del usuario de aplicación **`myuser`** (es la que usará Spring).

**No subas `.env` a Git** (está ignorado en el repositorio).

## 2. Arrancar MySQL

Desde la carpeta **`compose`**:

```bash
docker compose up -d
```

Comprobar estado:

```bash
docker compose ps
```

Esperar a que el contenedor esté **healthy** (healthcheck en el compose).

## 3. Arrancar el backend Spring

Las credenciales **no** van en `application.properties` (evita hallazgos Blocker en Sonar). Spring Boot usa el binding estándar:

- **`SPRING_DATASOURCE_PASSWORD`**: misma valor que **`MYSQL_PASSWORD`** del `.env`.
- **`SPRING_DATASOURCE_USERNAME`**: normalmente **`myuser`** (debe coincidir con `MYSQL_USER` del compose).

**PowerShell** (desde la raíz del proyecto backend):

```powershell
$env:SPRING_DATASOURCE_USERNAME = "myuser"
$env:SPRING_DATASOURCE_PASSWORD = "la_misma_que_MYSQL_PASSWORD_en_tu_env"
mvn spring-boot:run "-Dspring-boot.run.profiles=docker"
```

Perfil **por defecto** (MySQL en `localhost:3306`): mismas variables antes de `mvn spring-boot:run`.

## 4. Parar MySQL (sin borrar datos)

```bash
cd compose
docker compose stop
```

## 5. Parar y eliminar contenedor (los datos siguen en el volumen)

```bash
docker compose down
```

## 6. Parar y borrar también los datos del volumen

```bash
docker compose down -v
```

Solo si quieres empezar la base desde cero.

## Desde la raíz del repo (sin entrar a `compose`)

```bash
docker compose -f compose/docker-compose.yml --env-file compose/.env up -d
```

(Asegúrate de tener creado `compose/.env`.)
