// Error con código HTTP: los controladores lo lanzan y el manejador global lo convierte en JSON.
class HttpError extends Error {
  constructor(status, message) {
    super(message);
    this.status = status;
  }
}

module.exports = HttpError;
