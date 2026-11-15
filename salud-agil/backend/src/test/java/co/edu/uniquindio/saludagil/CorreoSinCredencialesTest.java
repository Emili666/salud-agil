package co.edu.uniquindio.saludagil;

import co.edu.uniquindio.saludagil.domain.Canal;
import co.edu.uniquindio.saludagil.service.EnviadorNotificaciones;
import co.edu.uniquindio.saludagil.service.EnviadorSimulado;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@SpringBootTest(properties = {
        "saludagil.correo.habilitado=true",
        "saludagil.correo.brevo-api-key=",
        "saludagil.correo.remitente="
})
class CorreoSinCredencialesTest {

    @Autowired
    private EnviadorNotificaciones enviador;

    @Test
    void arrancaConCorreoHabilitadoSinCredencialesYUsaElSimulado() {
        assertInstanceOf(EnviadorSimulado.class, enviador);
        enviador.enviar(Canal.EMAIL, "persona@example.test", "Aviso de prueba");
    }
}
