const jwt = require('jsonwebtoken');
const HttpError = require('./httpError');

// El secreto SOLO puede vivir en .env (ver .env.example): si falta, es
// mejor que la API no arranque a que arranque firmando tokens con un
// secreto adivinable.
const JWT_SECRET = process.env.JWT_SECRET;
if (!JWT_SECRET) {
  throw new Error('Falta JWT_SECRET en el archivo .env (ver .env.example)');
}

const EXPIRACION = '24h';

// Firma un token cuyo "dueño" (sub) es el usuario ya verificado.
function firmarToken(usuario) {
  return jwt.sign({ sub: usuario }, JWT_SECRET, { expiresIn: EXPIRACION });
}

// Middleware: exige "Authorization: Bearer <token>" válido y deja el
// usuario del token en req.usuarioToken para que las rutas protegidas
// puedan comparar contra el ?usuario= o el {usuario} del body.
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

// Corta la petición si el usuario del token no es dueño del recurso que
// está pidiendo/modificando. Se usa después de cargar la fila (o antes,
// cuando el usuario viene directo en la query o en el body).
function exigirDueño(req, usuarioDelRecurso) {
  if (usuarioDelRecurso !== req.usuarioToken) {
    throw new HttpError(403, 'No tienes permiso sobre ese recurso');
  }
}

module.exports = { firmarToken, requerirToken, exigirDueño, JWT_SECRET };
