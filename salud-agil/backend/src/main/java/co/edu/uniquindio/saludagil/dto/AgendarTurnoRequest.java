package co.edu.uniquindio.saludagil.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record AgendarTurnoRequest(@NotBlank String formulaCodigo, @NotNull LocalDate fecha, @NotNull LocalTime hora) {
}
