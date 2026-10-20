package com.campuslab.catalog.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.boot.autoconfigure.amqp.RabbitTemplateCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;

/**
 * Configuracion de mensajeria RabbitMQ del lado productor (catalog). El
 * controller no conoce exchanges ni routing keys: solo llama a
 * CatalogEventPublisher, que los toma de RabbitMqProperties.
 *
 * <pre>
 *   catalog --email.stock-agotado--> cmd.topic --email.*--> q.cmd.email (notify)
 * </pre>
 */
@Configuration
@EnableConfigurationProperties(RabbitMqProperties.class)
public class RabbitMqConfig {

    private static final Logger log = LoggerFactory.getLogger(RabbitMqConfig.class);

    @Bean
    public TopicExchange alertExchange(RabbitMqProperties props) {
        return ExchangeBuilder.topicExchange(props.exchange()).durable(true).build();
    }

    /**
     * Con mandatory=true (spring.rabbitmq.template.mandatory) el broker
     * devuelve los mensajes que no coinciden con ningun binding; se
     * registran en el log en vez de perderse en silencio.
     */
    @Bean
    public RabbitTemplateCustomizer mensajesNoEnrutadosCustomizer() {
        return template -> template.setReturnsCallback(returned -> log.error(
                "Mensaje NO entregado por RabbitMQ: exchange={} routingKey={} replyCode={} replyText={} cuerpo={}",
                returned.getExchange(), returned.getRoutingKey(), returned.getReplyCode(), returned.getReplyText(),
                new String(returned.getMessage().getBody(), StandardCharsets.UTF_8)));
    }
}
