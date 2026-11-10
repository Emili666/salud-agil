package co.edu.uniquindio.saludagil.service;

import co.edu.uniquindio.saludagil.domain.*;
import co.edu.uniquindio.saludagil.dto.*;
import co.edu.uniquindio.saludagil.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Ciclo de vida de una fórmula:
 * <pre>
 *  radicar ──► PENDIENTE ──(farmacia la despacha)──► EN_TRANSITO
 *                 │  ▲                                   │
 *     llegan las  │  │ se agotan antes                   │ llegan las
 *     existencias ▼  │ de la entrega                     ▼ existencias
 *               DISPONIBLE ◄─────────────────────────────┘
 *                 │
 *     dispensar   ├── completa ──► ENTREGADA
 *                 └── parcial  ──► PENDIENTE (con lo que falta)
 * </pre>
 * Cada cambio de estado relevante genera una notificación al afiliado.
 */
@Service
@Transactional
public class FormulaService {

    private static final EnumSet<EstadoFormula> ABIERTAS =
            EnumSet.of(EstadoFormula.PENDIENTE, EstadoFormula.EN_TRANSITO, EstadoFormula.DISPONIBLE);

    private final FormulaRepository formulaRepository;
    private final AfiliadoRepository afiliadoRepository;
    private final PuntoEntregaRepository puntoRepository;
    private final MedicamentoRepository medicamentoRepository;
    private final InventarioRepository inventarioRepository;
    private final DispensacionRepository dispensacionRepository;
    private final TurnoRepository turnoRepository;
    private final EncuestaRepository encuestaRepository;
    private final NotificacionService notificacionService;
    private final ClasificadorDisponibilidad clasificador;

    public FormulaService(FormulaRepository formulaRepository, AfiliadoRepository afiliadoRepository,
                          PuntoEntregaRepository puntoRepository, MedicamentoRepository medicamentoRepository,
                          InventarioRepository inventarioRepository, DispensacionRepository dispensacionRepository,
                          TurnoRepository turnoRepository, EncuestaRepository encuestaRepository,
                          NotificacionService notificacionService, ClasificadorDisponibilidad clasificador) {
        this.formulaRepository = formulaRepository;
        this.afiliadoRepository = afiliadoRepository;
        this.puntoRepository = puntoRepository;
        this.medicamentoRepository = medicamentoRepository;
        this.inventarioRepository = inventarioRepository;
        this.dispensacionRepository = dispensacionRepository;
        this.turnoRepository = turnoRepository;
        this.encuestaRepository = encuestaRepository;
        this.notificacionService = notificacionService;
        this.clasificador = clasificador;
    }

    // ---------------------------------------------------------------- consultas

    @Transactional(readOnly = true)
    public FormulaDto obtener(String codigo) {
        return aDto(buscar(codigo));
    }

