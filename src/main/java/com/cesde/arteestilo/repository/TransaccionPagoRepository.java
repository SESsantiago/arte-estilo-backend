package com.cesde.arteestilo.repository;

import com.cesde.arteestilo.model.EstadoPago;
import com.cesde.arteestilo.model.TransaccionPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransaccionPagoRepository extends JpaRepository<TransaccionPago, Long> {

    Optional<TransaccionPago> findByReferencia(String referencia);

    Optional<TransaccionPago> findByPedidoId(Long pedidoId);

    List<TransaccionPago> findByEstadoOrderByFechaCreacionDesc(EstadoPago estado);

    List<TransaccionPago> findAllByOrderByFechaCreacionDesc();
}
