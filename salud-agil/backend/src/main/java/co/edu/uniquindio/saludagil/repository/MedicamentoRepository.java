package co.edu.uniquindio.saludagil.repository;

import co.edu.uniquindio.saludagil.domain.Medicamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicamentoRepository extends JpaRepository<Medicamento, Long> {
    List<Medicamento> findTop20ByNombreContainingIgnoreCaseOrderByNombreAsc(String nombre);

    List<Medicamento> findAllByOrderByNombreAsc();
}