    @Transactional(readOnly = true)
    public List<FormulaDto> listarPorPunto(Long puntoId) {
        return formulaRepository.findByPuntoIdOrderByFechaRadicacionDesc(puntoId).stream()
                .map(this::aDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public AfiliadoResumenDto resumenAfiliado(String documento) {
        Afiliado afiliado = afiliadoRepository.findByDocumento(documento.trim())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No encontramos un afiliado con el documento " + documento));
        List<FormulaDto> formulas = formulaRepository
                .findByAfiliadoDocumentoOrderByFechaRadicacionDesc(afiliado.getDocumento()).stream()
                .map(this::aDto)
                .toList();
        return new AfiliadoResumenDto(afiliado.getDocumento(), afiliado.getNombres(), afiliado.getCanalPreferido(),
                Mapeo.enmascarar(afiliado.destinoNotificacion()), formulas,
                notificacionService.listarPorDocumento(afiliado.getDocumento()));
    }

    // ---------------------------------------------------------------- operaciones

    /** La farmacia registra una fórmula nueva. Queda DISPONIBLE si hay existencias para todo. */
    public FormulaDto radicar(RadicarFormulaRequest solicitud) {
        Afiliado afiliado = afiliadoRepository.findByDocumento(solicitud.documento().trim())
                .orElseThrow(() -> new RecursoNoEncontradoException("Afiliado no encontrado: " + solicitud.documento()));
        PuntoEntrega punto = puntoRepository.findById(solicitud.puntoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Punto de entrega no encontrado"));

        Formula formula = new Formula(siguienteCodigo(), afiliado, punto, LocalDateTime.now());
        for (RadicarFormulaRequest.Item item : solicitud.items()) {
            Medicamento medicamento = medicamentoRepository.findById(item.medicamentoId())
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                            "Medicamento no encontrado: " + item.medicamentoId()));
            formula.agregarItem(medicamento, item.cantidad());
        }
        formula.setEstado(estadoSegunInventario(formula));
        formulaRepository.save(formula);

        if (formula.getEstado() == EstadoFormula.DISPONIBLE) {
            notificacionService.notificar(afiliado, formula, "Su fórmula " + formula.getCodigo()
                    + " está lista en " + punto.getNombre() + ". Agende un turno para reclamarla.");
        } else {
            notificacionService.notificar(afiliado, formula, "Recibimos su fórmula " + formula.getCodigo()
                    + " en " + punto.getNombre()
                    + ". Algunos medicamentos no están disponibles; le avisaremos cuando lleguen.");
        }
        return aDto(formula);
    }

    /** La farmacia confirma que el pedido de reposición salió de bodega hacia el punto. */
    public FormulaDto marcarEnTransito(String codigo) {
        Formula formula = buscar(codigo);
        if (formula.getEstado() != EstadoFormula.PENDIENTE) {
            throw new ReglaNegocioException("Solo una fórmula PENDIENTE puede pasar a EN TRÁNSITO (estado actual: "
                    + formula.getEstado() + ")");
        }
        formula.setEstado(EstadoFormula.EN_TRANSITO);
        notificacionService.notificar(formula.getAfiliado(), formula, "Los medicamentos pendientes de su fórmula "
                + formula.getCodigo() + " van en camino a " + formula.getPunto().getNombre() + ".");
        return aDto(formula);
    }

    /** Entrega en ventanilla todo lo que haya en existencia. Si falta algo, la entrega es parcial. */
    public DispensacionResultadoDto dispensar(String codigo) {
        Formula formula = buscar(codigo);
        if (formula.getEstado() == EstadoFormula.ENTREGADA) {
            throw new ReglaNegocioException("La fórmula " + codigo + " ya fue entregada.");
        }
        Long puntoId = formula.getPunto().getId();
        List<String> entregados = new ArrayList<>();
        List<String> pendientes = new ArrayList<>();
        List<Long> medicamentosMovidos = new ArrayList<>();

        for (FormulaItem item : formula.getItems()) {
            int pendiente = item.getCantidadPendiente();
            if (pendiente == 0) {
                continue;
            }
            Inventario inventario = inventarioRepository
                    .findByPuntoIdAndMedicamentoId(puntoId, item.getMedicamento().getId())
                    .orElse(null);
            int existencias = inventario == null ? 0 : inventario.getCantidad();
            int aEntregar = Math.min(pendiente, existencias);
            if (aEntregar > 0) {
                inventario.actualizarCantidad(existencias - aEntregar);
                item.registrarEntrega(aEntregar);
                entregados.add(item.getMedicamento().getNombre() + " x" + aEntregar);
                medicamentosMovidos.add(item.getMedicamento().getId());
            }
            if (item.getCantidadPendiente() > 0) {
                pendientes.add(item.getMedicamento().getNombre() + " x" + item.getCantidadPendiente());
            }
        }

        if (entregados.isEmpty()) {
            throw new ReglaNegocioException(
                    "No hay existencias en el punto para ninguno de los medicamentos pendientes.");
        }

        LocalDateTime ahora = LocalDateTime.now();
        boolean completa = formula.todoEntregado();
        String detalle = "Entregado: " + String.join(", ", entregados)
                + (completa ? "" : " | Pendiente: " + String.join(", ", pendientes));

        if (completa) {
            formula.marcarEntregada(ahora);
        } else {
            formula.setEstado(EstadoFormula.PENDIENTE);
        }
        dispensacionRepository.save(new Dispensacion(formula, ahora, completa, detalle));
        turnoRepository.findFirstByFormulaIdAndEstado(formula.getId(), EstadoTurno.PROGRAMADO)
                .ifPresent(turno -> turno.setEstado(EstadoTurno.ATENDIDO));

        if (completa) {
            notificacionService.notificar(formula.getAfiliado(), formula, "Su fórmula " + formula.getCodigo()
                    + " fue entregada completa. Califique la atención en Salud Ágil.");
        } else {
            notificacionService.notificar(formula.getAfiliado(), formula, "Entrega parcial de su fórmula "
                    + formula.getCodigo() + ". Quedan pendientes: " + String.join(", ", pendientes)
                    + ". Le avisaremos cuando lleguen a " + formula.getPunto().getNombre() + ".");
        }

        // Lo que se entregó puede dejar sin existencias a otras fórmulas del mismo punto.
        for (Long medicamentoId : medicamentosMovidos) {
            reevaluarPorCambioDeInventario(puntoId, medicamentoId);
        }
        return new DispensacionResultadoDto(aDto(formula), completa, detalle);
    }

    /**
     * Se llama cada vez que cambian las existencias de un medicamento en un punto.
     * Actualiza el estado de las fórmulas abiertas que lo necesitan y avisa a los afiliados.
     */
    public void reevaluarPorCambioDeInventario(Long puntoId, Long medicamentoId) {
        for (Formula formula : formulaRepository.findByPuntoIdAndEstadoIn(puntoId, ABIERTAS)) {
            boolean loNecesita = formula.getItems().stream().anyMatch(i ->
                    i.getMedicamento().getId().equals(medicamentoId) && i.getCantidadPendiente() > 0);
            if (!loNecesita) {
                continue;
            }
            EstadoFormula anterior = formula.getEstado();
            EstadoFormula nuevo = estadoSegunInventario(formula);
            if (anterior == nuevo) {
                continue;
            }
            formula.setEstado(nuevo);
            if (nuevo == EstadoFormula.DISPONIBLE) {
                notificacionService.notificar(formula.getAfiliado(), formula, "Sus medicamentos de la fórmula "
                        + formula.getCodigo() + " ya están disponibles en " + formula.getPunto().getNombre()
                        + ". Agende su turno para reclamarlos.");
            } else if (anterior == EstadoFormula.DISPONIBLE) {
                notificacionService.notificar(formula.getAfiliado(), formula, "Las existencias para su fórmula "
                        + formula.getCodigo() + " se agotaron antes de su entrega. Le avisaremos apenas lleguen.");
            }
        }
    }

    public void registrarEncuesta(String codigo, EncuestaRequest solicitud) {
        Formula formula = buscar(codigo);
        if (formula.getEstado() != EstadoFormula.ENTREGADA) {
            throw new ReglaNegocioException("Puede calificar la atención cuando la fórmula esté entregada.");
        }
        if (encuestaRepository.existsByFormulaId(formula.getId())) {
            throw new ReglaNegocioException("Esta fórmula ya fue calificada. ¡Gracias!");
        }
        encuestaRepository.save(new Encuesta(formula, solicitud.calificacion(), solicitud.comentario(),
                LocalDateTime.now()));
    }

    // ---------------------------------------------------------------- apoyo

    public Formula buscar(String codigo) {
        return formulaRepository.findByCodigo(codigo.trim().toUpperCase())
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la fórmula " + codigo));
    }

    private EstadoFormula estadoSegunInventario(Formula formula) {
        if (formula.getEstado() == EstadoFormula.ENTREGADA) {
            return EstadoFormula.ENTREGADA;
        }
        if (hayExistenciasParaTodo(formula)) {
            return EstadoFormula.DISPONIBLE;
        }
        return formula.getEstado() == EstadoFormula.EN_TRANSITO ? EstadoFormula.EN_TRANSITO : EstadoFormula.PENDIENTE;
    }

    private boolean hayExistenciasParaTodo(Formula formula) {
        for (FormulaItem item : formula.getItems()) {
            if (item.getCantidadPendiente() > 0
                    && existencias(formula.getPunto().getId(), item.getMedicamento().getId()) < item.getCantidadPendiente()) {
                return false;
            }
        }
        return true;
    }

    private int existencias(Long puntoId, Long medicamentoId) {
        return inventarioRepository.findByPuntoIdAndMedicamentoId(puntoId, medicamentoId)
                .map(Inventario::getCantidad)
                .orElse(0);
    }

    private String siguienteCodigo() {
        long n = formulaRepository.count() + 1;
        String codigo = String.format("F-%05d", n);
        while (formulaRepository.findByCodigo(codigo).isPresent()) {
            codigo = String.format("F-%05d", ++n);
        }
        return codigo;
    }

    public FormulaDto aDto(Formula formula) {
        Long puntoId = formula.getPunto().getId();
        List<FormulaItemDto> items = formula.getItems().stream().map(item -> {
            int enPunto = existencias(puntoId, item.getMedicamento().getId());
            return new FormulaItemDto(Mapeo.medicamento(item.getMedicamento()), item.getCantidadPrescrita(),
                    item.getCantidadEntregada(), item.getCantidadPendiente(), enPunto, clasificador.clasificar(enPunto));
        }).toList();
        TurnoDto turno = turnoRepository.findFirstByFormulaIdAndEstado(formula.getId(), EstadoTurno.PROGRAMADO)
                .map(Mapeo::turno)
                .orElse(null);
        Afiliado afiliado = formula.getAfiliado();
        return new FormulaDto(formula.getCodigo(), afiliado.getNombres(), afiliado.getDocumento(),
                Mapeo.punto(formula.getPunto()), formula.getEstado(), formula.getFechaRadicacion(),
                formula.getFechaEntrega(), items, turno, encuestaRepository.existsByFormulaId(formula.getId()));
    }
}
