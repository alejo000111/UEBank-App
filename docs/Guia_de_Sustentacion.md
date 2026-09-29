# Guía de sustentación — UEBank Móvil

## 1. Qué es (30 segundos)

UEBank es una app bancaria Android en **Java** conectada a una **API REST en Node.js** con **PostgreSQL**. El cliente se registra, inicia sesión (contraseña con hash), toma su foto de perfil con la **cámara**, guarda beneficiarios en **SQLite** importándolos desde sus **contactos**, y administra cuentas, movimientos y metas de ahorro contra la API.

## 2. Cumplimiento de los requisitos

| Requisito | Dónde está |
|---|---|
| Acta PMBOK | `docs/Acta_de_Constitucion_UEBank.md` (información, justificación, descripción, objetivos, Gantt, presupuesto) |
| Login + seguridad (hash) | `LoginActivity`, `RegistroActivity`, `security/PasswordUtils` (PBKDF2 + sal) |
| CRUD en SQLite | `data/BeneficiarioRepository` (Create, Read, Update, Delete) |
| SharedPreferences y archivos | `managers/SessionManager` (sesión) · `managers/FotoPerfilManager` (foto .jpg) |
| Dos recursos del dispositivo | **Cámara** (`PerfilActivity`) y **Contactos** (`BeneficiariosActivity`) |
| API + PostgreSQL + 3 CRUD | `backend/` → cuentas, movimientos, metas · consumidos por `CuentasActivity`, `MovimientosActivity`, `MetasActivity` |

## 3. Arquitectura

```
┌─────────────── App Android (Java) ───────────────┐          ┌──── Servidor ────┐
│ Activities (UI)                                  │          │ Node.js + Express│
│   └─ ListaBaseActivity (esqueleto común)         │  HTTP    │  routes/         │
│ data/   SQLite  ── usuarios, beneficiarios       │  JSON    │  (cuentas,       │
│ managers/ SharedPreferences, archivos            │ ───────▶ │   movimientos,   │
│ api/    Retrofit ── cuentas, movimientos, metas  │ ◀─────── │   metas)         │
│ security/ PBKDF2                                 │          │       │          │
└──────────────────────────────────────────────────┘          │  PostgreSQL      │
                                                              └──────────────────┘
```

**Qué vive dónde y por qué**

- **Local (SQLite):** usuarios y beneficiarios. Funcionan sin internet; la sesión y la contraseña nunca salen del teléfono.
- **Remoto (PostgreSQL vía API):** cuentas, movimientos y metas. Son datos "del banco" que deben ser consistentes y no depender del teléfono.

## 4. Metodologías y patrones (lo que se pregunta más)

- **PMBOK:** gestión del proyecto con acta de constitución, cronograma (Gantt) y presupuesto. Pertenece al grupo de procesos de *Inicio*.
- **Arquitectura por capas y patrón Repository:** las Activities (UI) no escriben SQL; solo llaman a `UsuarioRepository` / `BeneficiarioRepository`. Ventaja: si se cambia SQLite por Room, la UI casi no cambia.
- **Patrón Contract:** `UsuarioContract` / `BeneficiarioContract` centralizan nombres de tablas y columnas para evitar errores de tipeo.
- **Template Method (`ListaBaseActivity`):** la clase base hace lo repetitivo (layout, RecyclerView, indicador de carga, botón agregar, mensaje de vacío) y las 4 pantallas solo implementan `titulo()`, `subtitulo()`, `cargarDatos()`, `onAgregar()` y `onItemSeleccionado()`. Se ahorró código duplicado.
- **Singleton (`ApiClient`):** una sola instancia de Retrofit para toda la app.
- **Única fuente de verdad para el saldo (`SaldoTotal`):** ni el panel ni el perfil guardan un número de saldo propio; ambos lo consultan a la API (suma de las cuentas del cliente) cada vez que se muestran. Evita que dos pantallas —o dos celulares del mismo cliente— muestren saldos distintos.
- **API REST:** verbos HTTP = operaciones CRUD (`POST` crea, `GET` lee, `PUT` actualiza, `DELETE` elimina); códigos de estado (201, 204, 400, 404, 409); datos en JSON.
- **Principio de responsabilidad única:** cada clase hace una cosa (hash, sesión, foto, SQL, formato).

## 5. Tecnologías

| Tecnología | Para qué se usa |
|---|---|
| Java 11, Android SDK (minSdk 26) | La app |
| Material Components, ConstraintLayout | Interfaz |
| **RecyclerView + SwipeRefreshLayout** | Las 4 pantallas de listado: reciclan las filas que salen de pantalla y muestran el indicador de carga (también sirve para "deslizar para refrescar") |
| **SplashScreen (androidx.core.splashscreen)** | Pantalla de bienvenida al abrir la app |
| **SQLite** (`SQLiteOpenHelper`) | Base local: `usuarios`, `beneficiarios` (versión 3, con migraciones en `onUpgrade`) |
| **SharedPreferences** | Sesión y "Recordar sesión" |
| **Almacenamiento interno (archivos)** | Foto de perfil, privada de la app |
| **PBKDF2WithHmacSHA256** (120.000 iteraciones + sal aleatoria) | Hash de contraseñas |
| **ActivityResult API** | Permiso de cámara, cámara y selector de contactos |
| **Retrofit 2 + Gson** | Cliente HTTP y conversión JSON ↔ objetos Java |
| **Node.js + Express 5** | API REST |
| **PostgreSQL** (`pg`) | Base de datos del servidor, con `CHECK`, `UNIQUE`, `FOREIGN KEY` |
| JUnit | Pruebas unitarias del hash (`PasswordUtilsTest`) |

