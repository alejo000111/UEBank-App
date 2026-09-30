//Clase
class HttpError extends Error {
  //Constructor
  constructor(status, message) {
    super(message);
    this.status = status;
  }
}

module.exports = HttpError;
