package com.cesde.arteestilo.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "transacciones_pago")
public class TransaccionPago extends BaseEntity {

    @Column(nullable = false, unique = true, length = 60)
    private String referencia;

    @Column(nullable = false, length = 40)
    private String pasarela;

    @Column(name = "metodo_pago", nullable = false, length = 40)
    private String metodoPago;

    @Column(name = "detalle_metodo", length = 100)
    private String detalleMetodo;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Builder.Default
    @Column(nullable = false, length = 10)
    private String moneda = "COP";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPago estado;

    @Column(name = "codigo_autorizacion", length = 60)
    private String codigoAutorizacion;

    @Column(name = "firma_integridad", length = 128)
    private String firmaIntegridad;

    @Column(name = "mensaje_respuesta", length = 255)
    private String mensajeRespuesta;

    @Column(name = "fecha_confirmacion")
    private LocalDateTime fechaConfirmacion;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false, unique = true)
    private Pedido pedido;
}
