package co.edu.uniquindio.saludagil.dto;

import co.edu.uniquindio.saludagil.domain.NivelDisponibilidad;

public record FormulaItemDto(MedicamentoDto medicamento, int cantidadPrescrita, int cantidadEntregada,
                             int cantidadPendiente, int existenciasEnPunto, NivelDisponibilidad nivelEnPunto) {
}
