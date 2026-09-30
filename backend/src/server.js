require('dotenv').config();
const express = require('express');
const cors = require('cors');

const app = express();
app.use(cors());
app.use(express.json());

const { requerirToken } = require('./auth');

app.get('/api/health', (req, res) => res.json({ ok: true }));

// Público: aquí es donde se consigue el token que las demás rutas exigen.
app.use('/api/auth', require('./routes/auth'));

// A partir de aquí, toda ruta exige "Authorization: Bearer <token>" válido
// (ver src/auth.js). Antes, cualquiera que conociera la API podía pedir los
// datos de cualquier "usuario" con solo poner su nombre en la URL; ahora
// hace falta un token emitido para ESE usuario (JWT_SECRET firma cada uno).
app.use('/api/cuentas', requerirToken, require('./routes/cuentas'));
app.use('/api/movimientos', requerirToken, require('./routes/movimientos'));
app.use('/api/metas', requerirToken, require('./routes/metas'));

app.use((req, res) => res.status(404).json({ error: 'Ruta no encontrada' }));

// Manejador global (Express 5 le pasa aquí también los errores de funciones async).
app.use((err, req, res, next) => {
  if (err.status) return res.status(err.status).json({ error: err.message });

  // Errores de PostgreSQL más comunes -> respuesta clara en vez de un 500.
  const porCodigo = {
    '23505': [409, 'Ya existe un registro con ese valor'],
    '23503': [400, 'La referencia indicada no existe'],
    '23514': [400, 'Un valor no cumple las reglas del banco (por ejemplo, saldo negativo)'],
    '23502': [400, 'Falta un campo obligatorio'],
    '22P02': [400, 'Formato de dato inválido'],
    '22007': [400, 'Formato de fecha inválido (use AAAA-MM-DD)'],
  };
  if (porCodigo[err.code]) {
    const [status, error] = porCodigo[err.code];
    return res.status(status).json({ error });
  }

  console.error(err);
  res.status(500).json({ error: 'Error interno del servidor' });
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => console.log(`API UEBank escuchando en http://localhost:${PORT}`));
