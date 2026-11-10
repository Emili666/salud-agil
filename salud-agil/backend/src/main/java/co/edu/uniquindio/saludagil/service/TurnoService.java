package co.edu.uniquindio.saludagil.service;

import co.edu.uniquindio.saludagil.config.SaludAgilProperties;
import co.edu.uniquindio.saludagil.domain.*;
import co.edu.uniquindio.saludagil.dto.AgendarTurnoRequest;
import co.edu.uniquindio.saludagil.dto.FranjaDto;
import co.edu.uniquindio.saludagil.dto.Mapeo;
import co.edu.uniquindio.saludagil.dto.TurnoDto;
import co.edu.uniquindio.saludagil.repository.PuntoEntregaRepository;
import co.edu.uniquindio.saludagil.repository.TurnoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Agenda de turnos por franjas con cupo limitado, para repartir la demanda durante el día. */
@Service
@Transactional
public class TurnoService {

    private static final DateTimeFormatter FECHA_LARGA =
            DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", Locale.of("es", "CO"));
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("h:mm a", Locale.of("es", "CO"));

    private final TurnoRepository turnoRepository;
    private final PuntoEntregaRepository puntoRepository;
    private final FormulaService formulaService;
    private final NotificacionService notificacionService;
    private final SaludAgilProperties.Turnos reglas;

    public TurnoService(TurnoRepository turnoRepository, PuntoEntregaRepository puntoRepository,
                        FormulaService formulaService, NotificacionService notificacionService,
                        SaludAgilProperties propiedades) {
        this.turnoRepository = turnoRepository;
        this.puntoRepository = puntoRepository;
        this.formulaService = formulaService;
        this.notificacionService = notificacionService;
        this.reglas = propiedades.turnos();
    }

    /** Franjas del día con su cupo. Domingos y fechas fuera de la ventana permitida no tienen franjas. */
    @Transactional(readOnly = true)
    public List<FranjaDto> franjas(Long puntoId, LocalDate fecha) {
        if (!puntoRepository.existsById(puntoId)) {
            throw new RecursoNoEncontradoException("Punto de entrega no encontrado");
        }
        List<FranjaDto> franjas = new ArrayList<>();
        if (!fechaAtendible(fecha)) {
            return franjas;
        }
        LocalDateTime ahora = LocalDateTime.now();
        for (LocalTime hora = reglas.horaApertura(); hora.isBefore(reglas.horaCierre());
             hora = hora.plusMinutes(reglas.minutosFranja())) {
            if (!fecha.atTime(hora).isAfter(ahora)) {
                continue;
            }
            int ocupados = (int) (turnoRepository.countByPuntoIdAndFechaAndHoraAndEstado(puntoId, fecha, hora,
                    EstadoTurno.PROGRAMADO)
                    + turnoRepository.countByPuntoIdAndFechaAndHoraAndEstado(puntoId, fecha, hora,
                    EstadoTurno.ATENDIDO));
            franjas.add(new FranjaDto(hora, reglas.cupoPorFranja(), ocupados,
                    Math.max(0, reglas.cupoPorFranja() - ocupados)));
        }
        return franjas;
    }

    public TurnoDto agendar(AgendarTurnoRequest solicitud) {
        Formula formula = formulaService.buscar(solicitud.formulaCodigo());
        if (formula.getEstado() != EstadoFormula.DISPONIBLE) {
            throw new ReglaNegocioException(
                    "Solo puede agendar turno cuando todos los medicamentos de la fórmula estén disponibles.");
        }
        turnoRepository.findFirstByFormulaIdAndEstado(formula.getId(), EstadoTurno.PROGRAMADO).ifPresent(t -> {
            throw new ReglaNegocioException("Esta fórmula ya tiene el turno " + t.getCodigo() + ".");
        });
        Long puntoId = formula.getPunto().getId();
        LocalTime hora = solicitud.hora().withSecond(0).withNano(0);
        FranjaDto franja = franjas(puntoId, solicitud.fecha()).stream()
                .filter(f -> f.hora().equals(hora))
                .findFirst()
                .orElseThrow(() -> new ReglaNegocioException("La franja elegida no está disponible."));
        if (franja.disponibles() == 0) {
            throw new ReglaNegocioException("La franja de las " + hora.format(HORA) + " ya está llena. Elija otra.");
        }

        Turno turno = turnoRepository.save(new Turno(generarCodigo(formula.getPunto(), solicitud.fecha()),
                formula, solicitud.fecha(), hora));
        notificacionService.notificar(formula.getAfiliado(), formula, "Turno " + turno.getCodigo()
                + " confirmado para el " + solicitud.fecha().format(FECHA_LARGA) + " a las " + hora.format(HORA)
                + " en " + formula.getPunto().getNombre() + ". Lleve su documento de identidad.");
        return Mapeo.turno(turno);
    }

    public TurnoDto cancelar(String codigo) {
        Turno turno = turnoRepository.findByCodigo(codigo.trim().toUpperCase())
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el turno " + codigo));
        if (turno.getEstado() != EstadoTurno.PROGRAMADO) {
            throw new ReglaNegocioException("Solo se pueden cancelar turnos programados.");
        }
        turno.setEstado(EstadoTurno.CANCELADO);
        notificacionService.notificar(turno.getFormula().getAfiliado(), turno.getFormula(),
                "Su turno " + turno.getCodigo() + " fue cancelado. Puede agendar uno nuevo cuando lo necesite.");
        return Mapeo.turno(turno);
    }

    @Transactional(readOnly = true)
    public List<TurnoDto> listarPorPunto(Long puntoId, LocalDate fecha) {
        return turnoRepository.findByPuntoIdAndFechaOrderByHoraAsc(puntoId, fecha).stream()
                .map(Mapeo::turno)
                .toList();
    }

    private boolean fechaAtendible(LocalDate fecha) {
        LocalDate hoy = LocalDate.now();
        return !fecha.isBefore(hoy)
                && !fecha.isAfter(hoy.plusDays(reglas.diasMaximosAnticipacion()))
                && fecha.getDayOfWeek() != DayOfWeek.SUNDAY;
    }

    /** Ej.: N1110-004 = punto Norte, 10 de noviembre, turno 4 del día. */
    private String generarCodigo(PuntoEntrega punto, LocalDate fecha) {
        String prefijo = punto.getZona().name().charAt(0) + fecha.format(DateTimeFormatter.ofPattern("MMdd")) + "-";
        long n = turnoRepository.countByPuntoIdAndFecha(punto.getId(), fecha) + 1;
        String codigo = prefijo + String.format("%03d", n);
        while (turnoRepository.findByCodigo(codigo).isPresent()) {
            codigo = prefijo + String.format("%03d", ++n);
        }
        return codigo;
    }
}
