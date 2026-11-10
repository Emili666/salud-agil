package co.edu.uniquindio.saludagil.repository;

import co.edu.uniquindio.saludagil.domain.PuntoEntrega;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PuntoEntregaRepository extends JpaRepository<PuntoEntrega, Long> {
    List<PuntoEntrega> findAllByOrderByIdAsc();
}
