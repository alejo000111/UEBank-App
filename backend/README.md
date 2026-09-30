# API UEBank (Node.js + Express + PostgreSQL)

## Puesta en marcha

1. Instalar [Node.js](https://nodejs.org) y [PostgreSQL](https://www.postgresql.org/download/).
2. Crear la base de datos y las tablas:
   ```bash
   createdb -U postgres uebank
   psql -U postgres -d uebank -f schema.sql
   ```
3. Configurar la conexión: copiar `.env.example` a `.env`, poner la clave real de PostgreSQL y generar un `JWT_SECRET` propio (por ejemplo con `openssl rand -hex 32`). Sin `JWT_SECRET` la API no arranca a propósito: es mejor que falle al iniciar a que firme tokens con un secreto adivinable.
4. Instalar dependencias y arrancar:
   ```bash
   npm install
   npm start
   ```
5. Probar: abrir http://localhost:3000/api/health → `{"ok":true}`.

### Si la API no arranca después de clonar

El archivo `.env` **no viene en el repo** (está en `.gitignore`), así que hay que crearlo en cada clon:

| Mensaje de error | Causa y solución |
|---|---|
| `Falta JWT_SECRET en el archivo .env` | No existe `backend/.env`. Copiar `.env.example` a `.env` y poner un `JWT_SECRET` propio. |
| `Falta DATABASE_URL en el archivo .env` | Igual: completar `DATABASE_URL` en `.env`. |
| `No se pudo conectar con PostgreSQL` | PostgreSQL no está corriendo, la clave es incorrecta o la base `uebank` no existe (`createdb -U postgres uebank`). |
| `Cannot find module ...` | Falta correr `npm install` dentro de `backend/`. |

## Conectar la app a esta API

**Cada integrante del equipo necesita la API corriendo para ver saldos, cuentas, movimientos y metas.** Sin ella, la app funciona igual (login, registro, beneficiarios, foto) pero muestra "No disponible" o "No se pudo conectar con el servidor" en esas pantallas — eso es justamente lo que se ve si a alguien "no le guarda el saldo": no le falta nada en la app, le falta correr este backend.

La URL de la API se configura en `local.properties` (raíz del proyecto Android), **no en el código** — así cada quien apunta a la suya sin generar conflictos de Git. Ver la plantilla en [`local.properties.example`](../local.properties.example). Opciones, de más simple a más cómoda para trabajar en equipo:

1. **Cada quien corre su propia API + su propio PostgreSQL** (lo descrito arriba) y prueba en el emulador con el valor por defecto (`http://10.0.2.2:3000/api/`). Ventaja: no depende de que nadie más tenga el computador prendido. Desventaja: cada uno ve sus propios datos, no los mismos que sus compañeros.
2. **Un celular físico** en la misma red WiFi que el computador con la API: usar la IP de ese computador (`API_BASE_URL=http://192.168.x.x:3000/api/` en `local.properties`) y agregar esa misma IP en `app/src/main/res/xml/network_security_config.xml` (Android bloquea HTTP sin cifrar hacia hosts no listados ahí).
3. **Recomendado para probar todos juntos:** desplegar esta API en un servicio gratuito con PostgreSQL incluido (por ejemplo [Render](https://render.com) o [Railway](https://railway.app)) y que todo el equipo apunte su `local.properties` a esa URL pública HTTPS. Así todos ven la misma base de datos, nadie necesita instalar PostgreSQL, y al ser HTTPS tampoco hace falta tocar `network_security_config.xml`. Si quieren, puedo preparar el `Dockerfile`/`render.yaml` para dejarlo listo.

## Autenticación (JWT)

Desde esta versión, `cuentas`, `movimientos` y `metas` exigen un token: toda
petición debe llevar el encabezado `Authorization: Bearer <token>`. Sin él
(o con uno inválido/vencido) la API responde `401`; con un token válido pero
de otro usuario, responde `403` (o `404` en rutas por id, para no revelar
que el recurso existe). `GET /api/health` y `POST /api/auth/*` siguen
siendo públicos: son la puerta de entrada para conseguir el token.

| Método y ruta | Body | Descripción |
|---|---|---|
| `POST /api/auth/registro` | `{usuario, hash}` | Da de alta las credenciales en la API y devuelve `{token}` |
| `POST /api/auth/login` | `{usuario, hash}` | Verifica el hash y devuelve `{token}` (expira en 24h) |

`hash` es el mismo `"salt:hash"` en Base64 que `PasswordUtils` ya calcula en
el teléfono (PBKDF2 + sal) — la contraseña real nunca sale del dispositivo
ni llega a la API en texto plano. La app llama `/login` **después** de
verificar la contraseña localmente contra su SQLite; ver el comentario en
`src/routes/auth.js` para el detalle de por qué esto sigue siendo una
verificación "de tránsito" y no una autenticación 100% del lado del
servidor (esa es la siguiente mejora pendiente: mover el login completo a
la API).

## Endpoints (3 CRUD)

Todos devuelven y reciben JSON. Los errores llegan como `{"error": "mensaje"}`. Estas tres rutas requieren el token (ver arriba).

| Recurso | Método y ruta | Descripción |
|---|---|---|
| Cuentas | `GET /api/cuentas?usuario=` | Lista las cuentas del usuario |
| | `GET /api/cuentas/:id` | Una cuenta |
| | `POST /api/cuentas` | Crea `{usuario, numero, tipo, saldo}` |
| | `PUT /api/cuentas/:id` | Edita `{numero, tipo}` (el saldo solo cambia con movimientos) |
| | `DELETE /api/cuentas/:id` | Elimina (y sus movimientos, en cascada) |
| Movimientos | `GET /api/movimientos?usuario=[&cuenta_id=]` | Lista movimientos |
| | `POST /api/movimientos` | Crea `{cuenta_id, tipo: DEPOSITO\|RETIRO, monto, descripcion}` y actualiza el saldo (transacción) |
| | `PUT /api/movimientos/:id` | Edita la descripción |
| | `DELETE /api/movimientos/:id` | Anula y revierte el saldo |
| Metas | `GET /api/metas?usuario=` | Lista metas |
| | `POST /api/metas` | Crea `{usuario, nombre, monto_objetivo, monto_ahorrado, fecha_limite}` |
| | `PUT /api/metas/:id` | Edita la meta |
| | `DELETE /api/metas/:id` | Elimina |

## Estructura

```
backend/
├── schema.sql            tablas de PostgreSQL
└── src/
    ├── server.js         Express, rutas y manejo global de errores
    ├── db.js             conexión (pool) a PostgreSQL
    ├── auth.js            firma y verifica los JWT (requerirToken, exigirDueño)
    ├── validar.js        validación de campos
    ├── httpError.js      error con código HTTP
    └── routes/           auth.js · cuentas.js · movimientos.js · metas.js
```

## Limitaciones conocidas

- **El login completo sigue sin vivir en la API.** `/api/auth/login` confía en que la app ya verificó la contraseña contra el SQLite del teléfono (le pasa el mismo hash, nunca la contraseña); la primera vez que un usuario llama a `/login`, la API se limita a registrar ese hash como "el suyo" (modelo de confianza en el primer uso). Migrar el registro/login completo a la API, para que sea ella la que verifique la contraseña de forma independiente, sigue siendo la mejora pendiente más importante.
- HTTP sin cifrar: solo aceptable en desarrollo local. En producción, HTTPS.
