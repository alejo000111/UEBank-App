const router = require('express').Router();
const db = require('../db');
const HttpError = require('../httpError');
const { requerido, numero } = require('../validar');
const { exigirDueño } = require('../auth');

const TIPOS = ['DEPOSITO', 'RETIRO'];

//Transacción
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

//READ lista
router.get('/', async (req, res) => {
  const usuario = requerido(req.query.usuario, 'usuario');
  exigirDueño(req, usuario);
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

//CREATE
router.post('/', async (req, res) => {
  const cuentaId = numero(req.body.cuenta_id, 'cuenta_id', { min: 0, minExclusivo: true });
  const tipo = String(req.body.tipo || '').toUpperCase();
  if (!TIPOS.includes(tipo)) throw new HttpError(400, 'El tipo debe ser DEPOSITO o RETIRO');
  const monto = numero(req.body.monto, 'monto', { min: 0, minExclusivo: true });
  const descripcion = (req.body.descripcion || '').toString().trim();

  const creado = await enTransaccion(async (client) => {
    //Bloquear fila y validar dueño
    const cuenta = await client.query(
      'SELECT usuario, saldo FROM cuentas WHERE id = $1 FOR UPDATE',
      [cuentaId]
    );
    if (!cuenta.rows.length || cuenta.rows[0].usuario !== req.usuarioToken) {
      throw new HttpError(404, 'Cuenta no encontrada');
    }
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

//UPDATE
router.put('/:id', async (req, res) => {
  const descripcion = (req.body.descripcion || '').toString().trim();

  const actual = await db.query(
    'SELECT c.usuario FROM movimientos m JOIN cuentas c ON c.id = m.cuenta_id WHERE m.id = $1',
    [req.params.id]
  );
  if (!actual.rows.length || actual.rows[0].usuario !== req.usuarioToken) {
    throw new HttpError(404, 'Movimiento no encontrado');
  }

  await db.query('UPDATE movimientos SET descripcion = $1 WHERE id = $2', [descripcion, req.params.id]);
  const { rows } = await db.query(`${SELECT_CON_CUENTA} WHERE m.id = $1`, [req.params.id]);
  res.json(rows[0]);
});

//DELETE
router.delete('/:id', async (req, res) => {
  await enTransaccion(async (client) => {
    const mov = await client.query(
      `SELECT m.cuenta_id, m.tipo, m.monto, c.usuario
       FROM movimientos m JOIN cuentas c ON c.id = m.cuenta_id
       WHERE m.id = $1 FOR UPDATE`,
      [req.params.id]
    );
    if (!mov.rows.length || mov.rows[0].usuario !== req.usuarioToken) {
      throw new HttpError(404, 'Movimiento no encontrado');
    }
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
