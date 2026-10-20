package com.campuslab.catalog.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Exchange y routing key de la alerta de stock agotado que catalog publica a
 * RabbitMQ, centralizados en application.yml (prefijo campuslab.rabbitmq).
 * Deben coincidir con el binding email.* -> q.cmd.email que declara
 * ms-campuslab-notify y con infra/mq/definitions.json.
 *
 * @param exchange               exchange topic de alertas internas (cmd.topic)
 * @param stockAgotadoRoutingKey routing key de la alerta (email.stock-agotado)
 */
@Validated
@ConfigurationProperties(prefix = "campuslab.rabbitmq")
public record RabbitMqProperties(
        @NotBlank String exchange,
        @NotBlank String stockAgotadoRoutingKey
) {
}
