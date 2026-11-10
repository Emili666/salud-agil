package co.edu.uniquindio.saludagil.repository;

import co.edu.uniquindio.saludagil.domain.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    List<Notificacion> findByAfiliadoDocumentoOrderByFechaEnvioDesc(String documento);
}
