package co.edu.uniquindio.saludagil.dto;

/**
 * Un indicador del tablero con su meta (acuerdo de nivel de servicio).
 *
 * @param mayorEsMejor true si el indicador cumple cuando valor >= meta; false si cumple cuando valor <= meta.
 * @param sinDatos     true si todavía no hay registros para calcularlo.
 */
public record IndicadorDto(String clave, String nombre, double valor, String unidad, double meta,
                           boolean mayorEsMejor, boolean cumple, boolean sinDatos) {
}
