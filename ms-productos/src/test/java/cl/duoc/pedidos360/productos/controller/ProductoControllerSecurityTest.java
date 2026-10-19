package cl.duoc.pedidos360.productos.controller;

import cl.duoc.pedidos360.productos.config.JsonSecurityErrorHandler;
import cl.duoc.pedidos360.productos.config.SecurityConfig;
import cl.duoc.pedidos360.productos.service.ProductoService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Pruebas basicas de seguridad: 401 sin token, 403 sin rol/scope, 2xx con token valido. */
@WebMvcTest(controllers = ProductoController.class, properties = {
        "AZURE_ISSUER_URI=https://test.ciamlogin.com/tenant/v2.0",
        "AZURE_JWK_SET_URI=https://test.ciamlogin.com/tenant/discovery/v2.0/keys",
        "AZURE_API_CLIENT_ID=00000000-0000-0000-0000-000000000000"
})
@Import({SecurityConfig.class, JsonSecurityErrorHandler.class})
class ProductoControllerSecurityTest {

    private static final SimpleGrantedAuthority SCOPE = new SimpleGrantedAuthority("SCOPE_access_as_user");
    private static final SimpleGrantedAuthority ROL_ADMIN = new SimpleGrantedAuthority("ROLE_Admin");
    private static final SimpleGrantedAuthority ROL_CLIENTE = new SimpleGrantedAuthority("ROLE_Cliente");

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ProductoService service;

    @MockBean
    private JwtDecoder jwtDecoder;   // evita llamadas reales a Entra ID durante el test

    @Test
    void sinTokenResponde401() throws Exception {
        mvc.perform(get("/api/productos"))
           .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenSinScopeResponde403() throws Exception {
        mvc.perform(get("/api/productos").with(jwt()))
           .andExpect(status().isForbidden());
    }

    @Test
    void tokenValidoPuedeListar200() throws Exception {
        when(service.listar()).thenReturn(List.of());
        mvc.perform(get("/api/productos").with(jwt().authorities(SCOPE)))
           .andExpect(status().isOk());
    }

    @Test
    void rolClienteNoPuedeCrearProducto403() throws Exception {
        mvc.perform(post("/api/productos").with(jwt().authorities(SCOPE, ROL_CLIENTE))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Pizza\",\"precio\":9990,\"stock\":5}"))
           .andExpect(status().isForbidden());
    }

    @Test
    void rolAdminPuedeCrearProducto201() throws Exception {
        when(service.crear(any())).thenAnswer(inv -> inv.getArgument(0));
        mvc.perform(post("/api/productos").with(jwt().authorities(SCOPE, ROL_ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Pizza\",\"precio\":9990,\"stock\":5}"))
           .andExpect(status().isCreated());
    }

    @Test
    void clienteNoPuedeDescontarStock403() throws Exception {
        mvc.perform(patch("/api/productos/1/descontar-stock").param("cantidad", "1")
                .with(jwt().authorities(SCOPE, ROL_CLIENTE)))
           .andExpect(status().isForbidden());
    }

    @Test
    void precioNegativoResponde400() throws Exception {
        mvc.perform(post("/api/productos").with(jwt().authorities(SCOPE, ROL_ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Pizza\",\"precio\":-1,\"stock\":5}"))
           .andExpect(status().isBadRequest());
    }
}
