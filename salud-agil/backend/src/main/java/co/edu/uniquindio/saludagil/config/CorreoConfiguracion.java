package co.edu.uniquindio.saludagil.config;

import co.edu.uniquindio.saludagil.service.EnviadorCorreoBrevo;
import co.edu.uniquindio.saludagil.service.EnviadorNotificaciones;
import co.edu.uniquindio.saludagil.service.EnviadorSimulado;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Conditional;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
@EnableAsync
public class CorreoConfiguracion {

    private static final Logger log = LoggerFactory.getLogger(CorreoConfiguracion.class);

    @Bean
    @Conditional(CorreoBrevoConfiguradoCondition.class)
    EnviadorNotificaciones enviadorCorreoBrevo(CorreoProperties properties, RestClient clienteBrevo) {
        return new EnviadorCorreoBrevo(properties, new EnviadorSimulado(), clienteBrevo);
    }

    @Bean
    RestClient clienteBrevo(RestClient.Builder builder) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(10));
        return builder.requestFactory(requestFactory).build();
    }

    @Bean(name = "correoTaskExecutor")
    ThreadPoolTaskExecutor correoTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("correo-");
        executor.setRejectedExecutionHandler((tarea, pool) ->
                log.warn("No se envió un correo: la cola de notificaciones está llena"));
        return executor;
    }

    @Bean
    @ConditionalOnProperty(name = "saludagil.correo.habilitado", havingValue = "true")
    ApplicationRunner advertirCorreoSinConfiguracion(CorreoProperties properties) {
        return args -> {
            if (!properties.brevoConfigurado()) {
                log.warn("Correo habilitado pero falta configuración (BREVO_API_KEY o CORREO_REMITENTE); se usarán notificaciones simuladas");
            }
        };
    }
}
