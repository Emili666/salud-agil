package co.edu.uniquindio.saludagil.web;

import co.edu.uniquindio.saludagil.dto.EncuestaRequest;
import co.edu.uniquindio.saludagil.dto.FormulaDto;
import co.edu.uniquindio.saludagil.service.FormulaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/formulas")
public class FormulaController {

    private final FormulaService formulaService;

    public FormulaController(FormulaService formulaService) {
        this.formulaService = formulaService;
    }

    @GetMapping("/{codigo}")
    public FormulaDto obtener(@PathVariable("codigo") String codigo) {
        return formulaService.obtener(codigo);
    }

    @PostMapping("/{codigo}/encuesta")
    @ResponseStatus(HttpStatus.CREATED)
    public void calificar(@PathVariable("codigo") String codigo, @Valid @RequestBody EncuestaRequest solicitud) {
        formulaService.registrarEncuesta(codigo, solicitud);
    }
}
