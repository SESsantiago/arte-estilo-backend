# 🛋️ Arte & Estilo — Documentación Técnica Full Stack (Frontend + Backend)

## 1. Arquitectura General del Sistema

El ecosistema **Arte & Estilo** está dividido en dos repositorios independientes que se comunican mediante una API REST versionada (`/api/v1`) sobre JSON:

* **Backend (`arte-estilo`)**: Java 17 + Spring Boot + Spring Data JPA (Hibernate) + PostgreSQL (Neon Cloud) + Validación Jakarta + Seguridad PBKDF2/HMAC-SHA256 + Swagger/OpenAPI.
* **Frontend (`arte-estilo-frontend-2`)**: React 19 + Vite 8 + React Router DOM 7 + Tailwind CSS 4.

Ubicación en el espacio de trabajo:
* Backend: `C:\Users\SANTIAGO\.gemini\antigravity\scratch\arte-estilo-workspace\arte-estilo`
* Frontend: `C:\Users\SANTIAGO\.gemini\antigravity\scratch\arte-estilo-workspace\arte-estilo-frontend-2`

---

## 2. Modelos de Base de Datos y Relación con las Cards

### `Producto` (Tabla `productos`)
Contiene todos los atributos reales que alimentan las cards (`CardHome`, `CardHomeDiscount`, `CardCollections`) y la vista de detalle (`ProductDetail`):
* `id`, `nombre`, `descripcion`
* `precio` (precio base), `precioDescuento` (para `CardHomeDiscount`)
* `stock`, `estadoActivo` (permite activar/desactivar cards desde el administrador)
* `imagenUrl` (imagen principal de la card), `imagenesGaleria` (URLs adicionales separadas por `|` para `ProductGallery`)
* `destacado` (booleano para el carrusel de destacados con `CardHome`)
* `etiqueta` (badge: `"Destacado"`, `"Oferta"`, `"Nuevo"`)
* Características técnicas (`ProductTableGrid`): `material`, `estilo`, `almacenamiento`, `idealPara`, `garantia`
* Relación `@ManyToOne` con `Categoria`

### `Categoria` (Tabla `categorias`)
* `id`, `nombre`, `descripcion`, `estadoActivo`
* Relación `@OneToMany` con `Producto`

### `Usuario` y `PerfilUsuario` (Tablas `usuarios`, `perfiles_usuario`, `usuario_producto_favorito`)
* `id`, `nombre`, `correo`, `passwordHash` (PBKDF2WithHmacSHA256 con salt aleatorio), `rol` (`ADMIN` | `USUARIO`), `telefono`, `ciudad`, `estadoActivo`
* Relación `1:1` con `PerfilUsuario` (`biografia`, `fotoPerfilUrl`)
* Relación `M:N` con `Producto` (lista de favoritos del usuario)

### `Pedido`, `DetallePedido` y `TransaccionPago` (Tablas `pedidos`, `detalles_pedido`, `transacciones_pago`)
* `Pedido`: `totalCompra`, `estado` (`PENDIENTE`, `EN_CAMINO`, `ENTREGADO`, `CANCELADO`), `estadoPago` (`PENDIENTE`, `EN_PROCESO`, `APROBADO`, `RECHAZADO`, `CANCELADO`), `metodoPago`, `referenciaPago`, `@Embedded DireccionEnvio`.
* `TransaccionPago`: `referencia`, `pasarela` (`Wompi-Colombia-Sandbox`), `metodoPago`, `detalleMetodo`, `monto`, `moneda` (`COP`), `estado`, `codigoAutorizacion`, `firmaIntegridad` (HMAC-SHA256 calculado en servidor), `mensajeRespuesta`, `fechaConfirmacion`.

---

## 3. Endpoints Principales del Backend (`/api/v1`)

### Autenticación (`/api/v1/auth`)
* `POST /api/v1/auth/login` — Valida credenciales y retorna `{ token, usuario }`.
* `POST /api/v1/auth/registro` — Registra un nuevo usuario con rol `USUARIO` y retorna `{ token, usuario }`.
* `GET /api/v1/auth/me` — Valida el token Bearer y retorna el perfil actualizado.
* `POST /api/v1/auth/recuperar` — Procesa recuperación/restablecimiento de contraseña.

### Productos / Cards (`/api/v1/productos`)
* `GET /api/v1/productos` — Lista cards activas (filtros opcionales: `categoria`, `minStock`, `destacado`, `enOferta`).
* `GET /api/v1/productos/admin` — Lista todas las cards (activas e inactivas) para el módulo administrativo.
* `GET /api/v1/productos/{id}` — Obtiene el detalle completo de un producto.
* `POST /api/v1/productos` — Crea una nueva card.
* `PUT /api/v1/productos/{id}` — Actualiza todos los campos de una card.
* `PATCH /api/v1/productos/{id}` — Actualiza parcialmente una card.
* `PATCH /api/v1/productos/{id}/estado` — Activa o desactiva una card (`{ "estadoActivo": true/false }`).
* `DELETE /api/v1/productos/{id}` — Baja lógica de una card.

