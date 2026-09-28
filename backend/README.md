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

El emulador de Android accede a esta API como `http://10.0.2.2:3000/api/`.
Con un celular físico, usar la IP del computador en la red WiFi (y agregarla en `app/src/main/res/xml/network_security_config.xml`).

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
