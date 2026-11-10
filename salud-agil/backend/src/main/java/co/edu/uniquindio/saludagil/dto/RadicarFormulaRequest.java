package co.edu.uniquindio.saludagil.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record RadicarFormulaRequest(@NotBlank String documento, @NotNull Long puntoId,
                                    @NotEmpty List<@Valid Item> items) {

    public record Item(@NotNull Long medicamentoId, @NotNull @Min(1) Integer cantidad) {
    }
}