### Categorías (`/api/v1/categorias`)
* `GET /api/v1/categorias`, `POST /api/v1/categorias`, `PUT /api/v1/categorias/{id}`, `PATCH /api/v1/categorias/{id}`, `DELETE /api/v1/categorias/{id}`.

### Usuarios y Favoritos (`/api/v1/usuarios`)
* `GET /api/v1/usuarios`, `GET /api/v1/usuarios/{id}`, `POST /api/v1/usuarios`, `PUT /api/v1/usuarios/{id}`, `PATCH /api/v1/usuarios/{id}`, `DELETE /api/v1/usuarios/{id}`.
* `GET /api/v1/usuarios/{id}/favoritos` — Lista favoritos del usuario.
* `POST /api/v1/usuarios/{id}/favoritos/{productoId}` — Agrega/elimina producto de favoritos.

### Pedidos y Pagos (`/api/v1/pedidos` y `/api/v1/pagos`)
* `POST /api/v1/pedidos` — Valida stock, aplica descuento del 10% en compras mayores a \$2.000.000 COP y crea el pedido.
* `GET /api/v1/pedidos` — Lista pedidos (filtrable por `usuarioId` y/o `estado`).
* `PATCH /api/v1/pedidos/{id}/estado` — Actualiza estado de pedido y/o pago.
* `POST /api/v1/pedidos/{id}/cancelar` — Cancela un pedido pendiente y reintegra el stock.
* `POST /api/v1/pagos/iniciar` — Inicia el cobro en la pasarela, genera firma de integridad y evalúa el pago en el servidor.
* `GET /api/v1/pagos/verificar/{referencia}` — Verifica en el servidor el estado real de la transacción.
* `POST /api/v1/pagos/webhook` — Recibe confirmación de pasarela verificando `firmaIntegridad`.
* `GET /api/v1/pagos` — Lista todas las transacciones para el módulo administrativo.

---

## 4. Credenciales de Prueba Sembradas Automáticamente

| Rol | Correo | Contraseña | Acceso |
|---|---|---|---|
| **Administrador** | `admin@arteyestilo.co` | `Admin123!` | Acceso total a `/admin`, `/perfil`, gestión de cards, categorías, pagos y usuarios |
| **Usuario (Cliente)** | `maria.perez@correo.com` | `Prueba123!` | Acceso a `/perfil`, `/pedidos`, favoritos y compras |
| **Usuario (Cliente)** | `prueba@arteyestilo.co` | `Prueba123!` | Acceso a `/perfil`, `/pedidos`, favoritos y compras |

### Tarjetas de Prueba en el Checkout (Pasarela Sandbox)
* Terminada en **`4242`** (o cualquier otra numeración estándar): Pago **`APROBADO`**.
* Terminada en **`1111`**: Pago **`EN_PROCESO`** (validación antifraude).
* Terminada en **`0000`**: Pago **`RECHAZADO`** (cancela la orden y reintegra el stock automáticamente en el backend).

---

## 5. Variables de Entorno e Instrucciones de Ejecución

### Backend (`arte-estilo/.env`)
```properties
DB_URL=jdbc:postgresql://ep-misty-pond-b5s4rvix-pooler.c-7.us-east-2.aws.neon.tech:5432/neondb?sslmode=require
DB_USERNAME=neondb_owner
DB_PASSWORD=npg_HMmZfl3PTNw0
JWT_SECRET=arte-estilo-secret-key-2026-cesde-full-stack-security
PAYMENT_GATEWAY_NAME=Wompi-Colombia-Sandbox
CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:5174,http://127.0.0.1:5173
```

Para iniciar el backend:
```powershell
cd C:\Users\SANTIAGO\.gemini\antigravity\scratch\arte-estilo-workspace\arte-estilo
.\mvnw.cmd spring-boot:run
```
*(Swagger UI disponible en `http://localhost:8080/swagger-ui.html`)*

### Frontend (`arte-estilo-frontend-2/.env`)
```properties
VITE_API_URL=http://localhost:8080/api/v1
```

Para iniciar el frontend:
```powershell
cd C:\Users\SANTIAGO\.gemini\antigravity\scratch\arte-estilo-workspace\arte-estilo-frontend-2
npm install
npm run dev
```
*(Aplicación disponible en `http://localhost:5173`)*
