const crypto = require('crypto');
const router = require('express').Router();
const db = require('../db');
const HttpError = require('../httpError');
const { requerido } = require('../validar');
const { firmarToken } = require('../auth');

// Compara dos "salt:hash" en tiempo constante. timingSafeEqual exige que
// los dos buffers midan lo mismo, así que si difieren ya sabemos que no
// coinciden (sin filtrar nada por temporización: la comparación siempre
// corre sobre buffers del mismo tamaño que el guardado).
function hashesIguales(a, b) {
  const bufA = Buffer.from(a, 'utf8');
  const bufB = Buffer.from(b, 'utf8');
  if (bufA.length !== bufB.length) return false;
  return crypto.timingSafeEqual(bufA, bufB);
}

/**
 * POST /api/auth/registro  { usuario, hash }
 *
 * La app llama esto justo después de crear el usuario en su SQLite local.
 * "hash" es el mismo "salt:hash" en Base64 que ya calculó PasswordUtils en
 * el teléfono: la contraseña real nunca sale del dispositivo. Si el
 * registro en la API falla (por ejemplo, sin conexión), el registro local
 * NO se revierte: el usuario sigue pudiendo usar la app sin conexión, y su
 * cuenta se sincroniza sola la próxima vez que inicie sesión con internet
 * (ver /login más abajo).
 */
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

/**
 * POST /api/auth/login  { usuario, hash }
 *
 * La app SOLO llama esto después de verificar la contraseña localmente con
 * PasswordUtils.verificarPassword (nunca antes). "hash" es ese mismo
 * "salt:hash" ya verificado, no la contraseña.
 *
 * - Si el usuario ya existe en esta tabla, se exige que el hash coincida
 *   exactamente con el guardado.
 * - Si no existe (cuentas creadas antes de este cambio, o registradas sin
 *   conexión), se crea aquí mismo con el hash recibido: como la app ya
 *   verificó la contraseña contra el SQLite del teléfono, este primer
 *   contacto sirve para "dar de alta" esa identidad en la API.
 *
 * Limitación conocida (ver docs/Guia_de_Sustentacion.md § 8.1): la API
 * confía en que quien llama ya verificó la contraseña en el teléfono, no
 * la reconstruye por su cuenta. Migrar el login completo a la API (para
 * que la propia API sea la que verifique la contraseña, sin depender del
 * cliente) es la siguiente mejora pendiente.
 */
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
