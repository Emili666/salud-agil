package co.edu.uniquindio.saludagil.service;

import co.edu.uniquindio.saludagil.domain.Afiliado;
import co.edu.uniquindio.saludagil.domain.Canal;
import co.edu.uniquindio.saludagil.domain.Rol;
import co.edu.uniquindio.saludagil.domain.Usuario;
import co.edu.uniquindio.saludagil.dto.LoginRequest;
import co.edu.uniquindio.saludagil.dto.RegistroRequest;
import co.edu.uniquindio.saludagil.dto.SesionDto;
import co.edu.uniquindio.saludagil.repository.AfiliadoRepository;
import co.edu.uniquindio.saludagil.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Autenticación simple para el MVP: usuario y contraseña, y un token aleatorio guardado en memoria.
 * Al reiniciar el backend todas las sesiones se cierran (igual que los datos de H2).
 */
@Service
public class AuthService {

    private static final Duration DURACION_SESION = Duration.ofHours(8);

    private final Map<String, Sesion> sesiones = new ConcurrentHashMap<>();
    private final SecureRandom azar = new SecureRandom();

    private final UsuarioRepository usuarioRepository;
    private final AfiliadoRepository afiliadoRepository;
    private final EnviadorNotificaciones enviador;

    public AuthService(UsuarioRepository usuarioRepository, AfiliadoRepository afiliadoRepository,
                       EnviadorNotificaciones enviador) {
        this.usuarioRepository = usuarioRepository;
        this.afiliadoRepository = afiliadoRepository;
        this.enviador = enviador;
    }

    @Transactional(readOnly = true)
    public SesionDto iniciarSesion(LoginRequest solicitud) {
        Usuario usuario = usuarioRepository.findByLogin(solicitud.usuario().trim().toLowerCase())
                .filter(u -> ClaveHasher.coincide(solicitud.clave(), u.getClaveHash()))
                .orElseThrow(() -> new NoAutenticadoException("Usuario o contraseña incorrectos."));
        return abrirSesion(usuario);
    }

    /**
     * Crea la cuenta de un afiliado que ya existe en la EPS. El correo registrado queda como
     * canal de avisos, así las notificaciones de fórmulas y turnos le llegan a ese correo.
     */
    @Transactional
    public SesionDto registrarAfiliado(RegistroRequest solicitud) {
        String documento = solicitud.documento().trim();
        Afiliado afiliado = afiliadoRepository.findByDocumento(documento)
                .orElseThrow(() -> new ReglaNegocioException(
                        "El documento " + documento + " no corresponde a un afiliado de la EPS."));
        if (usuarioRepository.existsByLogin(documento)) {
            throw new ReglaNegocioException("Ya existe una cuenta para este documento. Inicie sesión.");
        }
        String correo = solicitud.correo().trim().toLowerCase();
        afiliado.usarCorreoParaAvisos(correo);
        Usuario usuario = usuarioRepository.save(Usuario.afiliado(afiliado, ClaveHasher.hash(solicitud.clave())));

        enviador.enviar(Canal.EMAIL, correo, "Hola " + afiliado.getNombres().split(" ")[0]
                + ", su cuenta en Salud Ágil quedó creada. Desde ahora le enviaremos a este correo"
                + " los avisos de sus fórmulas y turnos.");
        return abrirSesion(usuario);
    }

    public Optional<Sesion> buscarSesion(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        Sesion sesion = sesiones.get(token);
        if (sesion == null) {
            return Optional.empty();
        }
        if (sesion.vencida()) {
            sesiones.remove(token);
            return Optional.empty();
        }
        return Optional.of(sesion);
    }

    public void cerrarSesion(String token) {
        if (token != null) {
            sesiones.remove(token);
        }
    }

    private SesionDto abrirSesion(Usuario usuario) {
        byte[] bytes = new byte[32];
        azar.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        String documento = usuario.getRol() == Rol.AFILIADO ? usuario.getAfiliado().getDocumento() : null;
        sesiones.put(token, new Sesion(token, usuario.getId(), usuario.getNombre(), usuario.getRol(), documento,
                Instant.now().plus(DURACION_SESION)));
        return new SesionDto(token, usuario.getNombre(), usuario.getRol(), documento);
    }
}
