package co.edu.uniquindio.saludagil;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SaludAgilApplication {

    public static void main(String[] args) {
        SpringApplication.run(SaludAgilApplication.class, args);
    }
}
