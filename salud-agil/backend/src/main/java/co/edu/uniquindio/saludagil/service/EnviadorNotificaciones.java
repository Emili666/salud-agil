package co.edu.uniquindio.saludagil.service;

import co.edu.uniquindio.saludagil.domain.Canal;

/**
 * Puerto de salida para enviar mensajes. El prototipo usa {@link EnviadorSimulado};
 * en producción se crearía una implementación por proveedor (Twilio para SMS,
 * WhatsApp Cloud API, SMTP para correo) sin tocar el resto del código.
 */
public interface EnviadorNotificaciones {
    void enviar(Canal canal, String destino, String mensaje);
}
