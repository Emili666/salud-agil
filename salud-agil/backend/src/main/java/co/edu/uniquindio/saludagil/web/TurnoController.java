package co.edu.uniquindio.saludagil.web;

import co.edu.uniquindio.saludagil.dto.AgendarTurnoRequest;
import co.edu.uniquindio.saludagil.dto.FranjaDto;
import co.edu.uniquindio.saludagil.dto.TurnoDto;
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

    public TurnoController(TurnoService turnoService) {
        this.turnoService = turnoService;
    }

    @GetMapping("/franjas")
    public List<FranjaDto> franjas(@RequestParam("puntoId") Long puntoId,
                                   @RequestParam("fecha") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return turnoService.franjas(puntoId, fecha);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TurnoDto agendar(@Valid @RequestBody AgendarTurnoRequest solicitud) {
        return turnoService.agendar(solicitud);
    }

    @DeleteMapping("/{codigo}")
    public TurnoDto cancelar(@PathVariable("codigo") String codigo) {
        return turnoService.cancelar(codigo);
    }
}
