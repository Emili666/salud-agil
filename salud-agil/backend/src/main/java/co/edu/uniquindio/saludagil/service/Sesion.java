package co.edu.uniquindio.saludagil.service;

import co.edu.uniquindio.saludagil.domain.Rol;

import java.time.Instant;

/** Sesión abierta en memoria. {@code documento} solo aplica al rol AFILIADO. */
public record Sesion(String token, Long usuarioId, String nombre, Rol rol, String documento, Instant expira) {

    public boolean vencida() {
        return Instant.now().isAfter(expira);
    }

    /** Un afiliado solo puede ver y operar sus propias fórmulas y turnos. */
    public void exigirAfiliado(String documentoDueno) {
        if (rol != Rol.AFILIADO || documento == null || !documento.equals(documentoDueno)) {
            throw new AccesoDenegadoException("Solo puede consultar su propia información.");
        }
    }
}
