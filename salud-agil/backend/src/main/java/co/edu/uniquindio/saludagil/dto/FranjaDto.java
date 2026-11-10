package co.edu.uniquindio.saludagil.dto;

import java.time.LocalTime;

public record FranjaDto(LocalTime hora, int cupoTotal, int ocupados, int disponibles) {
}
