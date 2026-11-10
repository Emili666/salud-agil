package co.edu.uniquindio.saludagil.dto;

import co.edu.uniquindio.saludagil.domain.Canal;

import java.time.LocalDateTime;

public record NotificacionDto(Long id, Canal canal, String destino, String mensaje, LocalDateTime fechaEnvio,
                              String formulaCodigo) {
}
