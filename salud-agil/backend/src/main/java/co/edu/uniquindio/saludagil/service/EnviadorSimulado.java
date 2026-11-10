package co.edu.uniquindio.saludagil.service;

import co.edu.uniquindio.saludagil.domain.Canal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Simula el envío escribiendo en el log. Los mensajes también quedan guardados en la tabla notificacion. */
@Component
public class EnviadorSimulado implements EnviadorNotificaciones {

    private static final Logger log = LoggerFactory.getLogger(EnviadorSimulado.class);

    @Override
    public void enviar(Canal canal, String destino, String mensaje) {
        log.info("[{} -> {}] {}", canal, destino, mensaje);
    }
}
