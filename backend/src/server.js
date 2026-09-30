require('dotenv').config();
const fs = require('fs');
const path = require('path');
const express = require('express');
const cors = require('cors');
const db = require('./db');

const app = express();
app.use(cors());
app.use(express.json());

const { requerirToken } = require('./auth');

//Rutas
app.get('/api/health', (req, res) => res.json({ ok: true }));

app.use('/api/auth', require('./routes/auth'));

app.use('/api/cuentas', requerirToken, require('./routes/cuentas'));
app.use('/api/movimientos', requerirToken, require('./routes/movimientos'));
app.use('/api/metas', requerirToken, require('./routes/metas'));

//Ruta no encontrada
app.use((req, res) => res.status(404).json({ error: 'Ruta no encontrada' }));

//Manejador de errores
app.use((err, req, res, next) => {
  if (err.status) return res.status(err.status).json({ error: err.message });

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

//Crear tablas
async function crearTablas() {
  const esquema = fs.readFileSync(path.join(__dirname, '..', 'schema.sql'), 'utf8');
  await db.query(esquema);
}

//Iniciar servidor
const PORT = process.env.PORT || 3000;
crearTablas()
  .then(() => app.listen(PORT, () => console.log(`API UEBank escuchando en http://localhost:${PORT}`)))
  .catch((err) => {
    console.error('No se pudo conectar con PostgreSQL:', err.message);
    process.exit(1);
  });
