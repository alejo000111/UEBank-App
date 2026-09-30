const HttpError = require('./httpError');

//Validar campo obligatorio
function requerido(valor, nombre) {
  if (valor === undefined || valor === null || String(valor).trim() === '') {
    throw new HttpError(400, `El campo "${nombre}" es obligatorio`);
  }
  return String(valor).trim();
}

//Validar número
function numero(valor, nombre, { min = 0, minExclusivo = false } = {}) {
  const n = Number(valor);
  if (!Number.isFinite(n) || (minExclusivo ? n <= min : n < min)) {
    throw new HttpError(
      400,
      `El campo "${nombre}" debe ser un número ${minExclusivo ? 'mayor' : 'mayor o igual'} a ${min}`
    );
  }
  return n;
}

module.exports = { requerido, numero };
