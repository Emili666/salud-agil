package co.edu.uniquindio.saludagil.dto;

import java.time.LocalDateTime;
import java.util.List;

public record PanelIndicadoresDto(TableroDto general, List<TableroDto> porPunto, LocalDateTime generadoEn) {
}
