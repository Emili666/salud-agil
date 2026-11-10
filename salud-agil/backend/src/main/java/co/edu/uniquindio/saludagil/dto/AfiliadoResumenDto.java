package co.edu.uniquindio.saludagil.dto;

import co.edu.uniquindio.saludagil.domain.Canal;

import java.util.List;

public record AfiliadoResumenDto(String documento, String nombres, Canal canalPreferido, String destinoEnmascarado,
                                 List<FormulaDto> formulas, List<NotificacionDto> notificaciones) {
}
