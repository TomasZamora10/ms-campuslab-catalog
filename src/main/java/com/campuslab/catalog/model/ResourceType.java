package com.campuslab.catalog.model;

/**
 * Clasifica los recursos administrados por el catálogo:
 * - LABORATORIO: usa el campo stockCupo como "cupo" (capacidad de estudiantes/horario).
 * - EQUIPO e INSUMO: usan el campo stockCupo como "stock" (unidades disponibles).
 */
public enum ResourceType {
    LABORATORIO,
    EQUIPO,
    INSUMO
}
