package co.edu.uniquindio.saludagil.service;

import co.edu.uniquindio.saludagil.config.SaludAgilProperties;
import co.edu.uniquindio.saludagil.domain.NivelDisponibilidad;
import org.springframework.stereotype.Component;

/** Traduce una cantidad en un nivel que el afiliado entiende: disponible, pocas unidades o agotado. */
@Component
public class ClasificadorDisponibilidad {

    private final int umbralPocasUnidades;

    public ClasificadorDisponibilidad(SaludAgilProperties propiedades) {
        this.umbralPocasUnidades = propiedades.inventario().umbralPocasUnidades();
    }

    public NivelDisponibilidad clasificar(int cantidad) {
        if (cantidad <= 0) {
            return NivelDisponibilidad.AGOTADO;
        }
        return cantidad < umbralPocasUnidades ? NivelDisponibilidad.POCAS_UNIDADES : NivelDisponibilidad.DISPONIBLE;
    }
}
