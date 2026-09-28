const router = require('express').Router();
const db = require('../db');
const HttpError = require('../httpError');
const { requerido, numero } = require('../validar');

const TIPOS = ['DEPOSITO', 'RETIRO'];

// Ejecuta fn dentro de una transacción: o se guardan movimiento Y saldo, o ninguno.
async function enTransaccion(fn) {
  const client = await db.pool.connect();
  try {
    await client.query('BEGIN');
    const resultado = await fn(client);
    await client.query('COMMIT');
    return resultado;
  } catch (err) {
    await client.query('ROLLBACK');
    throw err;
  } finally {
    client.release();
  }
}

const SELECT_CON_CUENTA =
  'SELECT m.*, c.numero AS cuenta_numero FROM movimientos m JOIN cuentas c ON c.id = m.cuenta_id';

// READ (lista): movimientos del usuario, opcionalmente de una sola cuenta
router.get('/', async (req, res) => {
  const usuario = requerido(req.query.usuario, 'usuario');
  const params = [usuario];
  let filtro = '';
  if (req.query.cuenta_id) {
    params.push(req.query.cuenta_id);
    filtro = ' AND m.cuenta_id = $2';
  }
  const { rows } = await db.query(
    `${SELECT_CON_CUENTA} WHERE c.usuario = $1${filtro} ORDER BY m.fecha DESC, m.id DESC`,
    params
  );
  res.json(rows);
});

// CREATE: registra el movimiento y actualiza el saldo de la cuenta
router.post('/', async (req, res) => {
  const cuentaId = numero(req.body.cuenta_id, 'cuenta_id', { min: 0, minExclusivo: true });
  const tipo = String(req.body.tipo || '').toUpperCase();
  if (!TIPOS.includes(tipo)) throw new HttpError(400, 'El tipo debe ser DEPOSITO o RETIRO');
  const monto = numero(req.body.monto, 'monto', { min: 0, minExclusivo: true });
  const descripcion = (req.body.descripcion || '').toString().trim();

  const creado = await enTransaccion(async (client) => {
    // FOR UPDATE bloquea la fila para que dos movimientos simultáneos no pisen el saldo.
    const cuenta = await client.query('SELECT saldo FROM cuentas WHERE id = $1 FOR UPDATE', [cuentaId]);
    if (!cuenta.rows.length) throw new HttpError(404, 'Cuenta no encontrada');
    if (tipo === 'RETIRO' && cuenta.rows[0].saldo < monto) throw new HttpError(400, 'Saldo insuficiente');

    const delta = tipo === 'DEPOSITO' ? monto : -monto;
    await client.query('UPDATE cuentas SET saldo = saldo + $1 WHERE id = $2', [delta, cuentaId]);
    const { rows } = await client.query(
      'INSERT INTO movimientos (cuenta_id, tipo, monto, descripcion) VALUES ($1, $2, $3, $4) RETURNING id',
      [cuentaId, tipo, monto, descripcion]
    );
    return rows[0].id;
  });

  const { rows } = await db.query(`${SELECT_CON_CUENTA} WHERE m.id = $1`, [creado]);
  res.status(201).json(rows[0]);
});

// UPDATE: solo la descripción (cambiar monto o tipo alteraría saldos ya registrados)
router.put('/:id', async (req, res) => {
  const descripcion = (req.body.descripcion || '').toString().trim();
  const { rowCount } = await db.query('UPDATE movimientos SET descripcion = $1 WHERE id = $2', [
    descripcion,
    req.params.id,
  ]);
  if (!rowCount) throw new HttpError(404, 'Movimiento no encontrado');
  const { rows } = await db.query(`${SELECT_CON_CUENTA} WHERE m.id = $1`, [req.params.id]);
  res.json(rows[0]);
});

// DELETE: anula el movimiento y revierte su efecto en el saldo
router.delete('/:id', async (req, res) => {
  await enTransaccion(async (client) => {
    const mov = await client.query(
      'SELECT cuenta_id, tipo, monto FROM movimientos WHERE id = $1 FOR UPDATE',
      [req.params.id]
    );
    if (!mov.rows.length) throw new HttpError(404, 'Movimiento no encontrado');
    const { cuenta_id, tipo, monto } = mov.rows[0];

    const cuenta = await client.query('SELECT saldo FROM cuentas WHERE id = $1 FOR UPDATE', [cuenta_id]);
    const delta = tipo === 'DEPOSITO' ? -monto : monto;
    if (cuenta.rows[0].saldo + delta < 0) {
      throw new HttpError(400, 'No se puede anular: el saldo quedaría negativo');
    }
    await client.query('UPDATE cuentas SET saldo = saldo + $1 WHERE id = $2', [delta, cuenta_id]);
    await client.query('DELETE FROM movimientos WHERE id = $1', [req.params.id]);
  });
  res.status(204).end();
});

module.exports = router;
