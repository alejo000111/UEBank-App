const router = require('express').Router();
const db = require('../db');
const HttpError = require('../httpError');
const { requerido, numero } = require('../validar');
const { exigirDueño } = require('../auth');

const TIPOS = ['AHORROS', 'CORRIENTE'];

function tipoValido(tipo) {
  const t = String(tipo || 'AHORROS').toUpperCase();
  if (!TIPOS.includes(t)) throw new HttpError(400, 'El tipo debe ser AHORROS o CORRIENTE');
  return t;
}

// READ (lista): solo las cuentas del usuario indicado, y solo si ese
// usuario es el dueño del token (ver requerirToken en server.js).
router.get('/', async (req, res) => {
  const usuario = requerido(req.query.usuario, 'usuario');
  exigirDueño(req, usuario);
  const { rows } = await db.query('SELECT * FROM cuentas WHERE usuario = $1 ORDER BY id', [usuario]);
  res.json(rows);
});

// READ (una): 404 también cuando la cuenta existe pero es de otro usuario,
// para no revelar con un 403 que ese id sí existe.
router.get('/:id', async (req, res) => {
  const { rows } = await db.query('SELECT * FROM cuentas WHERE id = $1', [req.params.id]);
  if (!rows.length || rows[0].usuario !== req.usuarioToken) {
    throw new HttpError(404, 'Cuenta no encontrada');
  }
  res.json(rows[0]);
});

// CREATE
router.post('/', async (req, res) => {
  const usuario = requerido(req.body.usuario, 'usuario');
  exigirDueño(req, usuario);
  const num = requerido(req.body.numero, 'numero');
  const tipo = tipoValido(req.body.tipo);
  const saldo = numero(req.body.saldo ?? 0, 'saldo');
  const { rows } = await db.query(
    'INSERT INTO cuentas (usuario, numero, tipo, saldo) VALUES ($1, $2, $3, $4) RETURNING *',
    [usuario, num, tipo, saldo]
  );
  res.status(201).json(rows[0]);
});

// UPDATE: el saldo NO se edita aquí, solo cambia con movimientos
router.put('/:id', async (req, res) => {
  const num = requerido(req.body.numero, 'numero');
  const tipo = tipoValido(req.body.tipo);

  const actual = await db.query('SELECT usuario FROM cuentas WHERE id = $1', [req.params.id]);
  if (!actual.rows.length || actual.rows[0].usuario !== req.usuarioToken) {
    throw new HttpError(404, 'Cuenta no encontrada');
  }

  const { rows } = await db.query(
    'UPDATE cuentas SET numero = $1, tipo = $2 WHERE id = $3 RETURNING *',
    [num, tipo, req.params.id]
  );
  res.json(rows[0]);
});

// DELETE (sus movimientos se borran en cascada)
router.delete('/:id', async (req, res) => {
  const actual = await db.query('SELECT usuario FROM cuentas WHERE id = $1', [req.params.id]);
  if (!actual.rows.length || actual.rows[0].usuario !== req.usuarioToken) {
    throw new HttpError(404, 'Cuenta no encontrada');
  }

  await db.query('DELETE FROM cuentas WHERE id = $1', [req.params.id]);
  res.status(204).end();
});

module.exports = router;
