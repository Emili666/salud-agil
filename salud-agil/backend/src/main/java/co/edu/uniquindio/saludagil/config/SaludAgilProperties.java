package co.edu.uniquindio.saludagil.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.LocalTime;

/** Reglas de negocio configurables desde application.properties (prefijo "saludagil"). */
@ConfigurationProperties(prefix = "saludagil")
public record SaludAgilProperties(Turnos turnos, Inventario inventario, Metas metas, String corsOrigenes) {

    /** Franjas de atención para los turnos. Horas en formato HH:mm. */
    public record Turnos(String apertura, String cierre, int minutosFranja, int cupoPorFranja,
                         int diasMaximosAnticipacion) {
        public LocalTime horaApertura() { return LocalTime.parse(apertura); }
        public LocalTime horaCierre() { return LocalTime.parse(cierre); }
    }

    /** Por debajo de este número de unidades se muestra "pocas unidades". */
    public record Inventario(int umbralPocasUnidades) {
    }

    /** Metas de los acuerdos de nivel de servicio (SLA) que se muestran en el tablero. */
    public record Metas(double tiempoDispensacionHoras, double entregasCompletas, double exactitudInventario,
                        double satisfaccion) {
    }
}
