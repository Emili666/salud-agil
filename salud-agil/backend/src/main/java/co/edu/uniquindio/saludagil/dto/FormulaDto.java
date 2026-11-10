package co.edu.uniquindio.saludagil.dto;

import co.edu.uniquindio.saludagil.domain.EstadoFormula;

import java.time.LocalDateTime;
import java.util.List;

public record FormulaDto(String codigo, String afiliadoNombre, String afiliadoDocumento, PuntoDto punto,
                         EstadoFormula estado, LocalDateTime fechaRadicacion, LocalDateTime fechaEntrega,
                         List<FormulaItemDto> items, TurnoDto turnoVigente, boolean encuestaRespondida) {
}
