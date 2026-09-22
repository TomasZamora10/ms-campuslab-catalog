package com.campuslab.catalog.messaging;

import com.campuslab.catalog.model.CatalogResource;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Publica a RabbitMQ (exchange cmd.topic, ya declarado por infra/mq/definitions.json)
 * la alerta de stock/cupo agotado, para que ms-campuslab-notify avise por
 * email al equipo administrador. Usa el exchange topic (routing key
 * "email.stock-agotado") en vez del direct porque este es un evento de
 * alerta interna, no una notificacion dirigida a un estudiante puntual; el
 * binding "email.*" -> q.cmd.email ya cubre este caso sin necesitar una cola
 * o binding nuevo.
 *
 * Es best-effort: si el broker no esta disponible, se registra el error pero
 * no se interrumpe la actualizacion de stock.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CatalogEventPublisher {

    private static final String EXCHANGE = "cmd.topic";
    private static final String ROUTING_KEY = "email.stock-agotado";

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public void publicarStockAgotadoSiCorresponde(CatalogResource recurso) {
        if (recurso.getStockCupo() == null || recurso.getStockCupo() > 0) {
            return;
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("recursoId", recurso.getId());
        payload.put("nombre", recurso.getName());
        payload.put("tipo", recurso.getResourceType().name());

        EventEnvelope envelope = EventEnvelope.of(
                "STOCK_AGOTADO", "recurso-" + recurso.getId(), payload);

        try {
            String json = objectMapper.writeValueAsString(envelope);
            rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, json);
            log.info("Evento STOCK_AGOTADO publicado (recursoId={}, eventId={})",
                    recurso.getId(), envelope.eventId());
        } catch (JsonProcessingException ex) {
            log.error("No fue posible serializar el evento STOCK_AGOTADO del recurso {}", recurso.getId(), ex);
        } catch (AmqpException ex) {
            log.error("No fue posible publicar el evento STOCK_AGOTADO del recurso {}: RabbitMQ no disponible",
                    recurso.getId(), ex);
        }
    }
}
