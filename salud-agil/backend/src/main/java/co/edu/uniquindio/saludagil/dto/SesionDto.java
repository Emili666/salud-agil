package co.edu.uniquindio.saludagil.dto;

import co.edu.uniquindio.saludagil.domain.Rol;

/** Respuesta del inicio de sesión. El frontend envía {@code token} en el header Authorization. */
public record SesionDto(String token, String nombre, Rol rol, String documento) {
}
