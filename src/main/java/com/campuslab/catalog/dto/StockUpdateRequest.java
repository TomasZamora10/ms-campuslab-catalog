package com.campuslab.catalog.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Payload para el PUT /api/catalog/resources/{id}.
 * Solo permite actualizar el stock (equipos/insumos) o el cupo (laboratorios).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockUpdateRequest {

    @NotNull(message = "El campo stockCupo es obligatorio")
    @Min(value = 0, message = "El stock/cupo no puede ser negativo")
    private Integer stockCupo;
}
