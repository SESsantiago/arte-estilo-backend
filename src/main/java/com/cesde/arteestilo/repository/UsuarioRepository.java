package com.cesde.arteestilo.repository;

import com.cesde.arteestilo.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByCorreo(String correo);

    Optional<Usuario> findByCorreoIgnoreCase(String correo);

    List<Usuario> findByEstadoActivoTrue();

    List<Usuario> findAllByOrderByIdAsc();
}
