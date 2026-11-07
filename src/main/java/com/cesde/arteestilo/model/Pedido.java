package com.cesde.arteestilo.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "pedidos")
public class Pedido extends BaseEntity {

    @Column(name = "total_compra", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalCompra;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPedido estado;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_pago", nullable = false, length = 20, columnDefinition = "varchar(20) default 'PENDIENTE'")
    private EstadoPago estadoPago = EstadoPago.PENDIENTE;

    @Column(name = "metodo_pago", length = 60)
    private String metodoPago;

    @Column(name = "referencia_pago", length = 60)
    private String referenciaPago;

    /** Componente embebido: las columnas de DireccionEnvio viven en esta tabla. */
    @Embedded
    private DireccionEnvio direccionEnvio;

    /** Muchos pedidos pertenecen a un usuario. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    /** Un pedido contiene muchos detalles (líneas). Cascade ALL: los detalles se persisten con el pedido. */
    @Builder.Default
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetallePedido> detalles = new ArrayList<>();

    @OneToOne(mappedBy = "pedido", cascade = CascadeType.ALL)
    private TransaccionPago transaccionPago;
}
