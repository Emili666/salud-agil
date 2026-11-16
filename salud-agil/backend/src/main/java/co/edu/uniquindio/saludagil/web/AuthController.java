package co.edu.uniquindio.saludagil.web;

import co.edu.uniquindio.saludagil.dto.LoginRequest;
import co.edu.uniquindio.saludagil.dto.RegistroRequest;
import co.edu.uniquindio.saludagil.dto.SesionDto;
import co.edu.uniquindio.saludagil.service.AuthService;
import co.edu.uniquindio.saludagil.service.Sesion;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public SesionDto login(@Valid @RequestBody LoginRequest solicitud) {
        return authService.iniciarSesion(solicitud);
    }

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public SesionDto registro(@Valid @RequestBody RegistroRequest solicitud) {
        return authService.registrarAfiliado(solicitud);
    }

    /** Datos de la sesión actual (sirve para validar un token guardado en el navegador). */
    @GetMapping("/yo")
    public SesionDto yo(@RequestAttribute(AutenticacionInterceptor.SESION) Sesion sesion) {
        return new SesionDto(sesion.token(), sesion.nombre(), sesion.rol(), sesion.documento());
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@RequestAttribute(AutenticacionInterceptor.SESION) Sesion sesion) {
        authService.cerrarSesion(sesion.token());
    }
}
