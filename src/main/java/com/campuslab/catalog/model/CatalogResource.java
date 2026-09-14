package com.campuslab.catalog.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entidad de dominio que representa un recurso del catálogo de CampusLab:
 * puede ser un LABORATORIO, un EQUIPO o un INSUMO.
 *
 * El campo stockCupo es polimórfico según el tipo de recurso:
 * - Para LABORATORIO representa el "cupo" disponible (ej. capacidad por turno).
 * - Para EQUIPO / INSUMO representa el "stock" disponible (unidades).
 */
@Entity
@Table(name = "CATALOG_RESOURCES")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CatalogResource {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "catalog_resource_seq")
    @SequenceGenerator(
            name = "catalog_resource_seq",
            sequenceName = "SEQ_CATALOG_RESOURCE",
            allocationSize = 1
    )
    @Column(name = "ID")
    private Long id;

    @NotBlank(message = "El nombre del recurso es obligatorio")
    @Column(name = "NAME", nullable = false, length = 150)
    private String name;

    @NotNull(message = "El tipo de recurso es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "RESOURCE_TYPE", nullable = false, length = 20)
    private ResourceType resourceType;

    @Column(name = "DESCRIPTION", length = 500)
    private String description;

    @Column(name = "LOCATION", length = 100)
    private String location;

    @NotNull(message = "El stock/cupo es obligatorio")
    @Min(value = 0, message = "El stock/cupo no puede ser negativo")
    @Column(name = "STOCK_CUPO", nullable = false)
    private Integer stockCupo;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
