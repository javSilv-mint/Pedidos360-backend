package cl.duoc.pedidos360.clientes.controller;

import cl.duoc.pedidos360.clientes.config.JsonSecurityErrorHandler;
import cl.duoc.pedidos360.clientes.config.SecurityConfig;
import cl.duoc.pedidos360.clientes.service.ClienteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import cl.duoc.pedidos360.clientes.dto.ClienteRequest;
import cl.duoc.pedidos360.clientes.model.Cliente;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Pruebas basicas de seguridad: 401 sin token, 403 sin rol/scope, 2xx con token valido. */
@WebMvcTest(controllers = ClienteController.class, properties = {
        "AZURE_ISSUER_URI=https://test.ciamlogin.com/tenant/v2.0",
        "AZURE_JWK_SET_URI=https://test.ciamlogin.com/tenant/discovery/v2.0/keys",
        "AZURE_API_CLIENT_ID=00000000-0000-0000-0000-000000000000"
})
@Import({SecurityConfig.class, JsonSecurityErrorHandler.class})
class ClienteControllerSecurityTest {

    private static final SimpleGrantedAuthority SCOPE = new SimpleGrantedAuthority("SCOPE_access_as_user");
    private static final SimpleGrantedAuthority ROL_ADMIN = new SimpleGrantedAuthority("ROLE_Admin");
    private static final SimpleGrantedAuthority ROL_CLIENTE = new SimpleGrantedAuthority("ROLE_Cliente");

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ClienteService service;

    @MockBean
    private JwtDecoder jwtDecoder;   // evita llamadas reales a Entra ID durante el test

    @Test
    void sinTokenResponde401() throws Exception {
        mvc.perform(get("/api/clientes"))
           .andExpect(status().isUnauthorized());
    }

    @Test
    void clienteNoPuedeListarClientes403() throws Exception {
        mvc.perform(get("/api/clientes").with(jwt().authorities(SCOPE, ROL_CLIENTE)))
           .andExpect(status().isForbidden());
    }

    @Test
    void adminPuedeListarClientes200() throws Exception {
        when(service.listar()).thenReturn(List.of());
        mvc.perform(get("/api/clientes").with(jwt().authorities(SCOPE, ROL_ADMIN)))
           .andExpect(status().isOk());
    }

    private static final String CUERPO = """
        {"rut":"12345678-9","nombre":"Ana","apellido":"Perez","email":"victima@test.cl"}
        """;

    @Test
    void usuarioNormalSoloPuedeRegistrarseConSuPropioEmail() throws Exception {
        when(service.crear(any())).thenReturn(new Cliente());
        mvc.perform(post("/api/clientes")
                .with(jwt().jwt(j -> j.claim("email", "real@test.cl")).authorities(SCOPE, ROL_CLIENTE))
                .contentType(MediaType.APPLICATION_JSON).content(CUERPO))
           .andExpect(status().isCreated());
        ArgumentCaptor<ClienteRequest> captor = ArgumentCaptor.forClass(ClienteRequest.class);
        verify(service).crear(captor.capture());
        assertThat(captor.getValue().email()).isEqualTo("real@test.cl");
    }

    @Test
    void rutInvalidoResponde400() throws Exception {
        mvc.perform(post("/api/clientes")
                .with(jwt().jwt(j -> j.claim("email", "real@test.cl")).authorities(SCOPE, ROL_CLIENTE))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"rut\":\"abc\",\"nombre\":\"Ana\",\"apellido\":\"Perez\",\"email\":\"a@test.cl\"}"))
           .andExpect(status().isBadRequest());
    }

    @Test
    void clienteNoPuedeVerOtroClientePorId403() throws Exception {
        mvc.perform(get("/api/clientes/5").with(jwt().authorities(SCOPE, ROL_CLIENTE)))
           .andExpect(status().isForbidden());
    }
}
