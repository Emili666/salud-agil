package co.edu.uniquindio.saludagil.dto;

import co.edu.uniquindio.saludagil.domain.Zona;

public record PuntoDto(Long id, String nombre, Zona zona, String direccion, String horario) {
}
