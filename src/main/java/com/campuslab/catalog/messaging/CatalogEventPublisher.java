package com.campuslab.catalog.messaging;

import com.campuslab.catalog.config.RabbitMqProperties;
import com.campuslab.catalog.model.CatalogResource;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Publica a RabbitMQ la alerta de stock/cupo agotado, para que
 * ms-campuslab-notify avise por email al equipo administrador. Exchange y
 * routing key vienen de application.yml (campuslab.rabbitmq.*, ver
 * RabbitMqProperties / RabbitMqConfig). Usa el exchange topic en vez del
 * direct porque es una alerta interna, no una notificacion dirigida a un
 * estudiante puntual; el binding "email.*" -> q.cmd.email cubre este caso.
 *
 * Es best-effort: si el broker no esta disponible, se registra el error pero
 * no se interrumpe la actualizacion de stock.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CatalogEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final RabbitMqProperties props;

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
            rabbitTemplate.convertAndSend(props.exchange(), props.stockAgotadoRoutingKey(), json,
                    new CorrelationData(envelope.eventId()));
            log.info("Evento STOCK_AGOTADO enviado, a la espera de confirmacion (recursoId={}, eventId={})",
                    recurso.getId(), envelope.eventId());
        } catch (JsonProcessingException ex) {
            log.error("No fue posible serializar el evento STOCK_AGOTADO del recurso {}", recurso.getId(), ex);
        } catch (AmqpException ex) {
            log.error("No fue posible publicar el evento STOCK_AGOTADO del recurso {}: RabbitMQ no disponible",
                    recurso.getId(), ex);
        }
    }
}
