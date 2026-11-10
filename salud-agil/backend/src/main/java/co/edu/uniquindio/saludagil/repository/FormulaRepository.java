package co.edu.uniquindio.saludagil.repository;

import co.edu.uniquindio.saludagil.domain.EstadoFormula;
import co.edu.uniquindio.saludagil.domain.Formula;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FormulaRepository extends JpaRepository<Formula, Long> {
    Optional<Formula> findByCodigo(String codigo);

    List<Formula> findByAfiliadoDocumentoOrderByFechaRadicacionDesc(String documento);

    List<Formula> findByPuntoIdOrderByFechaRadicacionDesc(Long puntoId);

    List<Formula> findByPuntoIdAndEstadoIn(Long puntoId, Collection<EstadoFormula> estados);
}
