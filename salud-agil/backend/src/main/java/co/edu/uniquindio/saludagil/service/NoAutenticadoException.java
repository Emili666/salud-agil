package co.edu.uniquindio.saludagil.service;

/** Falta iniciar sesión, la sesión venció o las credenciales no son válidas (HTTP 401). */
public class NoAutenticadoException extends RuntimeException {
    public NoAutenticadoException(String mensaje) {
        super(mensaje);
    }
}
