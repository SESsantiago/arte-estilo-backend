package com.cesde.arteestilo.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public record ProductoPatch(
        @Pattern(regexp = ".*\\S.*", message = "no puede estar vacío") @Size(max = 120) String nombre,
        @Size(max = 1000) String descripcion,
        @DecimalMin("0.01") @Digits(integer = 10, fraction = 2) BigDecimal precio,
        @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal precioDescuento,
        @PositiveOrZero Integer stock,
        @Size(max = 500) String imagenUrl,
        List<String> imagenesGaleria,
        Boolean destacado,
        @Size(max = 50) String etiqueta,
        @Size(max = 150) String material,
        @Size(max = 150) String estilo,
        @Size(max = 150) String almacenamiento,
        @Size(max = 200) String idealPara,
        @Size(max = 100) String garantia,
        Boolean estadoActivo,
        Long categoriaId
) {}
