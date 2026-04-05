# Backend CDY2203 — Semana 4

## Arranque

- MySQL con base `mydatabase`, usuario `myuser`, contraseña `password` (o el que definas en `application.properties`).
- Con MySQL en Docker en el puerto **3307**, activa el perfil: `spring.profiles.active=docker`.

## Usuario por defecto (desarrollo)

Si no existe el usuario `admin`, al iniciar se crea uno con contraseña hasheada:

- **Usuario:** `admin`
- **Contraseña:** `admin123`

Login (recomendado):

- `POST /login` con `user=admin` y `password=admin123`

También se acepta el parámetro legacy `encryptedPass` (misma contraseña en claro) por compatibilidad con el ejemplo del curso.

**Importante:** la tabla de usuarios en BD se llama `app_user` (evita conflicto con la palabra reservada `user` en H2/MySQL).

## JWT

La clave de firma sale de `app.jwt.secret` o de la variable de entorno **`JWT_SECRET`** (mínimo 32 caracteres). En Docker Compose del proyecto raíz ya se define un valor de desarrollo.

## Facturas por visita

- Cada factura debe referenciar una **visita** existente: `appointment: { "id": <id_cita> }`.
- Incluye listas de **servicios** (`cares`) y **medicamentos** (`medications`) por id, y opcionalmente **`additionalCharges`**: `[{ "description": "...", "amount": 1234 }]`.
- `GET /invoice/visit/{appointmentId}` lista facturas de esa visita.
