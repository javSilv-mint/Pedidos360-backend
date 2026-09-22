package cl.duoc.pedidos360.pedidos.controller;

import cl.duoc.pedidos360.pedidos.config.JsonSecurityErrorHandler;
import cl.duoc.pedidos360.pedidos.config.SecurityConfig;
import cl.duoc.pedidos360.pedidos.service.PedidoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Pruebas basicas de seguridad: 401 sin token, 403 sin rol/scope, 2xx con token valido. */
@WebMvcTest(controllers = PedidoController.class, properties = {
        "AZURE_ISSUER_URI=https://test.ciamlogin.com/tenant/v2.0",
        "AZURE_JWK_SET_URI=https://test.ciamlogin.com/tenant/discovery/v2.0/keys",
        "AZURE_API_CLIENT_ID=00000000-0000-0000-0000-000000000000"
})
@Import({SecurityConfig.class, JsonSecurityErrorHandler.class})
class PedidoControllerSecurityTest {

    private static final SimpleGrantedAuthority SCOPE = new SimpleGrantedAuthority("SCOPE_access_as_user");
    private static final SimpleGrantedAuthority ROL_ADMIN = new SimpleGrantedAuthority("ROLE_Admin");
    private static final SimpleGrantedAuthority ROL_CLIENTE = new SimpleGrantedAuthority("ROLE_Cliente");

    @Autowired
    private MockMvc mvc;

    @MockBean
    private PedidoService service;

    @MockBean
    private JwtDecoder jwtDecoder;   // evita llamadas reales a Entra ID durante el test

    @Test
    void sinTokenResponde401() throws Exception {
        mvc.perform(get("/api/pedidos/mis-pedidos"))
           .andExpect(status().isUnauthorized());
    }

    @Test
    void clientePuedeVerSusPedidos200() throws Exception {
        when(service.listarPorEmail("cliente@test.cl")).thenReturn(List.of());
        mvc.perform(get("/api/pedidos/mis-pedidos")
                .with(jwt().jwt(j -> j.claim("email", "cliente@test.cl")).authorities(SCOPE, ROL_CLIENTE)))
           .andExpect(status().isOk());
    }

    @Test
    void clienteNoPuedeListarTodos403() throws Exception {
        mvc.perform(get("/api/pedidos").with(jwt().authorities(SCOPE, ROL_CLIENTE)))
           .andExpect(status().isForbidden());
    }

    @Test
    void adminPuedeListarTodos200() throws Exception {
        when(service.listar()).thenReturn(List.of());
        mvc.perform(get("/api/pedidos").with(jwt().authorities(SCOPE, ROL_ADMIN)))
           .andExpect(status().isOk());
    }
}
