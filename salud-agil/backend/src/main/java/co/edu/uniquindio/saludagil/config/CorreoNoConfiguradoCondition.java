package co.edu.uniquindio.saludagil.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class CorreoNoConfiguradoCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return !new CorreoBrevoConfiguradoCondition().matches(context, metadata);
    }
}
