const router = require('express').Router();
const db = require('../db');
const HttpError = require('../httpError');
const { requerido, numero } = require('../validar');

//Leer campos
function leerCampos(body) {
  return [
    requerido(body.nombre, 'nombre'),
    numero(body.monto_objetivo, 'monto_objetivo', { min: 0, minExclusivo: true }),
    numero(body.monto_ahorrado ?? 0, 'monto_ahorrado'),
    body.fecha_limite ? String(body.fecha_limite) : null,
  ];
}

//READ lista
router.get('/', async (req, res) => {
  const usuario = requerido(req.query.usuario, 'usuario');
  const { rows } = await db.query('SELECT * FROM metas WHERE usuario = $1 ORDER BY id', [usuario]);
  res.json(rows);
});

//CREATE
router.post('/', async (req, res) => {
  const usuario = requerido(req.body.usuario, 'usuario');
  const { rows } = await db.query(
    `INSERT INTO metas (usuario, nombre, monto_objetivo, monto_ahorrado, fecha_limite)
     VALUES ($1, $2, $3, $4, $5) RETURNING *`,
    [usuario, ...leerCampos(req.body)]
  );
  res.status(201).json(rows[0]);
});

//UPDATE
router.put('/:id', async (req, res) => {
  const { rows } = await db.query(
    `UPDATE metas SET nombre = $1, monto_objetivo = $2, monto_ahorrado = $3, fecha_limite = $4
     WHERE id = $5 RETURNING *`,
    [...leerCampos(req.body), req.params.id]
  );
  if (!rows.length) throw new HttpError(404, 'Meta no encontrada');
  res.json(rows[0]);
});

//DELETE
router.delete('/:id', async (req, res) => {
  const { rowCount } = await db.query('DELETE FROM metas WHERE id = $1', [req.params.id]);
  if (!rowCount) throw new HttpError(404, 'Meta no encontrada');
  res.status(204).end();
});

module.exports = router;
