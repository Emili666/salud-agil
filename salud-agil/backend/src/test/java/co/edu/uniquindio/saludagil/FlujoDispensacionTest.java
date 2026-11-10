package co.edu.uniquindio.saludagil;

import co.edu.uniquindio.saludagil.domain.EstadoFormula;
import co.edu.uniquindio.saludagil.domain.EstadoTurno;
import co.edu.uniquindio.saludagil.dto.*;
import co.edu.uniquindio.saludagil.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Recorre el flujo completo sobre los datos de demostración. */
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class FlujoDispensacionTest {

    @Autowired InventarioService inventarioService;
    @Autowired FormulaService formulaService;
    @Autowired TurnoService turnoService;
    @Autowired IndicadorService indicadorService;

    private PuntoDto punto(String nombre) {
        return inventarioService.listarPuntos().stream().filter(p -> p.nombre().equals(nombre)).findFirst().orElseThrow();
    }

    private MedicamentoDto medicamento(String texto) {
        return inventarioService.buscarMedicamentos(texto).get(0);
    }

    private static LocalDate proximoDiaHabil() {
        LocalDate d = LocalDate.now().plusDays(1);
        return d.getDayOfWeek() == DayOfWeek.SUNDAY ? d.plusDays(1) : d;
    }

    @Test
    void consultaDeDisponibilidadMuestraLosTresPuntos() {
        List<DisponibilidadDto> d = inventarioService.consultarDisponibilidad(medicamento("Insulina").id());
        assertEquals(3, d.size());
        assertEquals(0, d.get(0).cantidad(), "La insulina está agotada en el punto norte");
        assertNotNull(d.get(1).ultimaActualizacion());
    }

    @Test
    void llegadaDeInventarioHabilitaLaFormulaYNotifica() {
        AfiliadoResumenDto maria = formulaService.resumenAfiliado("1094000001");
        FormulaDto pendiente = maria.formulas().stream()
                .filter(f -> f.estado() == EstadoFormula.PENDIENTE).findFirst().orElseThrow();
        int avisosAntes = maria.notificaciones().size();

        inventarioService.actualizar(new ActualizarInventarioRequest(punto("Punto Norte").id(),
                medicamento("Insulina").id(), 10));

        AfiliadoResumenDto despues = formulaService.resumenAfiliado("1094000001");
        assertEquals(EstadoFormula.DISPONIBLE, formulaService.obtener(pendiente.codigo()).estado());
        assertEquals(avisosAntes + 1, despues.notificaciones().size());
        assertTrue(despues.notificaciones().get(0).mensaje().contains("ya están disponibles"));
    }

    @Test
    void turnoYDispensacionCompletaCierranLaFormula() {
        FormulaDto lista = formulaService.resumenAfiliado("1094000001").formulas().stream()
                .filter(f -> f.estado() == EstadoFormula.DISPONIBLE).findFirst().orElseThrow();
        LocalDate dia = proximoDiaHabil();
        FranjaDto franja = turnoService.franjas(lista.punto().id(), dia).get(0);

        TurnoDto turno = turnoService.agendar(new AgendarTurnoRequest(lista.codigo(), dia, franja.hora()));
        assertEquals(EstadoTurno.PROGRAMADO, turno.estado());
        assertThrows(ReglaNegocioException.class, () ->
                turnoService.agendar(new AgendarTurnoRequest(lista.codigo(), dia, franja.hora())));

        DispensacionResultadoDto r = formulaService.dispensar(lista.codigo());
        assertTrue(r.completa());
        assertEquals(EstadoFormula.ENTREGADA, r.formula().estado());
        assertNull(r.formula().turnoVigente(), "El turno queda atendido");

        formulaService.registrarEncuesta(lista.codigo(), new EncuestaRequest(5, "Muy rápido"));
        assertThrows(ReglaNegocioException.class, () ->
                formulaService.registrarEncuesta(lista.codigo(), new EncuestaRequest(4, null)));
    }

    @Test
    void entregaParcialDejaLaFormulaPendiente() {
        PuntoDto sur = punto("Punto Sur");
        FormulaDto nueva = formulaService.radicar(new RadicarFormulaRequest("1094000003", sur.id(), List.of(
                new RadicarFormulaRequest.Item(medicamento("Omeprazol").id(), 10),
                new RadicarFormulaRequest.Item(medicamento("Salbutamol").id(), 1))));
        assertEquals(EstadoFormula.PENDIENTE, nueva.estado());
        assertThrows(ReglaNegocioException.class, () -> turnoService.agendar(
                new AgendarTurnoRequest(nueva.codigo(), proximoDiaHabil(), java.time.LocalTime.of(8, 0))));

        DispensacionResultadoDto r = formulaService.dispensar(nueva.codigo());
        assertFalse(r.completa());
        assertEquals(EstadoFormula.PENDIENTE, r.formula().estado());
        assertTrue(r.detalle().contains("Pendiente: Salbutamol"), r.detalle());
    }

    @Test
    void cupoPorFranjaSeRespeta() {
        PuntoDto centro = punto("Punto Centro");
        LocalDate dia = proximoDiaHabil();
        java.time.LocalTime hora = java.time.LocalTime.of(9, 0);
        MedicamentoDto acetaminofen = medicamento("Acetaminof");
        for (int i = 0; i < 4; i++) {
            FormulaDto f = formulaService.radicar(new RadicarFormulaRequest("1094000004", centro.id(),
                    List.of(new RadicarFormulaRequest.Item(acetaminofen.id(), 1))));
            turnoService.agendar(new AgendarTurnoRequest(f.codigo(), dia, hora));
        }
        FormulaDto quinta = formulaService.radicar(new RadicarFormulaRequest("1094000004", centro.id(),
                List.of(new RadicarFormulaRequest.Item(acetaminofen.id(), 1))));
        ReglaNegocioException e = assertThrows(ReglaNegocioException.class,
                () -> turnoService.agendar(new AgendarTurnoRequest(quinta.codigo(), dia, hora)));
        assertTrue(e.getMessage().contains("llena"));
    }

    @Test
    void indicadoresTienenDatosParaLaRedYCadaPunto() {
        PanelIndicadoresDto panel = indicadorService.calcular();
        assertEquals(3, panel.porPunto().size());
        assertEquals(4, panel.general().indicadores().size());
        panel.general().indicadores().forEach(i -> assertFalse(i.sinDatos(), i.nombre()));
    }
}
