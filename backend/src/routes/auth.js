const crypto = require('crypto');
const router = require('express').Router();
const db = require('../db');
const HttpError = require('../httpError');
const { requerido } = require('../validar');
const { firmarToken } = require('../auth');

//Comparar hashes
function hashesIguales(a, b) {
  const bufA = Buffer.from(a, 'utf8');
  const bufB = Buffer.from(b, 'utf8');
  if (bufA.length !== bufB.length) return false;
  return crypto.timingSafeEqual(bufA, bufB);
}

//Registro
router.post('/registro', async (req, res) => {
  const usuario = requerido(req.body.usuario, 'usuario');
  const hash = requerido(req.body.hash, 'hash');

  await db.query(
    `INSERT INTO usuarios (usuario, hash) VALUES ($1, $2)
     ON CONFLICT (usuario) DO NOTHING`,
    [usuario, hash]
  );

  res.status(201).json({ token: firmarToken(usuario) });
});

//Login
router.post('/login', async (req, res) => {
  const usuario = requerido(req.body.usuario, 'usuario');
  const hash = requerido(req.body.hash, 'hash');

  const { rows } = await db.query('SELECT hash FROM usuarios WHERE usuario = $1', [usuario]);

  if (!rows.length) {
    await db.query('INSERT INTO usuarios (usuario, hash) VALUES ($1, $2)', [usuario, hash]);
  } else if (!hashesIguales(hash, rows[0].hash)) {
    throw new HttpError(401, 'Usuario o contraseña incorrectos');
  }

  res.json({ token: firmarToken(usuario) });
});

module.exports = router;
