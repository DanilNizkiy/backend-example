package com.nizkiyd.receiver.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

@Configuration
@Slf4j
@EnableRetry
public class RabbitConfiguration {

    private static final String EXCHANGE_NAME = "tutorial-exchange";
    public static final String PRIMARY_QUEUE = "primaryWorkerQueue";
    private static final String WAIT_QUEUE = PRIMARY_QUEUE + ".wait";
    private static final String PRIMARY_ROUTING_KEY = "primaryRoutingKey";

    @Bean
    DirectExchange exchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    Queue primaryQueue() {
        return QueueBuilder.durable(PRIMARY_QUEUE)
                .deadLetterExchange(EXCHANGE_NAME)
                .deadLetterRoutingKey(WAIT_QUEUE)
                .build();
    }

    @Bean
    Queue waitQueue() {
        return QueueBuilder.durable(WAIT_QUEUE)
                .deadLetterExchange(EXCHANGE_NAME)
                .deadLetterRoutingKey(PRIMARY_ROUTING_KEY)
                .ttl(5000)
                .build();
    }

    @Bean
    Binding primaryBinding(Queue primaryQueue, DirectExchange exchange) {
        return BindingBuilder.bind(primaryQueue).to(exchange).with(PRIMARY_ROUTING_KEY);
    }

    @Bean
    Binding waitBinding(Queue waitQueue, DirectExchange exchange){
        return BindingBuilder.bind(waitQueue).to(exchange).with(WAIT_QUEUE);
    }


}
