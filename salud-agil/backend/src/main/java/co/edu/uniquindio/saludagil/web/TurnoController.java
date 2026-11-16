package co.edu.uniquindio.saludagil.web;

import co.edu.uniquindio.saludagil.dto.AgendarTurnoRequest;
import co.edu.uniquindio.saludagil.dto.FranjaDto;
import co.edu.uniquindio.saludagil.dto.TurnoDto;
import co.edu.uniquindio.saludagil.service.FormulaService;
import co.edu.uniquindio.saludagil.service.Sesion;
import co.edu.uniquindio.saludagil.service.TurnoService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/turnos")
public class TurnoController {

    private final TurnoService turnoService;
    private final FormulaService formulaService;

    public TurnoController(TurnoService turnoService, FormulaService formulaService) {
        this.turnoService = turnoService;
        this.formulaService = formulaService;
    }

    @GetMapping("/franjas")
    public List<FranjaDto> franjas(@RequestParam("puntoId") Long puntoId,
                                   @RequestParam("fecha") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return turnoService.franjas(puntoId, fecha);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TurnoDto agendar(@Valid @RequestBody AgendarTurnoRequest solicitud,
                            @RequestAttribute(AutenticacionInterceptor.SESION) Sesion sesion) {
        sesion.exigirAfiliado(formulaService.buscar(solicitud.formulaCodigo()).getAfiliado().getDocumento());
        return turnoService.agendar(solicitud);
    }

    @DeleteMapping("/{codigo}")
    public TurnoDto cancelar(@PathVariable("codigo") String codigo,
                             @RequestAttribute(AutenticacionInterceptor.SESION) Sesion sesion) {
        sesion.exigirAfiliado(turnoService.documentoDelAfiliado(codigo));
        return turnoService.cancelar(codigo);
    }
}
