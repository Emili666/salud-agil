package co.edu.uniquindio.saludagil;

import co.edu.uniquindio.saludagil.service.EnviadorNotificaciones;
import co.edu.uniquindio.saludagil.service.EnviadorSimulado;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
        "saludagil.correo.habilitado=true",
        "saludagil.correo.brevo-api-key=clave-de-prueba",
        "saludagil.correo.remitente=avisos@salud-agil.test"
})
class CorreoBrevoConfiguracionTest {

    @Autowired
    private EnviadorNotificaciones enviador;

    @Test
    void configuraUnSoloEnviadorBrevoYLoExponeComoProxyAsincrono() {
        assertFalse(enviador instanceof EnviadorSimulado);
        assertTrue(AopUtils.isAopProxy(enviador));
    }
}
