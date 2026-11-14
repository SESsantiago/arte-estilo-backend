package com.cesde.arteestilo.config;

import com.cesde.arteestilo.model.*;
import com.cesde.arteestilo.repository.CategoriaRepository;
import com.cesde.arteestilo.repository.PedidoRepository;
import com.cesde.arteestilo.repository.ProductoRepository;
import com.cesde.arteestilo.repository.TransaccionPagoRepository;
import com.cesde.arteestilo.repository.UsuarioRepository;
import com.cesde.arteestilo.security.JwtTokenService;
import com.cesde.arteestilo.security.PasswordHasher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Configuration
public class DataSeederConfig {

    @Bean
    public CommandLineRunner initData(
            UsuarioRepository usuarioRepository,
            CategoriaRepository categoriaRepository,
            ProductoRepository productoRepository,
            PedidoRepository pedidoRepository,
            TransaccionPagoRepository transaccionRepository,
            JwtTokenService jwtTokenService) {

        return args -> {
            System.out.println("⏳ Verificando y sincronizando datos iniciales de Arte & Estilo...");

            // 1. Asegurar Categorías del Frontend
            Categoria sala = asegurarCategoria(categoriaRepository, "Sala", "Sofás, poltronas y muebles de sala principal");
            Categoria comedor = asegurarCategoria(categoriaRepository, "Comedor", "Juegos de comedor y mesas de reunión");
            Categoria dormitorio = asegurarCategoria(categoriaRepository, "Dormitorio", "Camas, cabeceros y mesas de noche");
            Categoria auxiliares = asegurarCategoria(categoriaRepository, "Auxiliares", "Mesas de centro, aparadores y complementos");
            Categoria sillas = asegurarCategoria(categoriaRepository, "Sillas", "Sillas de comedor, estudio y acento");
            Categoria mesas = asegurarCategoria(categoriaRepository, "Mesas", "Mesas de centro y auxiliares");

            // 2. Asegurar Usuarios (Administrador y Usuarios de prueba)
            Usuario admin = asegurarUsuario(
                    usuarioRepository,
                    "Administrador Arte & Estilo",
                    "admin@arteyestilo.co",
                    "Admin123!",
                    RolUsuario.ADMIN,
                    "300 900 1122",
                    "Medellín",
                    "Administrador general del catálogo y operaciones de Arte & Estilo",
                    "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200&h=200&fit=crop&auto=format"
            );

            Usuario maria = asegurarUsuario(
                    usuarioRepository,
                    "María Pérez",
                    "maria.perez@correo.com",
                    "Prueba123!",
                    RolUsuario.USUARIO,
                    "300 000 0000",
                    "Medellín",
                    "Apasionada por el diseño interior sereno y atemporal",
                    "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200&h=200&fit=crop&auto=format"
            );

            Usuario prueba = asegurarUsuario(
                    usuarioRepository,
                    "Usuario de Prueba",
                    "prueba@arteyestilo.co",
                    "Prueba123!",
                    RolUsuario.USUARIO,
                    "300 111 2233",
                    "Medellín",
                    "Cliente frecuente de muebles de autor",
                    "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200&h=200&fit=crop&auto=format"
            );

            asegurarUsuario(
                    usuarioRepository,
                    "Cliente Arte & Estilo",
                    "cliente@arteyestilo.co",
                    "Cliente123*",
                    RolUsuario.USUARIO,
                    "300 222 3344",
                    "Medellín",
                    "Cliente registrado en Arte & Estilo",
                    "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200&h=200&fit=crop&auto=format"
            );

            // Actualizar usuarios antiguos sin contraseña o rol
            usuarioRepository.findAll().forEach(u -> {
                boolean changed = false;
                if (u.getPasswordHash() == null || u.getPasswordHash().isBlank()) {
                    u.setPasswordHash(PasswordHasher.hash("Prueba123!"));
                    changed = true;
                }
                if (u.getRol() == null) {
                    u.setRol(u.getCorreo().equalsIgnoreCase("admin@arteyestilo.co") ? RolUsuario.ADMIN : RolUsuario.USUARIO);
                    changed = true;
                }
                if (u.getCiudad() == null || u.getCiudad().isBlank()) {
                    u.setCiudad("Medellín");
                    changed = true;
                }
                if (changed) {
                    usuarioRepository.save(u);
                }
            });

            // 3. Asegurar Productos reales correspondientes a las Cards del Frontend
            asegurarProducto(productoRepository,
                    "Sofá Lugano",
                    "Sofá de tres puestos tapizado en lino belga de alto tráfico con estructura en madera de roble inmunizado. Diseñado para brindar confort envolvente y presencia arquitectónica en la sala.",
                    new BigDecimal("3490000.00"),
                    null,
                    8,
                    "https://images.unsplash.com/photo-1555041469-a586c61ea9bc?w=800&h=600&fit=crop&auto=format",
                    "https://images.unsplash.com/photo-1555041469-a586c61ea9bc?w=1000&h=700&fit=crop&auto=format|https://images.unsplash.com/photo-1680503397667-3877494708a1?w=1000&h=700&fit=crop&auto=format|https://images.unsplash.com/photo-1493663284031-b7e3aefcae8e?w=1000&h=700&fit=crop&auto=format",
                    true,
                    "Destacado",
                    "Roble macizo + Lino belga",
                    "Contemporáneo / Sereno",
                    "No incluye almacenamiento",
                    "Sala principal · Loft · Apartamento",
                    "24 meses",
                    sala
            );

            asegurarProducto(productoRepository,
                    "Sofá Milano",
                    "Sofá modular italiano de líneas bajas y cojinería en espuma penta de alta resiliencia. Una pieza versátil que combina elegancia atemporal con calidez orgánica.",
                    new BigDecimal("4200000.00"),
                    new BigDecimal("3490000.00"),
                    6,
                    "https://images.unsplash.com/photo-1680503397667-3877494708a1?w=800&h=600&fit=crop&auto=format",
                    "https://images.unsplash.com/photo-1680503397667-3877494708a1?w=1000&h=700&fit=crop&auto=format|https://images.unsplash.com/photo-1555041469-a586c61ea9bc?w=1000&h=700&fit=crop&auto=format",
                    true,
                    "Oferta",
                    "Madera flormorado + Bouclé suave",
                    "Italiano Moderno",
                    "No incluye almacenamiento",
                    "Sala principal · Estudio amplio",
                    "24 meses",
                    sala
            );

            asegurarProducto(productoRepository,
                    "Cama Oslo",
                    "Cama doble con cabecero tapizado en tejido texturizado color avena y base en madera natural. Inspirada en el diseño escandinavo para dormitorios luminosos.",
                    new BigDecimal("2800000.00"),
                    new BigDecimal("2190000.00"),
                    5,
                    "https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?w=800&h=600&fit=crop&auto=format",
                    "https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?w=1000&h=700&fit=crop&auto=format|https://images.unsplash.com/photo-1540518614846-7ede433c4efb?w=1000&h=700&fit=crop&auto=format",
                    true,
                    "Oferta",
                    "Madera de haya + Lino tejido",
                    "Nórdico / Escandinavo",
                    "Opcional cajones inferiores",
                    "Habitación principal · Huéspedes",
                    "18 meses",
                    dormitorio
            );

            asegurarProducto(productoRepository,
                    "Sofá Módena 2P",
                    "Sofá de dos puestos con diseño contemporáneo, líneas limpias y cojines generosos. Su tela suave y patas en madera lo convierten en una pieza protagonista para salas serenas y elegantes.",
                    new BigDecimal("2950000.00"),
                    null,
                    7,
                    "https://images.unsplash.com/photo-1493663284031-b7e3aefcae8e?w=800&h=600&fit=crop&auto=format",
                    "https://images.unsplash.com/photo-1493663284031-b7e3aefcae8e?w=1000&h=700&fit=crop&auto=format|https://images.unsplash.com/photo-1680503397692-cd0da21a0bec?w=1000&h=700&fit=crop&auto=format",
                    true,
                    "Destacado",
                    "Madera + tela",
                    "Moderno / Contemporáneo",
                    "No incluye almacenamiento",
                    "Sala principal · Estudio · Apartamento",
                    "12 meses",
                    sala
            );

            asegurarProducto(productoRepository,
                    "Comedor Ratán 4P",
                    "Conjunto de comedor para 4 personas con mesa circular en madera maciza y sillas con respaldo en tejido de ratán natural hecho a mano.",
                    new BigDecimal("3200000.00"),
                    new BigDecimal("2790000.00"),
                    4,
                    "https://images.unsplash.com/photo-1547062200-f195b1c77e30?w=800&h=600&fit=crop&auto=format",
                    "https://images.unsplash.com/photo-1547062200-f195b1c77e30?w=1000&h=700&fit=crop&auto=format|https://images.unsplash.com/photo-1615066390971-03e4e1c36ddf?w=1000&h=700&fit=crop&auto=format",
                    true,
                    "Destacado",
                    "Roble + Ratán natural",
                    "Orgánico / Mediterráneo",
                    "No incluye almacenamiento",
                    "Comedor · Terraza cubierta",
                    "18 meses",
                    comedor
            );

            asegurarProducto(productoRepository,
                    "Mesa de Centro Río",
                    "Mesa de centro de silueta orgánica esculpida en madera de teca con acabado mate natural que resalta las vetas originales.",
                    new BigDecimal("1150000.00"),
                    new BigDecimal("890000.00"),
                    9,
                    "https://images.unsplash.com/photo-1600623050499-84929aad17c9?w=800&h=600&fit=crop&auto=format",
                    "https://images.unsplash.com/photo-1600623050499-84929aad17c9?w=1000&h=700&fit=crop&auto=format",
                    true,
                    "Oferta",
                    "Madera de Teca Maciza",
                    "Orgánico Contemporáneo",
                    "Bandeja inferior integrada",
                    "Sala de estar · Recepción",
                    "12 meses",
                    auxiliares
            );

            asegurarProducto(productoRepository,
                    "Silla Arce Tapizada",
                    "Silla de comedor y estudio con estructura ergonómica en madera de arce y asiento tapizado en chenilla color salvia.",
                    new BigDecimal("680000.00"),
                    null,
                    14,
                    "https://images.unsplash.com/photo-1506898667547-42e22a46e125?w=800&h=600&fit=crop&auto=format",
                    "https://images.unsplash.com/photo-1506898667547-42e22a46e125?w=1000&h=700&fit=crop&auto=format",
                    true,
                    "Nuevo",
                    "Madera de Arce + Chenilla",
                    "Mid-Century Modern",
                    "No aplica",
                    "Comedor · Escritorio · Lectura",
                    "12 meses",
                    sillas
            );

            asegurarProducto(productoRepository,
                    "Aparador Nórdico",
                    "Aparador de tres puertas y cajones ocultos con frentes alistonados en madera natural. Ideal para organizar vajilla y objetos de arte.",
                    new BigDecimal("2150000.00"),
                    new BigDecimal("1800000.00"),
                    6,
                    "https://images.unsplash.com/photo-1576249720336-35b043fce96d?w=800&h=600&fit=crop&auto=format",
                    "https://images.unsplash.com/photo-1576249720336-35b043fce96d?w=1000&h=700&fit=crop&auto=format",
                    true,
                    "Oferta",
                    "Cedro + Herrajes cierre lento",
                    "Nórdico / Minimalista",
                    "3 compartimentos internos amplios",
                    "Comedor · Hall de acceso · Sala",
                    "24 meses",
                    auxiliares
            );

            // Actualizar imágenes placeholder antiguas si existen
            productoRepository.findAll().forEach(p -> {
                boolean mod = false;
                if (p.getImagenUrl() != null && p.getImagenUrl().contains("ejemplo.com")) {
                    p.setImagenUrl("https://images.unsplash.com/photo-1555041469-a586c61ea9bc?w=800&h=600&fit=crop&auto=format");
                    mod = true;
                }
                if (p.getDescripcion() == null || p.getDescripcion().isBlank()) {
                    p.setDescripcion("Pieza de autor diseñada para transformar tu hogar con materiales nobles y acabados artesanales.");
                    mod = true;
                }
                if (p.getMaterial() == null) {
                    p.setMaterial("Madera noble seleccionada + Tapicería premium");
                    p.setEstilo("Contemporáneo / Atemporal");
                    p.setAlmacenamiento("Según configuración");
                    p.setIdealPara("Hogar · Sala · Estudio");
                    p.setGarantia("12 meses");
                    mod = true;
                }
                if (mod) {
                    productoRepository.save(p);
                }
            });

            // 4. Sembrar un pedido y transacción inicial si no existen pedidos
            if (pedidoRepository.count() == 0) {
                List<Producto> prods = productoRepository.findByEstadoActivoTrueOrderByIdAsc();
                if (!prods.isEmpty()) {
                    Producto p1 = prods.get(0);
                    Pedido ped = new Pedido();
                    ped.setUsuario(maria);
                    ped.setEstado(EstadoPedido.EN_CAMINO);
                    ped.setEstadoPago(EstadoPago.APROBADO);
                    ped.setMetodoPago("Visa •••• 4821");
                    ped.setReferenciaPago("AE-PAY-INIT-001");
                    ped.setDireccionEnvio(new DireccionEnvio(
                            "Cra. 43A #1 Sur-100, Apto 502", "Medellín", "Antioquia", "Torre Parque"));
                    ped.setTotalCompra(p1.getPrecio());

                    DetallePedido det = new DetallePedido();
                    det.setPedido(ped);
                    det.setProducto(p1);
                    det.setCantidad(1);
                    det.setPrecioUnitario(p1.getPrecio());
                    ped.getDetalles().add(det);

                    Pedido savedPed = pedidoRepository.save(ped);

                    TransaccionPago tx = new TransaccionPago();
                    tx.setPedido(savedPed);
                    tx.setReferencia("AE-PAY-INIT-001");
                    tx.setPasarela("Wompi-Colombia-Sandbox");
                    tx.setMetodoPago("CARD");
                    tx.setDetalleMetodo("Visa •••• 4821");
                    tx.setMonto(savedPed.getTotalCompra());
                    tx.setMoneda("COP");
                    tx.setEstado(EstadoPago.APROBADO);
                    tx.setCodigoAutorizacion("AUT-884210");
                    tx.setFirmaIntegridad(jwtTokenService.computePaymentSignature(
                            "AE-PAY-INIT-001", savedPed.getTotalCompra().toPlainString(), "COP"));
                    tx.setMensajeRespuesta("Transacción aprobada exitosamente");
                    tx.setFechaConfirmacion(LocalDateTime.now());
                    transaccionRepository.save(tx);
                }
            }

            System.out.println("✅ Datos iniciales de Arte & Estilo verificados correctamente.");
        };
    }

