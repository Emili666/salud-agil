package co.edu.uniquindio.saludagil.dto;

import co.edu.uniquindio.saludagil.domain.*;

/** Conversión de entidades a DTO. */
public final class Mapeo {

    private Mapeo() {
    }

    public static PuntoDto punto(PuntoEntrega p) {
        return new PuntoDto(p.getId(), p.getNombre(), p.getZona(), p.getDireccion(), p.getHorario());
    }

    public static MedicamentoDto medicamento(Medicamento m) {
        return new MedicamentoDto(m.getId(), m.getCodigo(), m.getNombre(), m.getPresentacion(), m.isAltoCosto());
    }

    public static TurnoDto turno(Turno t) {
        Afiliado a = t.getFormula().getAfiliado();
        return new TurnoDto(t.getCodigo(), t.getFormula().getCodigo(), a.getNombres(), a.getDocumento(),
                punto(t.getPunto()), t.getFecha(), t.getHora(), t.getEstado());
    }

    public static NotificacionDto notificacion(Notificacion n) {
        return new NotificacionDto(n.getId(), n.getCanal(), enmascarar(n.getDestino()), n.getMensaje(),
                n.getFechaEnvio(), n.getFormula() == null ? null : n.getFormula().getCodigo());
    }

    /** Oculta parte del teléfono o correo: 300****233, lu****@example.com */
    public static String enmascarar(String destino) {
        if (destino == null || destino.length() < 6) {
            return destino;
        }
        int arroba = destino.indexOf('@');
        if (arroba > 2) {
            return destino.substring(0, 2) + "****" + destino.substring(arroba);
        }
        return destino.substring(0, 3) + "****" + destino.substring(destino.length() - 3);
    }
}
