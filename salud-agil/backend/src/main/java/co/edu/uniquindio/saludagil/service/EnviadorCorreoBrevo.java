package co.edu.uniquindio.saludagil.service;

import co.edu.uniquindio.saludagil.config.CorreoProperties;
import co.edu.uniquindio.saludagil.domain.Canal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;

public class EnviadorCorreoBrevo implements EnviadorNotificaciones {

    private static final Logger log = LoggerFactory.getLogger(EnviadorCorreoBrevo.class);
    private static final String URL_BREVO = "https://api.brevo.com/v3/smtp/email";
    private static final String ASUNTO = "Salud Ágil — Actualización de su fórmula";
    private static final String PIE = "Mensaje automático de un prototipo académico. Todos los datos son ficticios.";

    private final CorreoProperties properties;
    private final EnviadorNotificaciones simulado;
    private final RestClient cliente;

    public EnviadorCorreoBrevo(
            CorreoProperties properties,
            EnviadorNotificaciones simulado,
            RestClient cliente) {
        this.properties = properties;
        this.simulado = simulado;
        this.cliente = cliente;
    }

    @Async("correoTaskExecutor")
    @Override
    public void enviar(Canal canal, String destino, String mensaje) {
        if (canal != Canal.EMAIL) {
            simulado.enviar(canal, destino, mensaje);
            return;
        }

        if (!destinoValido(destino)) {
            log.warn("No se envió el correo: el destino no es válido");
            return;
        }

        boolean redireccionado = tieneTexto(properties.destinoForzado());
        String destinatario = redireccionado
                ? properties.destinoForzado().trim()
                : destino.trim();
        if (!destinoValido(destinatario)) {
            log.warn("No se envió el correo: el destino forzado no es válido");
            return;
        }

        try {
            String texto = construirTexto(destino, mensaje, redireccionado);
            Map<String, Object> cuerpo = Map.of(
                    "sender", Map.of(
                            "name", properties.remitenteNombre(),
                            "email", properties.remitente()),
                    "to", List.of(Map.of("email", destinatario)),
                    "subject", ASUNTO,
                    "textContent", texto);

            cliente.post()
                    .uri(URL_BREVO)
                    .header("api-key", properties.brevoApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(cuerpo)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception excepcion) {
            String codigoHttp = "desconocido";
            String respuesta = "";
            if (excepcion instanceof RestClientResponseException errorRespuesta) {
                codigoHttp = Integer.toString(errorRespuesta.getStatusCode().value());
                respuesta = errorRespuesta.getResponseBodyAsString();
            }
            if (tieneTexto(properties.brevoApiKey())) {
                respuesta = respuesta.replace(properties.brevoApiKey(), "[REDACTADO]");
            }
            log.warn("Falló el envío de correo con Brevo (HTTP {}). Respuesta: {}. Error: {}",
                    codigoHttp, respuesta, excepcion.getClass().getSimpleName());
        }
    }

    private static String construirTexto(String destinoOriginal, String mensaje, boolean redireccionado) {
        String texto = mensaje == null ? "" : mensaje;
        String prefijo = redireccionado
                ? "[Destinatario original: " + destinoOriginal + "]\n"
                : "";
        return prefijo + texto + "\n" + PIE;
    }

    private static boolean destinoValido(String destino) {
        return tieneTexto(destino) && destino.contains("@");
    }

    private static boolean tieneTexto(String valor) {
        return valor != null && !valor.isBlank();
    }
}