    private Categoria asegurarCategoria(CategoriaRepository repo, String nombre, String descripcion) {
        return repo.findByNombreIgnoreCase(nombre).orElseGet(() -> {
            Categoria c = Categoria.builder().nombre(nombre).descripcion(descripcion).build();
            c.setEstadoActivo(true);
            return repo.save(c);
        });
    }

    private Usuario asegurarUsuario(
            UsuarioRepository repo,
            String nombre,
            String correo,
            String password,
            RolUsuario rol,
            String telefono,
            String ciudad,
            String biografia,
            String fotoUrl) {
        return repo.findByCorreoIgnoreCase(correo).map(existente -> {
            existente.setRol(rol);
            if (existente.getPasswordHash() == null || existente.getPasswordHash().isBlank()) {
                existente.setPasswordHash(PasswordHasher.hash(password));
            }
            if (existente.getCiudad() == null) {
                existente.setCiudad(ciudad);
            }
            return repo.save(existente);
        }).orElseGet(() -> {
            PerfilUsuario perfil = new PerfilUsuario();
            perfil.setBiografia(biografia);
            perfil.setFotoPerfilUrl(fotoUrl);

            Usuario u = new Usuario();
            u.setNombre(nombre);
            u.setCorreo(correo);
            u.setPasswordHash(PasswordHasher.hash(password));
            u.setRol(rol);
            u.setTelefono(telefono);
            u.setCiudad(ciudad);
            u.setEstadoActivo(true);
            u.setPerfilUsuario(perfil);
            perfil.setUsuario(u);
            return repo.save(u);
        });
    }

    private void asegurarProducto(
            ProductoRepository repo,
            String nombre,
            String descripcion,
            BigDecimal precio,
            BigDecimal precioDescuento,
            Integer stock,
            String imagenUrl,
            String imagenesGaleria,
            Boolean destacado,
            String etiqueta,
            String material,
            String estilo,
            String almacenamiento,
            String idealPara,
            String garantia,
            Categoria categoria) {
        boolean existe = repo.findAll().stream()
                .anyMatch(p -> p.getNombre().equalsIgnoreCase(nombre));
        if (!existe) {
            Producto p = Producto.builder()
                    .nombre(nombre)
                    .descripcion(descripcion)
                    .precio(precio)
                    .precioDescuento(precioDescuento)
                    .stock(stock)
                    .imagenUrl(imagenUrl)
                    .imagenesGaleria(imagenesGaleria)
                    .destacado(destacado)
                    .etiqueta(etiqueta)
                    .material(material)
                    .estilo(estilo)
                    .almacenamiento(almacenamiento)
                    .idealPara(idealPara)
                    .garantia(garantia)
                    .categoria(categoria)
                    .build();
            p.setEstadoActivo(true);
            repo.save(p);
        }
    }
}
