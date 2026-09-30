require('dotenv').config();
const { Pool, types } = require('pg');

//Conversión de tipos
types.setTypeParser(1700, (v) => parseFloat(v));
types.setTypeParser(1082, (v) => v);

//Conexión
if (!process.env.DATABASE_URL) {
  throw new Error('Falta DATABASE_URL en el archivo .env (copia .env.example a .env y complétalo)');
}
const pool = new Pool({
  connectionString: process.env.DATABASE_URL,
  ssl: process.env.DATABASE_SSL === 'true' ? { rejectUnauthorized: false } : false,
});

module.exports = {
  query: (texto, params) => pool.query(texto, params),
  pool,
};
