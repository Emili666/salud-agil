package co.edu.uniquindio.saludagil.service;

/** La sesión es válida pero el rol no tiene permiso para la operación (HTTP 403). */
public class AccesoDenegadoException extends RuntimeException {
    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
