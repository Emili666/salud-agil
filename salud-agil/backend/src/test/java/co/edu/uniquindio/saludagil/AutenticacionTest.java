package co.edu.uniquindio.saludagil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Inicio de sesión, registro y permisos por rol sobre los datos de demostración. */
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AutenticacionTest {

    private static final Pattern TOKEN = Pattern.compile("\"token\"\\s*:\\s*\"([^\"]+)\"");

    @Autowired WebApplicationContext contexto;
    MockMvc mvc;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.webAppContextSetup(contexto).build();
    }

    private String login(String usuario, String clave) throws Exception {
        MvcResult r = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\":\"" + usuario + "\",\"clave\":\"" + clave + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return extraerToken(r);
    }

    private static String extraerToken(MvcResult r) throws Exception {
        Matcher m = TOKEN.matcher(r.getResponse().getContentAsString());
        assertTrue(m.find(), "La respuesta debe traer un token");
        return m.group(1);
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }

    @Test
    void consultaDeMedicamentosEsPublicaPeroLoDemasPideSesion() throws Exception {
        mvc.perform(get("/api/medicamentos")).andExpect(status().isOk());
        mvc.perform(get("/api/puntos")).andExpect(status().isOk());
        mvc.perform(get("/api/afiliados/1094000001")).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.estado").value(401));
        mvc.perform(get("/api/indicadores")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/farmacia/puntos/1/inventario")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/indicadores").header("Authorization", "Bearer inventado"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void claveIncorrectaDevuelve401() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\":\"farmacia\",\"clave\":\"mala\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void cadaRolSoloEntraASuSeccion() throws Exception {
        String afiliado = login("1094000001", "salud123");
        String farmacia = login("farmacia", "farmacia123");
        String eps = login("eps", "eps123");

        mvc.perform(get("/api/afiliados/1094000001").header("Authorization", bearer(afiliado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documento").value("1094000001"));
        mvc.perform(get("/api/farmacia/puntos/1/inventario").header("Authorization", bearer(farmacia)))
                .andExpect(status().isOk());
        mvc.perform(get("/api/indicadores").header("Authorization", bearer(eps)))
                .andExpect(status().isOk());

        mvc.perform(get("/api/indicadores").header("Authorization", bearer(afiliado)))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/farmacia/puntos/1/inventario").header("Authorization", bearer(eps)))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/afiliados/1094000001").header("Authorization", bearer(farmacia)))
                .andExpect(status().isForbidden());
    }

    @Test
    void unAfiliadoNoVeLosDatosDeOtro() throws Exception {
        String maria = login("1094000001", "salud123");
        mvc.perform(get("/api/afiliados/1094000002").header("Authorization", bearer(maria)))
                .andExpect(status().isForbidden());
    }

    @Test
    void cerrarSesionInvalidaElToken() throws Exception {
        String token = login("eps", "eps123");
        mvc.perform(get("/api/auth/yo").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("EPS"));
        mvc.perform(post("/api/auth/logout").header("Authorization", bearer(token)))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/auth/yo").header("Authorization", bearer(token)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registroDejaElCorreoComoCanalDeAvisos() throws Exception {
        // 1094100000 es un afiliado histórico de la demo sin cuenta.
        MvcResult r = mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"documento\":\"1094100000\",\"correo\":\"Nuevo@Example.com\",\"clave\":\"secreta1\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol").value("AFILIADO"))
                .andReturn();
        String token = extraerToken(r);

        mvc.perform(get("/api/afiliados/1094100000").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canalPreferido").value("EMAIL"));

        // Ya puede entrar con su documento y la clave elegida; no se puede registrar dos veces.
        login("1094100000", "secreta1");
        mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"documento\":\"1094100000\",\"correo\":\"otro@example.com\",\"clave\":\"secreta1\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void registroRechazaDocumentosQueNoSonAfiliados() throws Exception {
        mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"documento\":\"99999999\",\"correo\":\"x@example.com\",\"clave\":\"secreta1\"}"))
                .andExpect(status().isConflict());
        mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"documento\":\"1094100001\",\"correo\":\"no-es-correo\",\"clave\":\"123\"}"))
                .andExpect(status().isBadRequest());
    }
}
