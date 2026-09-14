package com.campuslab.catalog.repository;

import com.campuslab.catalog.model.CatalogResource;
import com.campuslab.catalog.model.ResourceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CatalogResourceRepository extends JpaRepository<CatalogResource, Long> {

    // Útil para filtrar el catálogo por tipo (laboratorio, equipo, insumo)
    List<CatalogResource> findByResourceType(ResourceType resourceType);
}
