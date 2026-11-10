package co.edu.uniquindio.saludagil.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final SaludAgilProperties propiedades;

    public WebConfig(SaludAgilProperties propiedades) {
        this.propiedades = propiedades;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(propiedades.corsOrigenes().split(","))
                .allowedMethods("GET", "POST", "PUT", "DELETE");
    }
}
