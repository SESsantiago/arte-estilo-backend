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
@Table(name = "productos")
public class Producto extends BaseEntity {

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(length = 1000)
    private String descripcion;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precio;

    @Column(name = "precio_descuento", precision = 12, scale = 2)
    private BigDecimal precioDescuento;

    @Column(nullable = false)
    private Integer stock;

    @Column(name = "imagen_url", length = 500)
    private String imagenUrl;

    @Column(name = "imagenes_galeria", length = 2000)
    private String imagenesGaleria;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "boolean default false")
    private Boolean destacado = false;

    @Column(length = 50)
    private String etiqueta;

    @Column(length = 150)
    private String material;

    @Column(length = 150)
    private String estilo;

    @Column(length = 150)
    private String almacenamiento;

    @Column(name = "ideal_para", length = 200)
    private String idealPara;

    @Column(length = 100)
    private String garantia;

    /** Muchos productos pertenecen a una categoría. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    /** Lado inverso de la relación M:N de favoritos (dueño: Usuario). */
    @Builder.Default
    @ManyToMany(mappedBy = "favoritos", fetch = FetchType.LAZY)
    private List<Usuario> usuariosQueFavorecen = new ArrayList<>();
}
