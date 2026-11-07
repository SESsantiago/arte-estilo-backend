package com.cesde.arteestilo.controller;

import com.cesde.arteestilo.dto.PagoIniciarRequest;
import com.cesde.arteestilo.dto.PagoWebhookRequest;
import com.cesde.arteestilo.dto.TransaccionPagoResponse;
import com.cesde.arteestilo.model.EstadoPago;
import com.cesde.arteestilo.service.PagoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pagos")
@Tag(name = "Pagos", description = "Procesamiento, verificación y webhooks de transacciones de pago")
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    @PostMapping("/iniciar")
    @Operation(summary = "Iniciar y procesar el pago de un pedido en la pasarela")
    public ResponseEntity<TransaccionPagoResponse> iniciarPago(@Valid @RequestBody PagoIniciarRequest dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pagoService.iniciarPago(dto));
    }

    @PostMapping("/webhook")
    @Operation(summary = "Recibir notificación / webhook de confirmación de pago y verificar firma")
    public ResponseEntity<TransaccionPagoResponse> webhook(@Valid @RequestBody PagoWebhookRequest dto) {
        return ResponseEntity.ok(pagoService.procesarWebhook(dto));
    }

    @GetMapping("/verificar/{referencia}")
    @Operation(summary = "Verificar el estado real de una transacción por su referencia")
    public ResponseEntity<TransaccionPagoResponse> verificar(@PathVariable String referencia) {
        return ResponseEntity.ok(pagoService.verificarPorReferencia(referencia));
    }

    @GetMapping("/pedido/{pedidoId}")
    @Operation(summary = "Consultar la transacción de pago asociada a un pedido")
    public ResponseEntity<TransaccionPagoResponse> porPedido(@PathVariable Long pedidoId) {
        return ResponseEntity.ok(pagoService.obtenerPorPedido(pedidoId));
    }

    @GetMapping
    @Operation(summary = "Listar todas las transacciones de pago (Módulo Administrativo)")
    public ResponseEntity<List<TransaccionPagoResponse>> listar(
            @RequestParam(required = false) EstadoPago estado) {
        return ResponseEntity.ok(pagoService.listarTodas(estado));
    }
}
