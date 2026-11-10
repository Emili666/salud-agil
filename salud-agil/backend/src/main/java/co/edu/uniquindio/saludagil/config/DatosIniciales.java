package co.edu.uniquindio.saludagil.config;

import co.edu.uniquindio.saludagil.domain.*;
import co.edu.uniquindio.saludagil.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Carga datos de demostración del piloto (tres puntos en Armenia: norte, centro y sur).
 * Todos los nombres, documentos, direcciones y teléfonos son ficticios.
 * Solo se ejecuta si la base de datos está vacía.
 */
@Component
public class DatosIniciales implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatosIniciales.class);

    private final PuntoEntregaRepository puntoRepository;
    private final MedicamentoRepository medicamentoRepository;
    private final InventarioRepository inventarioRepository;
    private final AfiliadoRepository afiliadoRepository;
    private final FormulaRepository formulaRepository;
    private final DispensacionRepository dispensacionRepository;
    private final TurnoRepository turnoRepository;
    private final NotificacionRepository notificacionRepository;
    private final ConteoFisicoRepository conteoRepository;
    private final EncuestaRepository encuestaRepository;
    private final SaludAgilProperties propiedades;

    private final Random azar = new Random(2026);
    private int consecutivoFormula = 0;

    public DatosIniciales(PuntoEntregaRepository puntoRepository, MedicamentoRepository medicamentoRepository,
                          InventarioRepository inventarioRepository, AfiliadoRepository afiliadoRepository,
                          FormulaRepository formulaRepository, DispensacionRepository dispensacionRepository,
                          TurnoRepository turnoRepository, NotificacionRepository notificacionRepository,
                          ConteoFisicoRepository conteoRepository, EncuestaRepository encuestaRepository,
                          SaludAgilProperties propiedades) {
        this.puntoRepository = puntoRepository;
        this.medicamentoRepository = medicamentoRepository;
        this.inventarioRepository = inventarioRepository;
        this.afiliadoRepository = afiliadoRepository;
        this.formulaRepository = formulaRepository;
        this.dispensacionRepository = dispensacionRepository;
        this.turnoRepository = turnoRepository;
        this.notificacionRepository = notificacionRepository;
        this.conteoRepository = conteoRepository;
        this.encuestaRepository = encuestaRepository;
        this.propiedades = propiedades;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (puntoRepository.count() > 0) {
            return;
        }
        LocalDateTime ahora = LocalDateTime.now();
        String horario = "Lunes a sábado, 7:00 a.m. a 5:00 p.m.";

        PuntoEntrega norte = puntoRepository.save(new PuntoEntrega("Punto Norte", Zona.NORTE,
                "Calle 50 # 14-20, norte de Armenia", horario));
        PuntoEntrega centro = puntoRepository.save(new PuntoEntrega("Punto Centro", Zona.CENTRO,
                "Carrera 16 # 21-35, centro de Armenia", horario));
        PuntoEntrega sur = puntoRepository.save(new PuntoEntrega("Punto Sur", Zona.SUR,
                "Avenida Centenario # 5-40, sur de Armenia", horario));
        List<PuntoEntrega> puntos = List.of(norte, centro, sur);

        Map<String, Medicamento> med = new LinkedHashMap<>();
        Object[][] catalogo = {
                {"Acetaminofén 500 mg", "Tabletas x 10", false},
                {"Adalimumab 40 mg", "Jeringa prellenada", true},
                {"Amlodipino 5 mg", "Tabletas x 30", false},
                {"Atorvastatina 20 mg", "Tabletas x 30", false},
                {"Carbamazepina 200 mg", "Tabletas x 30", false},
                {"Enalapril 20 mg", "Tabletas x 30", false},
                {"Hidroclorotiazida 25 mg", "Tabletas x 30", false},
                {"Insulina glargina 100 UI/mL", "Pluma precargada 3 mL", true},
                {"Levotiroxina 50 mcg", "Tabletas x 30", false},
                {"Losartán 50 mg", "Tabletas x 30", false},
                {"Metformina 850 mg", "Tabletas x 30", false},
                {"Omeprazol 20 mg", "Cápsulas x 30", false},
                {"Salbutamol 100 mcg", "Inhalador 200 dosis", false},
        };
        int n = 1;
        for (Object[] c : catalogo) {
            Medicamento m = medicamentoRepository.save(new Medicamento(String.format("MED-%03d", n++),
                    (String) c[0], (String) c[1], (Boolean) c[2]));
            med.put(((String) c[0]).split(" ")[0], m);
        }

        // ---- Inventario: base aleatoria y algunos casos forzados para la demo
        Map<String, Integer> forzado = Map.of(
                "Norte|Insulina", 0, "Norte|Adalimumab", 0, "Norte|Losartán", 140, "Norte|Metformina", 180,
                "Centro|Atorvastatina", 0, "Centro|Salbutamol", 4, "Centro|Enalapril", 90,
                "Sur|Salbutamol", 0, "Sur|Losartán", 6, "Sur|Insulina", 3);
        List<Inventario> inventarios = new ArrayList<>();
        for (PuntoEntrega p : puntos) {
            for (Map.Entry<String, Medicamento> e : med.entrySet()) {
                String clave = p.getNombre().replace("Punto ", "") + "|" + e.getKey();
                int cantidad = forzado.getOrDefault(clave, e.getValue().isAltoCosto()
                        ? 2 + azar.nextInt(10) : 20 + azar.nextInt(180));
                inventarios.add(inventarioRepository.save(new Inventario(p, e.getValue(), cantidad,
                        ahora.minusMinutes(5 + azar.nextInt(235)))));
            }
        }

        // ---- Afiliados ficticios
        Afiliado maria = afiliadoRepository.save(new Afiliado("1094000001", "María Elena Restrepo",
                "3001112233", "maria.restrepo@example.com", Canal.SMS));
        Afiliado jose = afiliadoRepository.save(new Afiliado("1094000002", "José Arcángel Giraldo",
                "3104445566", "jose.giraldo@example.com", Canal.WHATSAPP));
        Afiliado luz = afiliadoRepository.save(new Afiliado("1094000003", "Luz Dary Ospina",
                "3157778899", "luzdary.ospina@example.com", Canal.EMAIL));
        Afiliado carlos = afiliadoRepository.save(new Afiliado("1094000004", "Carlos Andrés Mejía",
                "3209990011", "carlos.mejia@example.com", Canal.WHATSAPP));

        String[] nombres = {"Ana", "Jorge", "Blanca", "Luis", "Rosa", "Hernán", "Gloria", "Fabio", "Marta",
                "Álvaro", "Cecilia", "Óscar", "Patricia", "Gustavo", "Nubia", "Ramiro", "Olga", "Wilson",
                "Amparo", "Diego"};
        String[] apellidos = {"Cardona", "Valencia", "Arias", "Zuluaga", "Henao", "López", "Montoya", "Castaño",
                "Salazar", "Quintero"};
        List<Afiliado> historicos = new ArrayList<>();
        for (int i = 0; i < nombres.length; i++) {
            Canal canal = Canal.values()[i % 3];
            historicos.add(afiliadoRepository.save(new Afiliado(String.valueOf(1094100000 + i),
                    nombres[i] + " " + apellidos[i % apellidos.length], "300" + (5000000 + i * 137),
                    "afiliado" + i + "@example.com", canal)));
        }

        // ---- Historial de los últimos 30 días (alimenta los indicadores)
        List<Medicamento> comunes = med.values().stream().filter(m -> !m.isAltoCosto()).toList();
        for (int i = 0; i < 45; i++) {
            PuntoEntrega p = puntos.get(i % 3);
            Afiliado a = historicos.get(azar.nextInt(historicos.size()));
            LocalDateTime radicada = ahora.minusDays(2 + azar.nextInt(28)).withHour(7 + azar.nextInt(9))
                    .withMinute(azar.nextInt(60));
            Formula f = new Formula(codigo(), a, p, radicada);
            f.agregarItem(comunes.get(azar.nextInt(comunes.size())), 30);
            if (azar.nextBoolean()) {
                f.agregarItem(comunes.get(azar.nextInt(comunes.size())), 30);
            }
            // El punto sur tiene más faltantes en el histórico: entregas más lentas y más parciales.
            boolean parcial = azar.nextInt(100) < (p == sur ? 35 : 12);
            int horas = parcial ? 30 + azar.nextInt(70) : 1 + azar.nextInt(p == sur ? 40 : 20);
            LocalDateTime entregada = radicada.plusHours(horas);
            f.getItems().forEach(it -> it.registrarEntrega(it.getCantidadPrescrita()));
            f.marcarEntregada(entregada);
            formulaRepository.save(f);
            if (parcial) {
                dispensacionRepository.save(new Dispensacion(f, radicada.plusMinutes(40), false,
                        "Entrega parcial (histórico)"));
            }
            dispensacionRepository.save(new Dispensacion(f, entregada, true, "Entrega completa (histórico)"));
            if (azar.nextInt(100) < 65) {
                int nota = parcial ? 2 + azar.nextInt(3) : 3 + azar.nextInt(3);
                encuestaRepository.save(new Encuesta(f, nota, null, entregada.plusHours(2)));
            }
        }

        // ---- Conteos físicos de inventario (exactitud)
        for (int i = 0; i < 36; i++) {
            Inventario inv = inventarios.get(azar.nextInt(inventarios.size()));
            int sistema = inv.getCantidad() + azar.nextInt(10);
            boolean coincide = azar.nextInt(100) < (inv.getPunto() == sur ? 80 : 95);
            int fisico = coincide ? sistema : Math.max(0, sistema - 1 - azar.nextInt(4));
            conteoRepository.save(new ConteoFisico(inv, sistema, fisico, ahora.minusDays(1 + azar.nextInt(29))));
        }

        // ---- Casos activos para la demostración
        // María: una fórmula lista para reclamar y otra pendiente (insulina agotada en el norte)
        Formula f1 = new Formula(codigo(), maria, norte, ahora.minusHours(20));
        f1.agregarItem(med.get("Losartán"), 30);
        f1.agregarItem(med.get("Metformina"), 60);
        f1.setEstado(EstadoFormula.DISPONIBLE);
        formulaRepository.save(f1);
        aviso(maria, f1, "Su fórmula " + f1.getCodigo() + " está lista en Punto Norte. Agende un turno para reclamarla.",
                ahora.minusHours(20));

        Formula f2 = new Formula(codigo(), maria, norte, ahora.minusHours(30));
        f2.agregarItem(med.get("Insulina"), 2);
        formulaRepository.save(f2);
        aviso(maria, f2, "Recibimos su fórmula " + f2.getCodigo() + " en Punto Norte. Algunos medicamentos no "
                + "están disponibles; le avisaremos cuando lleguen.", ahora.minusHours(30));

        // José: una pendiente que ya va en camino y una entregada sin calificar
        Formula f3 = new Formula(codigo(), jose, centro, ahora.minusDays(2));
        f3.agregarItem(med.get("Atorvastatina"), 30);
        f3.setEstado(EstadoFormula.EN_TRANSITO);
        formulaRepository.save(f3);
        aviso(jose, f3, "Recibimos su fórmula " + f3.getCodigo() + " en Punto Centro. Algunos medicamentos no "
                + "están disponibles; le avisaremos cuando lleguen.", ahora.minusDays(2));
        aviso(jose, f3, "Los medicamentos pendientes de su fórmula " + f3.getCodigo()
                + " van en camino a Punto Centro.", ahora.minusHours(5));

        Formula f4 = new Formula(codigo(), jose, centro, ahora.minusDays(3));
        f4.agregarItem(med.get("Losartán"), 30);
        f4.getItems().forEach(it -> it.registrarEntrega(it.getCantidadPrescrita()));
        f4.marcarEntregada(ahora.minusDays(1));
        formulaRepository.save(f4);
        dispensacionRepository.save(new Dispensacion(f4, ahora.minusDays(1), true, "Entregado: Losartán 50 mg x30"));
        aviso(jose, f4, "Su fórmula " + f4.getCodigo() + " fue entregada completa. Califique la atención en "
                + "Salud Ágil.", ahora.minusDays(1));

        // Luz Dary: entrega parcial ayer; falta el inhalador (agotado en el sur)
        Formula f5 = new Formula(codigo(), luz, sur, ahora.minusDays(1).minusHours(3));
        f5.agregarItem(med.get("Levotiroxina"), 30);
        f5.agregarItem(med.get("Salbutamol"), 1);
        f5.getItems().get(0).registrarEntrega(30);
        formulaRepository.save(f5);
        dispensacionRepository.save(new Dispensacion(f5, ahora.minusDays(1), false,
                "Entregado: Levotiroxina 50 mcg x30 | Pendiente: Salbutamol 100 mcg x1"));
        aviso(luz, f5, "Entrega parcial de su fórmula " + f5.getCodigo() + ". Quedan pendientes: Salbutamol "
                + "100 mcg x1. Le avisaremos cuando lleguen a Punto Sur.", ahora.minusDays(1));

        // Carlos (cuidador de su madre): fórmula lista y turno ya agendado
        Formula f6 = new Formula(codigo(), carlos, centro, ahora.minusHours(6));
        f6.agregarItem(med.get("Enalapril"), 60);
        f6.setEstado(EstadoFormula.DISPONIBLE);
        formulaRepository.save(f6);
        LocalDateTime cita = proximaFranja(ahora.plusHours(1));
        String codigoTurno = "C" + cita.format(DateTimeFormatter.ofPattern("MMdd")) + "-001";
        turnoRepository.save(new Turno(codigoTurno, f6, cita.toLocalDate(), cita.toLocalTime()));
        aviso(carlos, f6, "Su fórmula " + f6.getCodigo() + " está lista en Punto Centro. Agende un turno para "
                + "reclamarla.", ahora.minusHours(6));
        aviso(carlos, f6, "Turno " + codigoTurno + " confirmado en Punto Centro. Lleve su documento de identidad.",
                ahora.minusHours(5));

        log.info("Datos de demostración cargados: {} puntos, {} medicamentos, {} fórmulas. "
                        + "Pruebe con los documentos 1094000001 a 1094000004.",
                puntos.size(), med.size(), formulaRepository.count());
    }

    private String codigo() {
        return String.format("F-%05d", ++consecutivoFormula);
    }

    private void aviso(Afiliado a, Formula f, String mensaje, LocalDateTime fecha) {
        notificacionRepository.save(new Notificacion(a, f, a.getCanalPreferido(), a.destinoNotificacion(),
                mensaje, fecha));
    }

    /** Primera franja de atención (lun-sáb) igual o posterior al momento dado. */
    private LocalDateTime proximaFranja(LocalDateTime desde) {
        SaludAgilProperties.Turnos t = propiedades.turnos();
        LocalDate dia = desde.toLocalDate();
        LocalTime hora = t.horaApertura();
        while (true) {
            if (dia.getDayOfWeek() != DayOfWeek.SUNDAY) {
                for (LocalTime h = t.horaApertura(); h.isBefore(t.horaCierre()); h = h.plusMinutes(t.minutosFranja())) {
                    if (!dia.atTime(h).isBefore(desde)) {
                        return dia.atTime(h);
                    }
                }
            }
            dia = dia.plusDays(1);
            desde = dia.atTime(hora);
        }
    }
}
