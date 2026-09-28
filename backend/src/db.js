require('dotenv').config();
const { Pool, types } = require('pg');

// pg devuelve NUMERIC (1700) y DATE (1082) como texto; los pasamos a número / "AAAA-MM-DD".
types.setTypeParser(1700, (v) => parseFloat(v));
types.setTypeParser(1082, (v) => v);

const pool = new Pool({ connectionString: process.env.DATABASE_URL });

module.exports = {
  query: (texto, params) => pool.query(texto, params),
  pool,
};
