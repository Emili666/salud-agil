package co.edu.uniquindio.saludagil.config;

import co.edu.uniquindio.saludagil.web.AutenticacionInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final SaludAgilProperties propiedades;
    private final AutenticacionInterceptor autenticacion;

    public WebConfig(SaludAgilProperties propiedades, AutenticacionInterceptor autenticacion) {
        this.propiedades = propiedades;
        this.autenticacion = autenticacion;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(autenticacion).addPathPatterns("/api/**");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(propiedades.corsOrigenes().split(","))
                .allowedMethods("GET", "POST", "PUT", "DELETE");
    }
}
