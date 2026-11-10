package co.edu.uniquindio.saludagil.repository;

import co.edu.uniquindio.saludagil.domain.Encuesta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EncuestaRepository extends JpaRepository<Encuesta, Long> {
    boolean existsByFormulaId(Long formulaId);
}
