package co.edu.uniquindio.saludagil.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Registro de un afiliado que ya existe en la EPS. El correo pasa a ser su canal de avisos. */
public record RegistroRequest(
        @NotBlank @Pattern(regexp = "\\d{5,20}", message = "debe tener solo números") String documento,
        @NotBlank @Email @Size(max = 120) String correo,
        @NotBlank @Size(min = 6, max = 100, message = "debe tener al menos 6 caracteres") String clave) {
}
