package co.edu.uniquindio.saludagil.repository;

import co.edu.uniquindio.saludagil.domain.Afiliado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AfiliadoRepository extends JpaRepository<Afiliado, Long> {
    Optional<Afiliado> findByDocumento(String documento);
}
