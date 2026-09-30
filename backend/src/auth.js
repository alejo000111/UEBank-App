const jwt = require('jsonwebtoken');
const HttpError = require('./httpError');

//Secreto
const JWT_SECRET = process.env.JWT_SECRET;
if (!JWT_SECRET) {
  throw new Error('Falta JWT_SECRET en el archivo .env (ver .env.example)');
}

const EXPIRACION = '24h';

//Firmar token
function firmarToken(usuario) {
  return jwt.sign({ sub: usuario }, JWT_SECRET, { expiresIn: EXPIRACION });
}

//Verificar token
function requerirToken(req, res, next) {
  const encabezado = req.headers.authorization || '';
  const [tipo, token] = encabezado.split(' ');

  if (tipo !== 'Bearer' || !token) {
    throw new HttpError(401, 'Falta el token de sesión (encabezado Authorization)');
  }

  try {
    const payload = jwt.verify(token, JWT_SECRET);
    req.usuarioToken = payload.sub;
    next();
  } catch (e) {
    throw new HttpError(401, 'Token inválido o vencido: inicia sesión de nuevo');
  }
}

//Validar dueño
function exigirDueño(req, usuarioDelRecurso) {
  if (usuarioDelRecurso !== req.usuarioToken) {
    throw new HttpError(403, 'No tienes permiso sobre ese recurso');
  }
}

module.exports = { firmarToken, requerirToken, exigirDueño, JWT_SECRET };