## 6. Puntos técnicos para explicar

### 6.1 Seguridad de la contraseña
1. No se guarda la contraseña: se guarda `sal:hash` en Base64.
2. La **sal** (16 bytes aleatorios por usuario) evita que dos usuarios con la misma clave tengan el mismo hash y frena las tablas precalculadas.
3. **PBKDF2 con 120.000 iteraciones** es deliberadamente lento: encarece la fuerza bruta. Un SHA-256 simple es demasiado rápido.
4. La verificación usa `MessageDigest.isEqual` (**tiempo constante**) para evitar ataques de temporización.
5. Las consultas SQL usan parámetros `?` (**previene SQL injection**).

### 6.2 SQLite y migraciones
- La base pasó de versión 1 a 2 al agregar `beneficiarios` (`CREATE TABLE IF NOT EXISTS`, sin tocar los usuarios existentes) y de 2 a 3 al **quitar la columna `saldo`** de `usuarios` (ver 6.3). SQLite no permite `DROP COLUMN` en todas las versiones de Android, así que esa migración recrea la tabla: la renombra, crea la nueva sin esa columna, copia los datos de las demás columnas y borra la tabla vieja. Ningún usuario registrado pierde sus datos.
- `onConfigure` activa las llaves foráneas (SQLite las ignora por defecto): al borrar un usuario se borran sus beneficiarios (`ON DELETE CASCADE`).

### 6.3 Saldo unificado (una sola fuente de verdad)
Antes, el panel principal mostraba `usuarios.saldo` (un número guardado en SQLite) y las cuentas de la API tenían su propio saldo independiente: dos números que no se comunicaban entre sí. Ahora:
- Se **eliminó** la columna `saldo` de la tabla `usuarios` (migración de la base a versión 3).
- El panel principal y el perfil consultan `GET /api/cuentas` y muestran la **suma de los saldos de todas las cuentas del cliente** (clase `SaldoTotal`).
- Mientras llega la respuesta se muestra "Cargando…"; si no hay conexión, "No disponible (sin conexión)" en vez de un número desactualizado o inventado.
- Ventaja para la sustentación: se puede preguntar "¿por qué hay saldo en dos lados?" y la respuesta ahora es "ya no lo hay: hay una sola fuente de verdad, la API".

### 6.4 Recursos del dispositivo
- **Cámara:** `CAMERA` es un permiso peligroso → se declara en el manifest y se pide en tiempo de ejecución (`checkSelfPermission` + `RequestPermission`). La foto se guarda como archivo y en la BD solo va la ruta. `uses-feature required="false"` permite instalar en teléfonos sin cámara.
- **Contactos:** se usa el selector del sistema (`ACTION_PICK`). Android otorga acceso **temporal solo al contacto elegido**, por eso no se pide `READ_CONTACTS`: mínimo privilegio, y el usuario no ve un permiso alarmante. *Lógica del negocio:* un beneficiario suele ser alguien de la agenda.

### 6.5 API y consistencia del dinero
- **El servidor manda el saldo:** el cliente nunca envía "nuevo saldo"; envía un movimiento y la API lo calcula.
- **Transacción (`BEGIN … COMMIT/ROLLBACK`):** el movimiento y el cambio de saldo se guardan juntos o no se guarda nada.
- **`SELECT … FOR UPDATE`:** bloquea la fila de la cuenta para que dos movimientos simultáneos no corrompan el saldo.
- Un retiro mayor al saldo se rechaza (400), y la base tiene `CHECK (saldo >= 0)` como segunda barrera.
- Anular un movimiento revierte el saldo (y se rechaza si lo dejaría negativo).
- Los errores llegan como `{"error":"..."}` y la app los muestra tal cual (`ApiCallback`).

### 6.6 Comunicación app–API
- Retrofit ejecuta las llamadas en un hilo secundario (`enqueue`) y devuelve el resultado al hilo principal: la pantalla no se congela.
- `10.0.2.2` es el "localhost" del computador visto desde el emulador.
- Android bloquea HTTP sin cifrar por defecto; `network_security_config.xml` lo permite **solo** para `10.0.2.2` y `localhost` (desarrollo). Es una decisión deliberada solo para poder probar contra la API local sin montar un servidor HTTPS; en producción se retira este permiso.
- La URL de la API (`ApiClient.BASE_URL`) sale de `local.properties` (vía `BuildConfig.API_BASE_URL`), **no está escrita en el código**: cada integrante del equipo apunta a su propio backend sin generar conflictos de Git al compartir el repositorio. Ver `local.properties.example` y `backend/README.md`.

