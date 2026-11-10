package co.edu.uniquindio.saludagil.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ConteoFisicoRequest(@NotNull Long puntoId, @NotNull Long medicamentoId,
                                  @NotNull @Min(0) Integer cantidadFisica) {
}
