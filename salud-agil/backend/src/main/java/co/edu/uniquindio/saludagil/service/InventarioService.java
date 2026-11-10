package co.edu.uniquindio.saludagil.service;

import co.edu.uniquindio.saludagil.domain.ConteoFisico;
import co.edu.uniquindio.saludagil.domain.Inventario;
import co.edu.uniquindio.saludagil.domain.Medicamento;
import co.edu.uniquindio.saludagil.domain.PuntoEntrega;
import co.edu.uniquindio.saludagil.dto.*;
import co.edu.uniquindio.saludagil.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Consulta de disponibilidad (afiliado) y actualización de existencias (farmacia). */
@Service
@Transactional
public class InventarioService {

    private final InventarioRepository inventarioRepository;
    private final PuntoEntregaRepository puntoRepository;
    private final MedicamentoRepository medicamentoRepository;
    private final ConteoFisicoRepository conteoRepository;
    private final FormulaService formulaService;
    private final ClasificadorDisponibilidad clasificador;

    public InventarioService(InventarioRepository inventarioRepository, PuntoEntregaRepository puntoRepository,
                             MedicamentoRepository medicamentoRepository, ConteoFisicoRepository conteoRepository,
                             FormulaService formulaService, ClasificadorDisponibilidad clasificador) {
        this.inventarioRepository = inventarioRepository;
        this.puntoRepository = puntoRepository;
        this.medicamentoRepository = medicamentoRepository;
        this.conteoRepository = conteoRepository;
        this.formulaService = formulaService;
        this.clasificador = clasificador;
    }

    @Transactional(readOnly = true)
    public List<PuntoDto> listarPuntos() {
        return puntoRepository.findAllByOrderByIdAsc().stream().map(Mapeo::punto).toList();
    }

    @Transactional(readOnly = true)
    public List<MedicamentoDto> buscarMedicamentos(String texto) {
        List<Medicamento> encontrados = (texto == null || texto.isBlank())
                ? medicamentoRepository.findAllByOrderByNombreAsc()
                : medicamentoRepository.findTop20ByNombreContainingIgnoreCaseOrderByNombreAsc(texto.trim());
        return encontrados.stream().map(Mapeo::medicamento).toList();
    }

    /** Disponibilidad de un medicamento en todos los puntos, con la hora de la última actualización. */
    @Transactional(readOnly = true)
    public List<DisponibilidadDto> consultarDisponibilidad(Long medicamentoId) {
        if (!medicamentoRepository.existsById(medicamentoId)) {
            throw new RecursoNoEncontradoException("Medicamento no encontrado");
        }
        Map<Long, Inventario> porPunto = inventarioRepository.findByMedicamentoId(medicamentoId).stream()
                .collect(Collectors.toMap(i -> i.getPunto().getId(), Function.identity()));
        return puntoRepository.findAllByOrderByIdAsc().stream().map(punto -> {
            Inventario inv = porPunto.get(punto.getId());
            int cantidad = inv == null ? 0 : inv.getCantidad();
            return new DisponibilidadDto(Mapeo.punto(punto), cantidad, clasificador.clasificar(cantidad),
                    inv == null ? null : inv.getUltimaActualizacion());
        }).toList();
    }

    @Transactional(readOnly = true)
    public List<InventarioDto> listarPorPunto(Long puntoId) {
        validarPunto(puntoId);
        return inventarioRepository.findByPuntoIdOrderByMedicamentoNombreAsc(puntoId).stream()
                .map(this::aDto)
                .toList();
    }

    /** La farmacia registra la cantidad actual (p. ej. al recibir un pedido). */
    public InventarioDto actualizar(ActualizarInventarioRequest solicitud) {
        Inventario inventario = obtenerOCrear(solicitud.puntoId(), solicitud.medicamentoId());
        inventario.actualizarCantidad(solicitud.cantidad());
        inventarioRepository.save(inventario);
        formulaService.reevaluarPorCambioDeInventario(solicitud.puntoId(), solicitud.medicamentoId());
        return aDto(inventario);
    }

    /** Conteo físico: se guarda la diferencia (para el indicador de exactitud) y se corrige el sistema. */
    public ConteoResultadoDto registrarConteo(ConteoFisicoRequest solicitud) {
        Inventario inventario = obtenerOCrear(solicitud.puntoId(), solicitud.medicamentoId());
        int enSistema = inventario.getCantidad();
        int fisico = solicitud.cantidadFisica();
        inventarioRepository.save(inventario);
        conteoRepository.save(new ConteoFisico(inventario, enSistema, fisico, LocalDateTime.now()));
        inventario.actualizarCantidad(fisico);
        formulaService.reevaluarPorCambioDeInventario(solicitud.puntoId(), solicitud.medicamentoId());
        return new ConteoResultadoDto(aDto(inventario), enSistema, fisico, fisico - enSistema);
    }

    private Inventario obtenerOCrear(Long puntoId, Long medicamentoId) {
        return inventarioRepository.findByPuntoIdAndMedicamentoId(puntoId, medicamentoId).orElseGet(() -> {
            PuntoEntrega punto = validarPunto(puntoId);
            Medicamento medicamento = medicamentoRepository.findById(medicamentoId)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Medicamento no encontrado"));
            return new Inventario(punto, medicamento, 0, LocalDateTime.now());
        });
    }

    private PuntoEntrega validarPunto(Long puntoId) {
        return puntoRepository.findById(puntoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Punto de entrega no encontrado"));
    }

    private InventarioDto aDto(Inventario i) {
        return new InventarioDto(i.getId(), Mapeo.medicamento(i.getMedicamento()), i.getCantidad(),
                clasificador.clasificar(i.getCantidad()), i.getUltimaActualizacion());
    }
}
