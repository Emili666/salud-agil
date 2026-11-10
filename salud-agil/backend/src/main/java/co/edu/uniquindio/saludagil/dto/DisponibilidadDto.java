package co.edu.uniquindio.saludagil.dto;

import co.edu.uniquindio.saludagil.domain.NivelDisponibilidad;

import java.time.LocalDateTime;

/** Disponibilidad de un medicamento en un punto, tal como la ve el afiliado. */
public record DisponibilidadDto(PuntoDto punto, int cantidad, NivelDisponibilidad nivel,
                                LocalDateTime ultimaActualizacion) {
}
