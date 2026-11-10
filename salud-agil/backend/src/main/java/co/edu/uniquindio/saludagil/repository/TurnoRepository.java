package co.edu.uniquindio.saludagil.repository;

import co.edu.uniquindio.saludagil.domain.EstadoTurno;
import co.edu.uniquindio.saludagil.domain.Turno;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface TurnoRepository extends JpaRepository<Turno, Long> {
    Optional<Turno> findByCodigo(String codigo);

    long countByPuntoIdAndFechaAndHoraAndEstado(Long puntoId, LocalDate fecha, LocalTime hora, EstadoTurno estado);

    long countByPuntoIdAndFecha(Long puntoId, LocalDate fecha);

    List<Turno> findByPuntoIdAndFechaOrderByHoraAsc(Long puntoId, LocalDate fecha);

    Optional<Turno> findFirstByFormulaIdAndEstado(Long formulaId, EstadoTurno estado);
}
