package co.edu.uniquindio.saludagil.service;

import co.edu.uniquindio.saludagil.config.CorreoProperties;
import co.edu.uniquindio.saludagil.domain.Canal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class EnviadorCorreoBrevoTest {

    private static final String URL_BREVO = "https://api.brevo.com/v3/smtp/email";
    private static final String PIE = "Mensaje automático de un prototipo académico. Todos los datos son ficticios.";

    @Test
    void emailEnvíaLaSolicitudCompletaPorHttpsABrevo() {
        var preparado = preparar("");
        preparado.servidor().expect(requestTo(URL_BREVO))
                .andExpect(method(org.springframework.http.HttpMethod.POST))
                .andExpect(header("api-key", "clave-de-prueba"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(header("accept", MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.sender.name").value("Salud Ágil"))
                .andExpect(jsonPath("$.sender.email").value("avisos@salud-agil.test"))
                .andExpect(jsonPath("$.to[0].email").value("persona@example.test"))
                .andExpect(jsonPath("$.subject").value("Salud Ágil — Actualización de su fórmula"))
                .andExpect(jsonPath("$.textContent").value("Su fórmula está disponible.\n" + PIE))
                .andRespond(withSuccess());

        preparado.enviador().enviar(Canal.EMAIL, "persona@example.test", "Su fórmula está disponible.");

        preparado.servidor().verify();
    }

    @Test
    void smsYWhatsAppSeDeleganSinLlamarPorHttp() {
        var preparado = preparar("");

        preparado.enviador().enviar(Canal.SMS, "3001234567", "Aviso SMS");
        preparado.enviador().enviar(Canal.WHATSAPP, "3001234567", "Aviso WhatsApp");

        assertEquals(List.of(
                new Envio(Canal.SMS, "3001234567", "Aviso SMS"),
                new Envio(Canal.WHATSAPP, "3001234567", "Aviso WhatsApp")),
                preparado.simulado().envios());
        preparado.servidor().verify();
    }

    @Test
    void destinoForzadoRedirigeYConservaElDestinatarioOriginalEnElTexto() {
        var preparado = preparar("demo@example.test");
        preparado.servidor().expect(requestTo(URL_BREVO))
                .andExpect(jsonPath("$.to[0].email").value("demo@example.test"))
                .andExpect(jsonPath("$.textContent").value(
                        "[Destinatario original: persona@example.test]\nAviso de prueba\n" + PIE))
                .andRespond(withSuccess());

        preparado.enviador().enviar(Canal.EMAIL, "persona@example.test", "Aviso de prueba");

        preparado.servidor().verify();
    }

    @Test
    void respuestasDeErrorBrevoNoSePropagan() {
        for (HttpStatus estado : new HttpStatus[]{HttpStatus.BAD_REQUEST, HttpStatus.UNAUTHORIZED,
                HttpStatus.INTERNAL_SERVER_ERROR}) {
            var preparado = preparar("");
            preparado.servidor().expect(requestTo(URL_BREVO))
                    .andRespond(withStatus(estado).contentType(MediaType.APPLICATION_JSON)
                            .body("{\"message\":\"solicitud rechazada\"}"));

            assertDoesNotThrow(() ->
                    preparado.enviador().enviar(Canal.EMAIL, "persona@example.test", "Aviso"));

            preparado.servidor().verify();
        }
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "sin-arroba"})
    void destinoInvalidoNoGeneraLlamadaHttp(String destino) {
        var preparado = preparar("");

        preparado.enviador().enviar(Canal.EMAIL, destino, "Aviso");

        preparado.servidor().verify();
    }

    private static Preparado preparar(String destinoForzado) {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer servidor = MockRestServiceServer.bindTo(builder).build();
        RestClient cliente = builder.build();
        EnviadorSimuladoGrabador simulado = new EnviadorSimuladoGrabador();
        CorreoProperties properties = new CorreoProperties(true, "brevo", "clave-de-prueba",
                "avisos@salud-agil.test", "Salud Ágil", destinoForzado);
        return new Preparado(new EnviadorCorreoBrevo(properties, simulado, cliente), simulado, servidor);
    }

    private static final class EnviadorSimuladoGrabador implements EnviadorNotificaciones {
        private final List<Envio> envios = new ArrayList<>();

        @Override
        public void enviar(Canal canal, String destino, String mensaje) {
            envios.add(new Envio(canal, destino, mensaje));
        }

        List<Envio> envios() {
            return List.copyOf(envios);
        }
    }

    private record Envio(Canal canal, String destino, String mensaje) {
    }

    private record Preparado(
            EnviadorCorreoBrevo enviador,
            EnviadorSimuladoGrabador simulado,
            MockRestServiceServer servidor) {
    }
}
