package com.cesde.arteestilo.config;

import com.cesde.arteestilo.model.Categoria;
import com.cesde.arteestilo.model.PerfilUsuario;
import com.cesde.arteestilo.model.Producto;
import com.cesde.arteestilo.model.Usuario;
import com.cesde.arteestilo.repository.CategoriaRepository;
import com.cesde.arteestilo.repository.ProductoRepository;
import com.cesde.arteestilo.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

/**
 * Siembra datos iniciales de forma idempotente: cada entidad se busca antes de crearla,
 * así que se puede arrancar la app cualquier número de veces sin duplicar ni romper.
 */
@Configuration
public class DataSeederConfig {

    private static final Logger log = LoggerFactory.getLogger(DataSeederConfig.class);

    @Bean
    public CommandLineRunner initData(UsuarioRepository usuarioRepository,
                                      CategoriaRepository categoriaRepository,
                                      ProductoRepository productoRepository) {
        return args -> {
            sembrarUsuario(usuarioRepository);

            Categoria sillas = obtenerOCrearCategoria(categoriaRepository, "Sillas", "Sillas de comedor y oficina");
            Categoria mesas = obtenerOCrearCategoria(categoriaRepository, "Mesas", "Mesas de centro y comedor");
            Categoria sofas = obtenerOCrearCategoria(categoriaRepository, "Sofás", "Sofás y sillones");

            if (productoRepository.count() > 0) {
                log.info("Los productos iniciales ya existen. Omitiendo siembra de productos.");
                return;
            }

            productoRepository.saveAll(List.of(
                    Producto.builder().nombre("Silla Nórdica de Roble")
                            .precio(new BigDecimal("350000.00")).stock(10)
                            .imagenUrl("https://ejemplo.com/silla.jpg").categoria(sillas).build(),
                    Producto.builder().nombre("Mesa de Centro en Vidrio")
                            .precio(new BigDecimal("1200000.00")).stock(5)
                            .imagenUrl("https://ejemplo.com/mesa.jpg").categoria(mesas).build(),
                    Producto.builder().nombre("Sofá de Lujo Italiano")
                            .precio(new BigDecimal("2500000.00")).stock(2)
                            .imagenUrl("https://ejemplo.com/sofa.jpg").categoria(sofas).build()
            ));
            log.info("Productos iniciales sembrados.");
        };
    }

    private void sembrarUsuario(UsuarioRepository usuarioRepository) {
        String correo = "santiago@cesde.net";
        if (usuarioRepository.findByCorreo(correo).isPresent()) {
            log.info("El usuario inicial ya existe. Omitiendo.");
            return;
        }
        PerfilUsuario perfil = new PerfilUsuario();
        perfil.setBiografia("Cliente frecuente de muebles de lujo");
        perfil.setFotoPerfilUrl("https://ejemplo.com/santiago.jpg");

        Usuario usuario = new Usuario();
        usuario.setNombre("Santiago Espinosa");
        usuario.setCorreo(correo);
        usuario.setTelefono("3001234567");
        usuario.setPerfilUsuario(perfil);
        perfil.setUsuario(usuario);
        usuarioRepository.save(usuario);
        log.info("Usuario inicial sembrado.");
    }

    private Categoria obtenerOCrearCategoria(CategoriaRepository repo, String nombre, String descripcion) {
        Categoria c = repo.findByNombre(nombre)
                .orElseGet(() -> Categoria.builder().nombre(nombre).descripcion(descripcion).build());
        c.setEstadoActivo(true);
        return repo.save(c);
    }
}