### 6.7 Detalles de experiencia de usuario (UX)
- **Indicador de carga:** las 4 pantallas de listado usan `SwipeRefreshLayout`: se ve girando mientras se cargan los datos (al entrar) y el usuario también puede deslizar hacia abajo para refrescar manualmente.
- **Validación de formato en el cliente:** el número de cuenta (4 a 20 dígitos) y la fecha límite de una meta (`AAAA-MM-DD`, con fecha real: rechaza `2026-02-30`) se validan **antes** de llamar a la API (`Formato.numeroCuentaValido` / `Formato.fechaValida`). Evita una ida y vuelta de red innecesaria cuando el error ya es obvio en el teléfono; el servidor igual vuelve a validar todo (nunca hay que confiar solo en el cliente).
- **Pantalla de bienvenida (splash screen):** con la librería `androidx.core.splashscreen`, usando el mismo ícono y color del banco.
- **RecyclerView en vez de ListView:** recicla las filas que salen de la pantalla (patrón ViewHolder) en vez de crear vistas nuevas; es el estándar actual de Android para listas.

## 7. Demostración sugerida (5 minutos)

1. Arrancar la API (`npm start`) y el emulador.
2. **Registrarse** → mostrar que entra directo al panel. Cerrar sesión e **ingresar** con "Recordar sesión"; cerrar y abrir la app (salta el login).
3. **Perfil → Tomar foto** (aparece el permiso de cámara) → volver a entrar: la foto persiste.
4. **Beneficiarios (SQLite + contactos):** agregar desde un contacto, editar, eliminar.
5. **Mis cuentas** (API): crear una cuenta con saldo → **Ver movimientos**.
6. **Movimientos:** depósito, luego un retiro mayor al saldo (error del servidor), luego un retiro válido; volver a Cuentas y mostrar el saldo actualizado.
7. **Metas:** crear, editar el ahorro, ver el porcentaje, eliminar.
8. (Opcional) En `psql`: `SELECT * FROM cuentas;` para mostrar que los datos están realmente en PostgreSQL.

## 8. Preguntas probables y respuestas

**¿Por qué no guardan la contraseña con SHA-256 simple?** Porque es rápido y sin sal se rompe con tablas precalculadas; PBKDF2 con sal e iteraciones lo hace costoso.

**¿Por qué SQLite nativo y no Room?** Para entender la base del acceso a datos (SQL, cursores, migraciones). Room genera ese mismo código; migrar es un paso natural y gracias al patrón Repository el cambio se limita a la capa `data`.

**¿Por qué el saldo lo calcula el servidor?** Por seguridad y consistencia: un cliente modificado podría enviar cualquier saldo. Con transacciones y `FOR UPDATE` se evitan también condiciones de carrera.

**¿Qué pasa si no hay internet?** Login, registro y beneficiarios siguen funcionando por completo (son locales). En perfil, la foto sigue funcionando pero el saldo muestra "No disponible" porque se consulta a la API a propósito (ver 6.3.1). Las pantallas de cuentas, movimientos y metas muestran "No se pudo conectar con el servidor".

**¿Por qué no piden permiso de contactos?** Porque el selector del sistema da acceso solo al contacto elegido (principio de mínimo privilegio).

**¿Qué mejoraría con más tiempo?** *(Sé honesto: son limitaciones reales.)*
1. **Autenticación en la API con JWT:** hoy la API identifica al dueño por el parámetro `usuario`, y no hay tokens; otro cliente podría consultar datos ajenos. Es la limitación de seguridad más importante.
2. **Mover el login/registro también a la API:** hoy el usuario y su contraseña viven solo en el SQLite del teléfono; si el cliente cambia de celular, pierde la cuenta. Lo ideal es que también pasen por el backend y SQLite quede como una caché local.
3. HTTPS en producción y despliegue de la API.
4. Migrar de SQLite nativo a Room + ViewModel/LiveData.
5. Pruebas automáticas de la API (por ejemplo con `supertest`) y pruebas instrumentadas de la interfaz (Espresso).

## 9. Reparto sugerido del equipo (ajústenlo a la realidad)

| Integrante | Módulos |
|---|---|
| 1 | Login, registro, seguridad (PBKDF2), consumo de la API (Retrofit) |
| 2 | SQLite, sesión, foto (cámara), beneficiarios y contactos |
| 3 | API Node.js, PostgreSQL, transacciones, documentación de endpoints |

**Cada integrante debe poder explicar el proyecto completo**, no solo su parte.

## 10. Cómo ejecutar todo

```bash
# 1) Base de datos y API
createdb -U postgres uebank
psql -U postgres -d uebank -f backend/schema.sql
cd backend && cp .env.example .env    # editar la clave de PostgreSQL
npm install && npm start

# 2) App: abrir el proyecto en Android Studio y ejecutar en un emulador
```
