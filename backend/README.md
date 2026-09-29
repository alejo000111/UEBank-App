# API UEBank (Node.js + Express + PostgreSQL)

## Puesta en marcha

1. Instalar [Node.js](https://nodejs.org) y [PostgreSQL](https://www.postgresql.org/download/).
2. Crear la base de datos y las tablas:
   ```bash
   createdb -U postgres uebank
   psql -U postgres -d uebank -f schema.sql
   ```
3. Configurar la conexión: copiar `.env.example` a `.env` y poner la clave real de PostgreSQL.
4. Instalar dependencias y arrancar:
   ```bash
   npm install
   npm start
   ```
5. Probar: abrir http://localhost:3000/api/health → `{"ok":true}`.

## Conectar la app a esta API

**Cada integrante del equipo necesita la API corriendo para ver saldos, cuentas, movimientos y metas.** Sin ella, la app funciona igual (login, registro, beneficiarios, foto) pero muestra "No disponible" o "No se pudo conectar con el servidor" en esas pantallas — eso es justamente lo que se ve si a alguien "no le guarda el saldo": no le falta nada en la app, le falta correr este backend.

La URL de la API se configura en `local.properties` (raíz del proyecto Android), **no en el código** — así cada quien apunta a la suya sin generar conflictos de Git. Ver la plantilla en [`local.properties.example`](../local.properties.example). Opciones, de más simple a más cómoda para trabajar en equipo:

1. **Cada quien corre su propia API + su propio PostgreSQL** (lo descrito arriba) y prueba en el emulador con el valor por defecto (`http://10.0.2.2:3000/api/`). Ventaja: no depende de que nadie más tenga el computador prendido. Desventaja: cada uno ve sus propios datos, no los mismos que sus compañeros.
2. **Un celular físico** en la misma red WiFi que el computador con la API: usar la IP de ese computador (`API_BASE_URL=http://192.168.x.x:3000/api/` en `local.properties`) y agregar esa misma IP en `app/src/main/res/xml/network_security_config.xml` (Android bloquea HTTP sin cifrar hacia hosts no listados ahí).
3. **Recomendado para probar todos juntos:** desplegar esta API en un servicio gratuito con PostgreSQL incluido (por ejemplo [Render](https://render.com) o [Railway](https://railway.app)) y que todo el equipo apunte su `local.properties` a esa URL pública HTTPS. Así todos ven la misma base de datos, nadie necesita instalar PostgreSQL, y al ser HTTPS tampoco hace falta tocar `network_security_config.xml`. Si quieren, puedo preparar el `Dockerfile`/`render.yaml` para dejarlo listo.

## Endpoints (3 CRUD)

Todos devuelven y reciben JSON. Los errores llegan como `{"error": "mensaje"}`.

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
    ├── validar.js        validación de campos
    ├── httpError.js      error con código HTTP
    └── routes/           cuentas.js · movimientos.js · metas.js
```

## Limitaciones conocidas

- La API identifica al dueño por el parámetro `usuario` que envía la app; **no hay tokens (JWT)**, así que cualquiera que conozca la API podría pedir datos de otro usuario. Es la primera mejora de seguridad pendiente.
- HTTP sin cifrar: solo aceptable en desarrollo local. En producción, HTTPS.
