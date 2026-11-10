package co.edu.uniquindio.saludagil.service;

import co.edu.uniquindio.saludagil.domain.Afiliado;
import co.edu.uniquindio.saludagil.domain.Formula;
import co.edu.uniquindio.saludagil.domain.Notificacion;
import co.edu.uniquindio.saludagil.dto.Mapeo;
import co.edu.uniquindio.saludagil.dto.NotificacionDto;
import co.edu.uniquindio.saludagil.repository.NotificacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final EnviadorNotificaciones enviador;

    public NotificacionService(NotificacionRepository notificacionRepository, EnviadorNotificaciones enviador) {
        this.notificacionRepository = notificacionRepository;
        this.enviador = enviador;
    }

    /** Envía el mensaje por el canal preferido del afiliado y lo deja registrado. */
    @Transactional
    public Notificacion notificar(Afiliado afiliado, Formula formula, String mensaje) {
        Notificacion notificacion = new Notificacion(afiliado, formula, afiliado.getCanalPreferido(),
                afiliado.destinoNotificacion(), mensaje, LocalDateTime.now());
        enviador.enviar(notificacion.getCanal(), notificacion.getDestino(), mensaje);
        return notificacionRepository.save(notificacion);
    }

    @Transactional(readOnly = true)
    public List<NotificacionDto> listarPorDocumento(String documento) {
        return notificacionRepository.findByAfiliadoDocumentoOrderByFechaEnvioDesc(documento).stream()
                .map(Mapeo::notificacion)
                .toList();
    }
}
