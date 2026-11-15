package co.edu.uniquindio.saludagil.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.core.env.Environment;

public class CorreoBrevoConfiguradoCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Environment environment = context.getEnvironment();
        return Boolean.parseBoolean(environment.getProperty("saludagil.correo.habilitado", "false"))
                && tieneTexto(environment.getProperty("saludagil.correo.brevo-api-key"))
                && tieneTexto(environment.getProperty("saludagil.correo.remitente"));
    }

    private static boolean tieneTexto(String valor) {
        return valor != null && !valor.isBlank();
    }
}
