package co.edu.uniquindio.saludagil.dto;

import co.edu.uniquindio.saludagil.domain.NivelDisponibilidad;

import java.time.LocalDateTime;

public record InventarioDto(Long id, MedicamentoDto medicamento, int cantidad, NivelDisponibilidad nivel,
                            LocalDateTime ultimaActualizacion) {
}
