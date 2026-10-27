package com.campuslab.catalog;

import com.campuslab.catalog.model.CatalogResource;
import com.campuslab.catalog.model.ResourceType;
import com.campuslab.catalog.repository.CatalogResourceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Carga el contexto completo (perfil "test": H2 en memoria) y prueba el CRUD
 * del catalogo y la publicacion de la alerta de stock agotado a RabbitMQ.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MsCampuslabCatalogApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CatalogResourceRepository repository;

    @MockBean
    private RabbitTemplate rabbitTemplate;

    @BeforeEach
    void limpiar() {
        repository.deleteAll();
    }

    @Test
    void contextLoads() throws Exception {
        mockMvc.perform(get("/api/catalog/resources"))
                .andExpect(status().isOk());
    }

    @Test
    void crearRecurso_sinNombre_responde400() throws Exception {
        mockMvc.perform(post("/api/catalog/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"resourceType\":\"EQUIPO\",\"stockCupo\":3}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crearRecurso_valido_responde201() throws Exception {
        mockMvc.perform(post("/api/catalog/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Osciloscopio\",\"resourceType\":\"EQUIPO\",\"stockCupo\":3}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Osciloscopio"));
    }

    @Test
    void actualizarStockA0_publicaAlertaDeStockAgotado() throws Exception {
        Long id = guardarRecurso(2);

        mockMvc.perform(put("/api/catalog/resources/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stockCupo\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockCupo").value(0));

        verify(rabbitTemplate).convertAndSend(eq("cmd.topic"), eq("email.stock-agotado"), contains("STOCK_AGOTADO"), any(CorrelationData.class));
    }

    @Test
    void actualizarStockPositivo_noPublicaAlerta() throws Exception {
        Long id = guardarRecurso(2);

        mockMvc.perform(put("/api/catalog/resources/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stockCupo\":5}"))
                .andExpect(status().isOk());

        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), anyString(), any(CorrelationData.class));
    }

    @Test
    void actualizarStockNegativo_responde400() throws Exception {
        Long id = guardarRecurso(2);

        mockMvc.perform(put("/api/catalog/resources/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stockCupo\":-1}"))
                .andExpect(status().isBadRequest());
    }

    private Long guardarRecurso(int stock) {
        CatalogResource recurso = new CatalogResource();
        recurso.setName("Multimetro");
        recurso.setResourceType(ResourceType.EQUIPO);
        recurso.setStockCupo(stock);
        return repository.save(recurso).getId();
    }
}
