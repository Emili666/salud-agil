package co.edu.uniquindio.saludagil.repository;

import co.edu.uniquindio.saludagil.domain.Inventario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventarioRepository extends JpaRepository<Inventario, Long> {
    List<Inventario> findByMedicamentoId(Long medicamentoId);

    List<Inventario> findByPuntoIdOrderByMedicamentoNombreAsc(Long puntoId);

    Optional<Inventario> findByPuntoIdAndMedicamentoId(Long puntoId, Long medicamentoId);
}
