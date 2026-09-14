package com.campuslab.catalog.controller;

import com.campuslab.catalog.dto.StockUpdateRequest;
import com.campuslab.catalog.model.CatalogResource;
import com.campuslab.catalog.model.ResourceType;
import com.campuslab.catalog.repository.CatalogResourceRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST del microservicio ms-campuslab-catalog.
 * Expone el CRUD de laboratorios, equipos e insumos.
 */
@RestController
@RequestMapping("/api/catalog/resources")
public class CatalogResourceController {

    private final CatalogResourceRepository repository;

    public CatalogResourceController(CatalogResourceRepository repository) {
        this.repository = repository;
    }

    /**
     * GET /api/catalog/resources
     * Lista todos los recursos del catálogo. Opcionalmente filtra por tipo
     * (?type=LABORATORIO | EQUIPO | INSUMO).
     */
    @GetMapping
    public ResponseEntity<List<CatalogResource>> listResources(
            @RequestParam(required = false) ResourceType type) {

        List<CatalogResource> resources = (type != null)
                ? repository.findByResourceType(type)
                : repository.findAll();

        return ResponseEntity.ok(resources);
    }

    /**
     * POST /api/catalog/resources
     * Crea un nuevo laboratorio, equipo o insumo en el catálogo.
     */
    @PostMapping
    public ResponseEntity<CatalogResource> createResource(
            @Valid @RequestBody CatalogResource resource) {

        // Se garantiza que sea una creación (ignora cualquier id enviado por el cliente)
        resource.setId(null);
        CatalogResource saved = repository.save(resource);

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * PUT /api/catalog/resources/{id}
     * Actualiza el stock (equipos/insumos) o el cupo (laboratorios) de un recurso existente.
     */
    @PutMapping("/{id}")
    public ResponseEntity<CatalogResource> updateStockOrCupo(
            @PathVariable Long id,
            @Valid @RequestBody StockUpdateRequest request) {

        return repository.findById(id)
                .map(existing -> {
                    existing.setStockCupo(request.getStockCupo());
                    CatalogResource updated = repository.save(existing);
                    return ResponseEntity.ok(updated);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
