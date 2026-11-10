package co.edu.uniquindio.saludagil.service;

/** Operación válida en forma pero no permitida por las reglas del negocio (p. ej. agendar sin existencias). */
public class ReglaNegocioException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
