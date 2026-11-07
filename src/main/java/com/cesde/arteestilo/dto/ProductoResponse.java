package com.cesde.arteestilo.dto;

import java.math.BigDecimal;
import java.util.List;

public record ProductoResponse(
        Long id,
        String nombre,
        String descripcion,
        BigDecimal precio,
        BigDecimal precioDescuento,
        Integer stock,
        String imagenUrl,
        List<String> imagenesGaleria,
        Boolean destacado,
        String etiqueta,
        String material,
        String estilo,
        String almacenamiento,
        String idealPara,
        String garantia,
        Boolean estadoActivo,
        Long categoriaId,
        String categoriaNombre
) {}
