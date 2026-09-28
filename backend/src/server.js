require('dotenv').config();
const express = require('express');
const cors = require('cors');

const app = express();
app.use(cors());
app.use(express.json());

app.get('/api/health', (req, res) => res.json({ ok: true }));
app.use('/api/cuentas', require('./routes/cuentas'));
app.use('/api/movimientos', require('./routes/movimientos'));
app.use('/api/metas', require('./routes/metas'));

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
