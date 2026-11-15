package co.edu.uniquindio.saludagil.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "saludagil.correo")
public record CorreoProperties(
        boolean habilitado,
        String proveedor,
        String brevoApiKey,
        String remitente,
        String remitenteNombre,
        String destinoForzado) {

    public boolean brevoConfigurado() {
        return habilitado
                && tieneTexto(brevoApiKey)
                && tieneTexto(remitente);
    }

    private static boolean tieneTexto(String valor) {
        return valor != null && !valor.isBlank();
    }
}
