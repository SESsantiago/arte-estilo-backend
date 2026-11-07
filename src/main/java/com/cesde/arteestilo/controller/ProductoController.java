package com.cesde.arteestilo.controller;

import com.cesde.arteestilo.dto.ProductoPatch;
import com.cesde.arteestilo.dto.ProductoRequest;
import com.cesde.arteestilo.dto.ProductoResponse;
import com.cesde.arteestilo.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/productos")
@Tag(name = "Productos", description = "Catálogo y administración dinámica de cards de productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    @Operation(summary = "Listar productos activos",
            description = "Filtros opcionales: categoria, minStock, destacado, enOferta")
    public ResponseEntity<List<ProductoResponse>> listar(
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) Integer minStock,
            @RequestParam(required = false) Boolean destacado,
            @RequestParam(required = false) Boolean enOferta) {
        return ResponseEntity.ok(productoService.listar(categoria, minStock, destacado, enOferta));
    }

    @GetMapping("/admin")
    @Operation(summary = "[Admin] Listar todos los productos (activos e inactivos)")
    public ResponseEntity<List<ProductoResponse>> listarTodosAdmin() {
        return ResponseEntity.ok(productoService.listarTodosAdmin());
    }

    @GetMapping("/bajo-stock")
    @Operation(summary = "[Personalizado] Productos con poco inventario",
            description = "Devuelve los productos activos con stock menor o igual a 'limite', del más escaso al menos escaso")
    public ResponseEntity<List<ProductoResponse>> bajoStock(
            @RequestParam(defaultValue = "5") Integer limite) {
        return ResponseEntity.ok(productoService.listarBajoStock(limite));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un producto por id")
    public ResponseEntity<ProductoResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.obtenerCualquiera(id));
    }

    @PostMapping
    @Operation(summary = "Crear un producto (card)")
    public ResponseEntity<ProductoResponse> crear(@Valid @RequestBody ProductoRequest dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productoService.crear(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Reemplazar un producto completo")
    public ResponseEntity<ProductoResponse> reemplazar(@PathVariable Long id,
                                                       @Valid @RequestBody ProductoRequest dto) {
        return ResponseEntity.ok(productoService.reemplazar(id, dto));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Actualizar parcialmente un producto")
    public ResponseEntity<ProductoResponse> actualizarParcial(@PathVariable Long id,
                                                              @Valid @RequestBody ProductoPatch dto) {
        return ResponseEntity.ok(productoService.actualizarParcial(id, dto));
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Activar o desactivar una card de producto")
    public ResponseEntity<ProductoResponse> cambiarEstado(@PathVariable Long id,
                                                          @RequestBody Map<String, Boolean> body) {
        boolean activo = body.getOrDefault("estadoActivo", true);
        return ResponseEntity.ok(productoService.cambiarEstadoActivo(id, activo));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar (baja lógica) un producto")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
