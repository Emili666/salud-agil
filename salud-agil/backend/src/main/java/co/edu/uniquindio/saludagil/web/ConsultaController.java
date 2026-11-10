package co.edu.uniquindio.saludagil.web;

import co.edu.uniquindio.saludagil.dto.*;
import co.edu.uniquindio.saludagil.service.FormulaService;
import co.edu.uniquindio.saludagil.service.InventarioService;
import co.edu.uniquindio.saludagil.service.NotificacionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Endpoints que usa el afiliado. */
@RestController
@RequestMapping("/api")
public class ConsultaController {

    private final InventarioService inventarioService;
    private final FormulaService formulaService;
    private final NotificacionService notificacionService;

    public ConsultaController(InventarioService inventarioService, FormulaService formulaService,
                              NotificacionService notificacionService) {
        this.inventarioService = inventarioService;
        this.formulaService = formulaService;
        this.notificacionService = notificacionService;
    }

    @GetMapping("/puntos")
    public List<PuntoDto> puntos() {
        return inventarioService.listarPuntos();
    }

    @GetMapping("/medicamentos")
    public List<MedicamentoDto> medicamentos(@RequestParam(name = "q", required = false) String texto) {
        return inventarioService.buscarMedicamentos(texto);
    }

    @GetMapping("/medicamentos/{id}/disponibilidad")
    public List<DisponibilidadDto> disponibilidad(@PathVariable("id") Long medicamentoId) {
        return inventarioService.consultarDisponibilidad(medicamentoId);
    }

    @GetMapping("/afiliados/{documento}")
    public AfiliadoResumenDto afiliado(@PathVariable("documento") String documento) {
        return formulaService.resumenAfiliado(documento);
    }

    @GetMapping("/afiliados/{documento}/notificaciones")
    public List<NotificacionDto> notificaciones(@PathVariable("documento") String documento) {
        return notificacionService.listarPorDocumento(documento);
    }
}
