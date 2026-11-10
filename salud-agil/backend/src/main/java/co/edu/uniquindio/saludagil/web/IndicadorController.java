package co.edu.uniquindio.saludagil.web;

import co.edu.uniquindio.saludagil.dto.PanelIndicadoresDto;
import co.edu.uniquindio.saludagil.service.IndicadorService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/indicadores")
public class IndicadorController {

    private final IndicadorService indicadorService;

    public IndicadorController(IndicadorService indicadorService) {
        this.indicadorService = indicadorService;
    }

    @GetMapping
    public PanelIndicadoresDto panel() {
        return indicadorService.calcular();
    }
}
