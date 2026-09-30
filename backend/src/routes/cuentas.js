const router = require('express').Router();
const db = require('../db');
const HttpError = require('../httpError');
const { requerido, numero } = require('../validar');

const TIPOS = ['AHORROS', 'CORRIENTE'];

//Validar tipo de cuenta
function tipoValido(tipo) {
  const t = String(tipo || 'AHORROS').toUpperCase();
  if (!TIPOS.includes(t)) throw new HttpError(400, 'El tipo debe ser AHORROS o CORRIENTE');
  return t;
}

//READ lista
router.get('/', async (req, res) => {
  const usuario = requerido(req.query.usuario, 'usuario');
  const { rows } = await db.query('SELECT * FROM cuentas WHERE usuario = $1 ORDER BY id', [usuario]);
  res.json(rows);
});

//READ una
router.get('/:id', async (req, res) => {
  const { rows } = await db.query('SELECT * FROM cuentas WHERE id = $1', [req.params.id]);
  if (!rows.length) throw new HttpError(404, 'Cuenta no encontrada');
  res.json(rows[0]);
});

//CREATE
router.post('/', async (req, res) => {
  const usuario = requerido(req.body.usuario, 'usuario');
  const num = requerido(req.body.numero, 'numero');
  const tipo = tipoValido(req.body.tipo);
  const saldo = numero(req.body.saldo ?? 0, 'saldo');
  const { rows } = await db.query(
    'INSERT INTO cuentas (usuario, numero, tipo, saldo) VALUES ($1, $2, $3, $4) RETURNING *',
    [usuario, num, tipo, saldo]
  );
  res.status(201).json(rows[0]);
});

//UPDATE
router.put('/:id', async (req, res) => {
  const num = requerido(req.body.numero, 'numero');
  const tipo = tipoValido(req.body.tipo);
  const { rows } = await db.query(
    'UPDATE cuentas SET numero = $1, tipo = $2 WHERE id = $3 RETURNING *',
    [num, tipo, req.params.id]
  );
  if (!rows.length) throw new HttpError(404, 'Cuenta no encontrada');
  res.json(rows[0]);
});

//DELETE
router.delete('/:id', async (req, res) => {
  const { rowCount } = await db.query('DELETE FROM cuentas WHERE id = $1', [req.params.id]);
  if (!rowCount) throw new HttpError(404, 'Cuenta no encontrada');
  res.status(204).end();
});

module.exports = router;
