package co.edu.uniquindio.saludagil.dto;

import co.edu.uniquindio.saludagil.domain.EstadoTurno;

import java.time.LocalDate;
import java.time.LocalTime;

public record TurnoDto(String codigo, String formulaCodigo, String afiliadoNombre, String afiliadoDocumento,
                       PuntoDto punto, LocalDate fecha, LocalTime hora, EstadoTurno estado) {
}
