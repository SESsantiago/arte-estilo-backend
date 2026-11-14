package com.cesde.arteestilo.controller;

import com.cesde.arteestilo.dto.CategoriaResponse;
import com.cesde.arteestilo.dto.PedidoResponse;
import com.cesde.arteestilo.dto.ProductoResponse;
import com.cesde.arteestilo.dto.TransaccionPagoResponse;
import com.cesde.arteestilo.dto.UsuarioResponse;
import com.cesde.arteestilo.service.CategoriaService;
import com.cesde.arteestilo.service.PagoService;
import com.cesde.arteestilo.service.PedidoService;
import com.cesde.arteestilo.service.ProductoService;
import com.cesde.arteestilo.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/analitica")
@Tag(name = "Analítica", description = "Endpoint consolidado para el Tablero Analítico (Dashboard)")
public class AnaliticaController {

    private final ProductoService productoService;
    private final CategoriaService categoriaService;
    private final PedidoService pedidoService;
    private final PagoService pagoService;
    private final UsuarioService usuarioService;

    public AnaliticaController(ProductoService productoService,
                               CategoriaService categoriaService,
                               PedidoService pedidoService,
                               PagoService pagoService,
                               UsuarioService usuarioService) {
        this.productoService = productoService;
        this.categoriaService = categoriaService;
        this.pedidoService = pedidoService;
        this.pagoService = pagoService;
        this.usuarioService = usuarioService;
    }

    @GetMapping("/reporte")
    @Operation(summary = "Obtener reporte analítico consolidado en tiempo real",
            description = "Retorna productos, categorías, pedidos, transacciones de pago y usuarios para el Dashboard Analítico")
    public ResponseEntity<Map<String, Object>> obtenerReporte() {
        List<ProductoResponse> productos = productoService.listarTodosAdmin();
        List<CategoriaResponse> categorias = categoriaService.listar();
        List<PedidoResponse> pedidos = pedidoService.listar(null, null);
        List<TransaccionPagoResponse> pagos = pagoService.listarTodas(null);
        List<UsuarioResponse> usuarios = usuarioService.listar();

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("timestamp", LocalDateTime.now().toString());
        payload.put("productos", productos);
        payload.put("categorias", categorias);
        payload.put("pedidos", pedidos);
        payload.put("pagos", pagos);
        payload.put("usuarios", usuarios);
        return ResponseEntity.ok(payload);
    }
}
