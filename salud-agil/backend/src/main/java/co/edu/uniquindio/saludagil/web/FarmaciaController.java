package co.edu.uniquindio.saludagil.web;

import co.edu.uniquindio.saludagil.dto.*;
import co.edu.uniquindio.saludagil.service.FormulaService;
import co.edu.uniquindio.saludagil.service.InventarioService;
import co.edu.uniquindio.saludagil.service.TurnoService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Endpoints del personal de farmacia. En el prototipo no tienen autenticación;
 * en producción irían protegidos (Spring Security + rol FARMACIA).
 */
@RestController
@RequestMapping("/api/farmacia")
public class FarmaciaController {

    private final InventarioService inventarioService;
    private final FormulaService formulaService;
    private final TurnoService turnoService;

    public FarmaciaController(InventarioService inventarioService, FormulaService formulaService,
                              TurnoService turnoService) {
        this.inventarioService = inventarioService;
        this.formulaService = formulaService;
        this.turnoService = turnoService;
    }

    @GetMapping("/puntos/{puntoId}/inventario")
    public List<InventarioDto> inventario(@PathVariable("puntoId") Long puntoId) {
        return inventarioService.listarPorPunto(puntoId);
    }

    @PutMapping("/inventario")
    public InventarioDto actualizarInventario(@Valid @RequestBody ActualizarInventarioRequest solicitud) {
        return inventarioService.actualizar(solicitud);
    }

    @PostMapping("/conteos")
    @ResponseStatus(HttpStatus.CREATED)
    public ConteoResultadoDto registrarConteo(@Valid @RequestBody ConteoFisicoRequest solicitud) {
        return inventarioService.registrarConteo(solicitud);
    }

    @GetMapping("/puntos/{puntoId}/formulas")
    public List<FormulaDto> formulas(@PathVariable("puntoId") Long puntoId) {
        return formulaService.listarPorPunto(puntoId);
    }

    @PostMapping("/formulas")
    @ResponseStatus(HttpStatus.CREATED)
    public FormulaDto radicar(@Valid @RequestBody RadicarFormulaRequest solicitud) {
        return formulaService.radicar(solicitud);
    }

    @PostMapping("/formulas/{codigo}/en-transito")
    public FormulaDto enTransito(@PathVariable("codigo") String codigo) {
        return formulaService.marcarEnTransito(codigo);
    }

    @PostMapping("/formulas/{codigo}/dispensar")
    public DispensacionResultadoDto dispensar(@PathVariable("codigo") String codigo) {
        return formulaService.dispensar(codigo);
    }

    @GetMapping("/puntos/{puntoId}/turnos")
    public List<TurnoDto> turnos(@PathVariable("puntoId") Long puntoId,
                                 @RequestParam(name = "fecha", required = false)
                                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return turnoService.listarPorPunto(puntoId, fecha == null ? LocalDate.now() : fecha);
    }
}
