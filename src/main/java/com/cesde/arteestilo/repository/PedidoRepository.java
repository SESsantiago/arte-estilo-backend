package com.cesde.arteestilo.repository;

import com.cesde.arteestilo.model.EstadoPedido;
import com.cesde.arteestilo.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    /** Query Method derivado: pedidos de un usuario filtrados por estado. */
    List<Pedido> findByUsuarioIdAndEstado(Long usuarioId, EstadoPedido estado);

    List<Pedido> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);

    List<Pedido> findByEstadoOrderByFechaCreacionDesc(EstadoPedido estado);

    List<Pedido> findAllByOrderByFechaCreacionDesc();
}
