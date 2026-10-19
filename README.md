# Pedidos360 — Backend (DSY1107 · Desarrollo Cloud Native I)

Tres microservicios Spring Boot 3.3 / Java 17 / MySQL que actúan como **OAuth2 Resource Server**:
cada request debe traer un **access token** emitido por **Microsoft Entra External ID**.

| Microservicio | Puerto | Base de datos | Endpoints |
|---|---|---|---|
| `ms-productos` | 8081 | `pedidos360_productos` | `/api/productos` |
| `ms-pedidos`   | 8082 | `pedidos360_pedidos`   | `/api/pedidos` |
| `ms-clientes`  | 8083 | `pedidos360_clientes`  | `/api/clientes` |

## Seguridad

- `spring-boot-starter-oauth2-resource-server` valida **firma (JWKS), issuer, audience y expiración**.
- `config/SecurityConfig`: sesión stateless, CORS configurable, `scp` → `SCOPE_*`, `roles` → `ROLE_*`.
- Todas las rutas exigen el scope `access_as_user`; las operaciones administrativas exigen además el rol `Admin`.
- `config/JsonSecurityErrorHandler`: respuestas JSON **401** (sin token / token inválido) y **403** (sin rol o scope).
- `/api/pedidos/mis-pedidos` y `/api/clientes/mi-perfil` toman el email **del token**, no de un parámetro.

| Ruta | Acceso |
|---|---|
| `GET /api/productos`, `GET /api/productos/{id}` | autenticado |
| `POST/PUT/DELETE/PATCH /api/productos/**` | Admin |
| `POST /api/pedidos`, `GET /api/pedidos/mis-pedidos`, `GET /api/pedidos/{id}`, `PATCH /api/pedidos/{id}/cancelar` | autenticado |
| `GET /api/pedidos`, `GET /api/pedidos/por-email`, `PATCH /api/pedidos/{id}/estado`, `DELETE /api/pedidos/{id}` | Admin |
| `POST /api/clientes`, `GET /api/clientes/mi-perfil` | autenticado |
| resto de `/api/clientes/**` | Admin |

## Variables de entorno

| Variable | Valor |
|---|---|
| `AZURE_ISSUER_URI` | campo `issuer` de `https://<SUBDOMINIO>.ciamlogin.com/<TENANT_ID>/v2.0/.well-known/openid-configuration` |
| `AZURE_JWK_SET_URI` | campo `jwks_uri` del mismo documento |
| `AZURE_API_CLIENT_ID` | Application (client) ID del registro del API |
| `AZURE_API_SCOPE` | opcional, por defecto `access_as_user` |
| `CORS_ORIGINS` | opcional, por defecto `http://localhost:4200` |
| `DB_URL`, `DB_USER`, `DB_PASS` | conexión MySQL / RDS |

## Ejecutar y probar

```bash
export AZURE_ISSUER_URI=... AZURE_JWK_SET_URI=... AZURE_API_CLIENT_ID=...
cd ms-productos && mvn clean verify     # tests de seguridad (401 / 403 / 2xx)
mvn spring-boot:run
curl -i http://localhost:8081/api/productos   # -> 401 sin token
```

## Despliegue en EC2

`deploy/instalar-ec2.sh` instala los tres JAR como servicios **systemd** (Amazon Linux 2023)
y guarda las variables en `/etc/pedidos360/comun.env` con permisos 600.
