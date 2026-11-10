package co.edu.uniquindio.saludagil.dto;

import java.util.List;

/** Indicadores para un punto, o para toda la red si punto es null. */
public record TableroDto(PuntoDto punto, List<IndicadorDto> indicadores, ResumenOperativoDto operacion) {
}
