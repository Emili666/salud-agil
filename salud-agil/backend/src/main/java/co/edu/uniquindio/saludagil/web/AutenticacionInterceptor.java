package co.edu.uniquindio.saludagil.web;

import co.edu.uniquindio.saludagil.domain.Rol;
import co.edu.uniquindio.saludagil.service.AccesoDenegadoException;
import co.edu.uniquindio.saludagil.service.AuthService;
import co.edu.uniquindio.saludagil.service.NoAutenticadoException;
import co.edu.uniquindio.saludagil.service.Sesion;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Revisa el header {@code Authorization: Bearer <token>} en cada llamada a /api y aplica los permisos por rol:
 * <ul>
 *   <li>Públicas: iniciar sesión, registrarse, puntos y búsqueda/disponibilidad de medicamentos.</li>
 *   <li>/api/farmacia/** → FARMACIA · /api/indicadores/** → EPS.</li>
 *   <li>/api/afiliados/**, /api/formulas/**, /api/turnos/** → AFILIADO (y solo sus propios datos).</li>
 * </ul>
 */
@Component
public class AutenticacionInterceptor implements HandlerInterceptor {

    public static final String SESION = "saludagil.sesion";

    private final AuthService authService;

    public AutenticacionInterceptor(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String ruta = request.getRequestURI().substring(request.getContextPath().length());
        if (esPublica(request.getMethod(), ruta)) {
            return true;
        }
        Sesion sesion = authService.buscarSesion(token(request))
                .orElseThrow(() -> new NoAutenticadoException("Inicie sesión para continuar."));
        Rol requerido = rolRequerido(ruta);
        if (requerido != null && sesion.rol() != requerido) {
            throw new AccesoDenegadoException("Su perfil no tiene acceso a esta sección.");
        }
        request.setAttribute(SESION, sesion);
        return true;
    }

    private static boolean esPublica(String metodo, String ruta) {
        if (ruta.equals("/api/auth/login") || ruta.equals("/api/auth/registro")) {
            return true;
        }
        return "GET".equalsIgnoreCase(metodo)
                && (ruta.equals("/api/puntos") || ruta.startsWith("/api/medicamentos"));
    }

    private static Rol rolRequerido(String ruta) {
        if (ruta.startsWith("/api/farmacia")) {
            return Rol.FARMACIA;
        }
        if (ruta.startsWith("/api/indicadores")) {
            return Rol.EPS;
        }
        if (ruta.startsWith("/api/afiliados") || ruta.startsWith("/api/formulas") || ruta.startsWith("/api/turnos")) {
            return Rol.AFILIADO;
        }
        return null; // cualquier usuario con sesión (p. ej. /api/auth/yo)
    }

    private static String token(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return header.substring(7).trim();
        }
        return null;
    }
}
