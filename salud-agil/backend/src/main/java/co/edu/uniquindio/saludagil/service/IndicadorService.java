package co.edu.uniquindio.saludagil.service;

import co.edu.uniquindio.saludagil.config.SaludAgilProperties;
import co.edu.uniquindio.saludagil.domain.*;
import co.edu.uniquindio.saludagil.dto.*;
import co.edu.uniquindio.saludagil.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Predicate;

/**
 * Indicadores de desempeño para la EPS y el gestor farmacéutico.
 * Se calculan en memoria: suficiente para el piloto de tres puntos. Con más volumen
 * convendría pasarlos a consultas agregadas o a una vista materializada.
 */
@Service
@Transactional(readOnly = true)
public class IndicadorService {

    private final PuntoEntregaRepository puntoRepository;
    private final FormulaRepository formulaRepository;
    private final DispensacionRepository dispensacionRepository;
    private final ConteoFisicoRepository conteoRepository;
    private final EncuestaRepository encuestaRepository;
    private final InventarioRepository inventarioRepository;
    private final TurnoRepository turnoRepository;
    private final SaludAgilProperties.Metas metas;

    public IndicadorService(PuntoEntregaRepository puntoRepository, FormulaRepository formulaRepository,
                            DispensacionRepository dispensacionRepository, ConteoFisicoRepository conteoRepository,
                            EncuestaRepository encuestaRepository, InventarioRepository inventarioRepository,
                            TurnoRepository turnoRepository, SaludAgilProperties propiedades) {
        this.puntoRepository = puntoRepository;
        this.formulaRepository = formulaRepository;
        this.dispensacionRepository = dispensacionRepository;
        this.conteoRepository = conteoRepository;
        this.encuestaRepository = encuestaRepository;
        this.inventarioRepository = inventarioRepository;
        this.turnoRepository = turnoRepository;
        this.metas = propiedades.metas();
    }

    public PanelIndicadoresDto calcular() {
        Datos datos = new Datos(formulaRepository.findAll(), dispensacionRepository.findAll(),
                conteoRepository.findAll(), encuestaRepository.findAll(), inventarioRepository.findAll(),
                turnoRepository.findAll());
        TableroDto general = tablero(null, datos);
        List<TableroDto> porPunto = puntoRepository.findAllByOrderByIdAsc().stream()
                .map(p -> tablero(p, datos))
                .toList();
        return new PanelIndicadoresDto(general, porPunto, LocalDateTime.now());
    }

    private TableroDto tablero(PuntoEntrega punto, Datos d) {
        Predicate<PuntoEntrega> enAlcance = p -> punto == null || p.getId().equals(punto.getId());

        List<Formula> formulas = d.formulas().stream().filter(f -> enAlcance.test(f.getPunto())).toList();
        List<Dispensacion> dispensaciones = d.dispensaciones().stream()
                .filter(x -> enAlcance.test(x.getFormula().getPunto())).toList();
        List<ConteoFisico> conteos = d.conteos().stream()
                .filter(c -> enAlcance.test(c.getInventario().getPunto())).toList();
        List<Encuesta> encuestas = d.encuestas().stream()
                .filter(e -> enAlcance.test(e.getFormula().getPunto())).toList();

        List<Formula> entregadas = formulas.stream()
                .filter(f -> f.getEstado() == EstadoFormula.ENTREGADA && f.getFechaEntrega() != null).toList();
        double horasPromedio = entregadas.stream()
                .mapToLong(f -> Duration.between(f.getFechaRadicacion(), f.getFechaEntrega()).toMinutes())
                .average().orElse(0) / 60.0;
        double completas = porcentaje(dispensaciones.stream().filter(Dispensacion::isCompleta).count(),
                dispensaciones.size());
        double exactitud = porcentaje(conteos.stream().filter(ConteoFisico::coincide).count(), conteos.size());
        double satisfaccion = encuestas.stream().mapToInt(Encuesta::getCalificacion).average().orElse(0);

        List<IndicadorDto> indicadores = List.of(
                indicador("tiempo-dispensacion", "Tiempo promedio de dispensación", horasPromedio, "horas",
                        metas.tiempoDispensacionHoras(), false, entregadas.isEmpty()),
                indicador("entregas-completas", "Entregas completas", completas, "%",
                        metas.entregasCompletas(), true, dispensaciones.isEmpty()),
                indicador("exactitud-inventario", "Exactitud del inventario", exactitud, "%",
                        metas.exactitudInventario(), true, conteos.isEmpty()),
                indicador("satisfaccion", "Satisfacción de los usuarios", satisfaccion, "/ 5",
                        metas.satisfaccion(), true, encuestas.isEmpty()));

        LocalDate hoy = LocalDate.now();
        ResumenOperativoDto operacion = new ResumenOperativoDto(
                (int) formulas.stream().filter(f -> f.getEstado() == EstadoFormula.PENDIENTE
                        || f.getEstado() == EstadoFormula.EN_TRANSITO).count(),
                (int) formulas.stream().filter(f -> f.getEstado() == EstadoFormula.DISPONIBLE).count(),
                (int) d.turnos().stream().filter(t -> enAlcance.test(t.getPunto()) && t.getFecha().equals(hoy)
                        && t.getEstado() != EstadoTurno.CANCELADO).count(),
                (int) d.inventarios().stream().filter(i -> enAlcance.test(i.getPunto()) && i.getCantidad() == 0)
                        .count());

        return new TableroDto(punto == null ? null : Mapeo.punto(punto), indicadores, operacion);
    }

    private static IndicadorDto indicador(String clave, String nombre, double valor, String unidad, double meta,
                                          boolean mayorEsMejor, boolean sinDatos) {
        double redondeado = Math.round(valor * 10) / 10.0;
        boolean cumple = !sinDatos && (mayorEsMejor ? redondeado >= meta : redondeado <= meta);
        return new IndicadorDto(clave, nombre, redondeado, unidad, meta, mayorEsMejor, cumple, sinDatos);
    }

    private static double porcentaje(long parte, long total) {
        return total == 0 ? 0 : parte * 100.0 / total;
    }

    private record Datos(List<Formula> formulas, List<Dispensacion> dispensaciones, List<ConteoFisico> conteos,
                         List<Encuesta> encuestas, List<Inventario> inventarios, List<Turno> turnos) {
    }
}
