package cl.duoc.lab.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;

@Configuration
public class RabbitPedidosConfig {

    private final RabbitNamesProperties.Pedidos p;
    private final RabbitPoliciesProperties pol;
    public static final String RAW_LISTENER_FACTORY = "rawListenerContainerFactory";

    public RabbitPedidosConfig(RabbitNamesProperties names, RabbitPoliciesProperties policies) {
        this.p = names.pedidos();
        this.pol = policies;
    }

    // ===================== Exchanges =====================

    @Bean
    public DirectExchange pedidosExchange() {
        return new DirectExchange(p.exchange(), true, false);
    }

    /** Dead Letter Exchange: recibe los mensajes que murieron en inventario. */
    @Bean
    public DirectExchange pedidosDlx() {
        return new DirectExchange(p.dlxExchange(), true, false);
    }

    // ======================= Colas =======================

    @Bean
    public Queue notificacionesQueue() {
        return QueueBuilder.durable(p.notificacionesQueue()).build();
    }

    /** Cola principal de inventario, con DLX, TTL y límite de largo. */
    @Bean
    public Queue inventarioQueue() {
        return QueueBuilder.durable(p.inventarioQueue())
                .ttl(pol.inventarioTtlMs())
                .maxLength(pol.inventarioMaxLength())
                .deadLetterExchange(p.dlxExchange())
                .deadLetterRoutingKey(p.inventarioDlqRk())
                .build();
    }

    /** DLQ: guarda para auditoría los mensajes fallidos (retención de 24 h). */
    @Bean
    public Queue inventarioDlq() {
        return QueueBuilder.durable(p.inventarioDlq())
                .ttl(pol.dlqTtlMs())
                .maxLength(pol.dlqMaxLength())
                .build();
    }

    // ===================== Bindings ======================
    // Notificaciones recibe los 3 eventos

    @Bean
    public Binding notificacionesCreado() {
        return BindingBuilder.bind(notificacionesQueue()).to(pedidosExchange()).with(p.rkCreado());
    }

    @Bean
    public Binding notificacionesPagado() {
        return BindingBuilder.bind(notificacionesQueue()).to(pedidosExchange()).with(p.rkPagado());
    }

    @Bean
    public Binding notificacionesCancelado() {
        return BindingBuilder.bind(notificacionesQueue()).to(pedidosExchange()).with(p.rkCancelado());
    }

    // Inventario recibe solo creado y cancelado

    @Bean
    public Binding inventarioCreado() {
        return BindingBuilder.bind(inventarioQueue()).to(pedidosExchange()).with(p.rkCreado());
    }

    @Bean
    public Binding inventarioCancelado() {
        return BindingBuilder.bind(inventarioQueue()).to(pedidosExchange()).with(p.rkCancelado());
    }

    // La DLQ se enlaza al DLX
    @Bean
    public Binding inventarioDlqBinding() {
        return BindingBuilder.bind(inventarioDlq()).to(pedidosDlx()).with(p.inventarioDlqRk());
    }

    // ================== Serialización ====================

    /** Serializa los eventos como JSON. */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

        /** Fábrica sin conversor JSON: entrega el mensaje crudo, para que el consumidor decida cómo leerlo. */
    @Bean(name = RAW_LISTENER_FACTORY)
    public SimpleRabbitListenerContainerFactory rawListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setPrefetchCount(pol.prefetch());
        return factory;
    }
}