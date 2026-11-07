package com.cesde.arteestilo.service;

import com.cesde.arteestilo.dto.ProductoPatch;
import com.cesde.arteestilo.dto.ProductoRequest;
import com.cesde.arteestilo.dto.ProductoResponse;
import com.cesde.arteestilo.exception.RecursoNoEncontradoException;
import com.cesde.arteestilo.model.Categoria;
import com.cesde.arteestilo.model.Producto;
import com.cesde.arteestilo.repository.CategoriaRepository;
import com.cesde.arteestilo.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    public ProductoService(ProductoRepository productoRepository,
                           CategoriaRepository categoriaRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> listar(String categoria, Integer minStock, Boolean destacado, Boolean enOferta) {
        List<Producto> productos;
        if (Boolean.TRUE.equals(destacado)) {
            productos = productoRepository.findByDestacadoTrueAndEstadoActivoTrueOrderByIdAsc();
        } else if (Boolean.TRUE.equals(enOferta)) {
            productos = productoRepository.findProductosConDescuentoActivos();
        } else if (categoria != null && !categoria.isBlank() && !categoria.equalsIgnoreCase("Todos")) {
            productos = productoRepository.findByCategoriaNombreIgnoreCaseAndEstadoActivoTrue(categoria.trim());
        } else if (minStock != null) {
            productos = productoRepository.findProductosEnOfertaConStockSup(minStock);
        } else {
            productos = productoRepository.findByEstadoActivoTrueOrderByIdAsc();
        }
        return productos.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> listarTodosAdmin() {
        return productoRepository.findAllByOrderByIdAsc().stream().map(this::toResponse).toList();
    }

    /** ENDPOINT PERSONALIZADO 2: productos activos con stock menor o igual al límite. */
    @Transactional(readOnly = true)
    public List<ProductoResponse> listarBajoStock(Integer limite) {
        if (limite == null || limite < 0) {
            throw new IllegalArgumentException("El límite debe ser un número mayor o igual a cero");
        }
        return productoRepository.findByStockLessThanEqualAndEstadoActivoTrueOrderByStockAsc(limite)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ProductoResponse obtener(Long id) {
        return toResponse(buscarActivo(id));
    }

    @Transactional(readOnly = true)
    public ProductoResponse obtenerCualquiera(Long id) {
        return toResponse(buscarCualquiera(id));
    }

    @Transactional
    public ProductoResponse crear(ProductoRequest dto) {
        Producto p = new Producto();
        aplicarRequest(p, dto);
        return toResponse(productoRepository.save(p));
    }

    /** PUT: reemplaza todos los campos editables. */
    @Transactional
    public ProductoResponse reemplazar(Long id, ProductoRequest dto) {
        Producto p = buscarCualquiera(id);
        aplicarRequest(p, dto);
        return toResponse(productoRepository.save(p));
    }

    /** PATCH: cambia solo los campos que llegan en el JSON. */
    @Transactional
    public ProductoResponse actualizarParcial(Long id, ProductoPatch dto) {
        Producto p = buscarCualquiera(id);
        if (dto.nombre() != null) p.setNombre(dto.nombre().trim());
        if (dto.descripcion() != null) p.setDescripcion(dto.descripcion().trim());
        if (dto.precio() != null) p.setPrecio(dto.precio());
        if (dto.precioDescuento() != null) {
            p.setPrecioDescuento(dto.precioDescuento().signum() > 0 ? dto.precioDescuento() : null);
        }
        if (dto.stock() != null) p.setStock(dto.stock());
        if (dto.imagenUrl() != null) p.setImagenUrl(dto.imagenUrl().trim());
        if (dto.imagenesGaleria() != null) p.setImagenesGaleria(serializarGaleria(dto.imagenesGaleria()));
        if (dto.destacado() != null) p.setDestacado(dto.destacado());
        if (dto.etiqueta() != null) p.setEtiqueta(dto.etiqueta().isBlank() ? null : dto.etiqueta().trim());
        if (dto.material() != null) p.setMaterial(dto.material().trim());
        if (dto.estilo() != null) p.setEstilo(dto.estilo().trim());
        if (dto.almacenamiento() != null) p.setAlmacenamiento(dto.almacenamiento().trim());
        if (dto.idealPara() != null) p.setIdealPara(dto.idealPara().trim());
        if (dto.garantia() != null) p.setGarantia(dto.garantia().trim());
        if (dto.estadoActivo() != null) p.setEstadoActivo(dto.estadoActivo());
        if (dto.categoriaId() != null) p.setCategoria(buscarCategoria(dto.categoriaId()));
        return toResponse(productoRepository.save(p));
    }

    @Transactional
    public ProductoResponse cambiarEstadoActivo(Long id, boolean activo) {
        Producto p = buscarCualquiera(id);
        p.setEstadoActivo(activo);
        return toResponse(productoRepository.save(p));
    }

    /** DELETE lógico: marca estadoActivo=false (los pedidos antiguos conservan su referencia). */
    @Transactional
    public void eliminar(Long id) {
        Producto p = buscarCualquiera(id);
        p.setEstadoActivo(false);
        productoRepository.save(p);
    }

    private void aplicarRequest(Producto p, ProductoRequest dto) {
        p.setNombre(dto.nombre().trim());
        p.setDescripcion(dto.descripcion() != null ? dto.descripcion().trim() : null);
        p.setPrecio(dto.precio());
        p.setPrecioDescuento(dto.precioDescuento() != null && dto.precioDescuento().signum() > 0
                ? dto.precioDescuento() : null);
        p.setStock(dto.stock());
        p.setImagenUrl(dto.imagenUrl() != null ? dto.imagenUrl().trim() : null);
        p.setImagenesGaleria(serializarGaleria(dto.imagenesGaleria()));
        p.setDestacado(Boolean.TRUE.equals(dto.destacado()));
        p.setEtiqueta(dto.etiqueta() != null && !dto.etiqueta().isBlank() ? dto.etiqueta().trim() : null);
        p.setMaterial(dto.material());
        p.setEstilo(dto.estilo());
        p.setAlmacenamiento(dto.almacenamiento());
        p.setIdealPara(dto.idealPara());
        p.setGarantia(dto.garantia());
        if (dto.estadoActivo() != null) {
            p.setEstadoActivo(dto.estadoActivo());
        } else if (p.getEstadoActivo() == null) {
            p.setEstadoActivo(true);
        }
        p.setCategoria(buscarCategoria(dto.categoriaId()));
    }

    private String serializarGaleria(List<String> urls) {
        if (urls == null || urls.isEmpty()) return null;
        String joined = urls.stream()
                .filter(u -> u != null && !u.isBlank())
                .map(String::trim)
                .collect(Collectors.joining("|"));
        return joined.isEmpty() ? null : joined;
    }

    private List<String> deserializarGaleria(String galeria, String imagenPrincipal) {
        if (galeria == null || galeria.isBlank()) {
            return imagenPrincipal != null && !imagenPrincipal.isBlank()
                    ? List.of(imagenPrincipal)
                    : Collections.emptyList();
        }
        return Arrays.stream(galeria.split("\\|"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    private Producto buscarActivo(Long id) {
        return productoRepository.findById(id)
                .filter(p -> Boolean.TRUE.equals(p.getEstadoActivo()))
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Producto no encontrado con id " + id));
    }

    private Producto buscarCualquiera(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Producto no encontrado con id " + id));
    }

    private Categoria buscarCategoria(Long id) {
        return categoriaRepository.findById(id)
                .filter(c -> Boolean.TRUE.equals(c.getEstadoActivo()))
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Categoría no encontrada con id " + id));
    }

    public ProductoResponse toResponse(Producto p) {
        return new ProductoResponse(
                p.getId(),
                p.getNombre(),
                p.getDescripcion(),
                p.getPrecio(),
                p.getPrecioDescuento(),
                p.getStock(),
                p.getImagenUrl(),
                deserializarGaleria(p.getImagenesGaleria(), p.getImagenUrl()),
                Boolean.TRUE.equals(p.getDestacado()),
                p.getEtiqueta(),
                p.getMaterial(),
                p.getEstilo(),
                p.getAlmacenamiento(),
                p.getIdealPara(),
                p.getGarantia(),
                p.getEstadoActivo(),
                p.getCategoria().getId(),
                p.getCategoria().getNombre()
        );
    }
}
