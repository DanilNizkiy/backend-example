package com.nizkiyd.receiver.config;

import lombok.extern.slf4j.Slf4j;
import org.aopalliance.aop.Advice;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.interceptor.RetryInterceptorBuilder;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Slf4j
@EnableRetry
public class RabbitConfiguration {

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory("localhost");
        return connectionFactory;
    }

    @Bean
    public AmqpAdmin amqpAdmin() {
        return new RabbitAdmin(connectionFactory());
    }

    @Bean
    public RabbitTemplate rabbitTemplate() {
        return new RabbitTemplate(connectionFactory());
    }

    //direct

    @Bean
    public Queue queueDirect() {
        return new Queue("queue.direct");
    }

    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange("exchange.direct");
    }

    @Bean
    public Binding bindingDirect() {
        return BindingBuilder.bind(queueDirect()).to(directExchange()).with("create");
    }

    //fanout
    @Bean
    public Queue queueFanout1() {
        return new Queue("queue.fanout.first");
    }

    @Bean
    public Queue queueFanout2() {
        return new Queue("queue.fanout.second");
    }

    @Bean
    public FanoutExchange fanoutExchange() {
        return new FanoutExchange("exchange.fanout");
    }

    @Bean
    public Binding bindingFanout1() {
        return BindingBuilder.bind(queueFanout1()).to(fanoutExchange());
    }

    @Bean
    public Binding bindingFanout2() {
        return BindingBuilder.bind(queueFanout2()).to(fanoutExchange());
    }



    @Bean
    public DirectExchange directExchangeDeadLetter() {
        return new DirectExchange("tutorial-exchange");
    }

    @Bean
    public Queue queueDeadLetter() {
        return QueueBuilder.durable("primaryWorkerQueue")
                .deadLetterExchange("tutorial-exchange")
                .deadLetterRoutingKey("primaryWorkerQueue.parkingLot")
                .build();
    }

    @Bean
    public Queue deadLetterHandling() {
        return new Queue("primaryWorkerQueue.parkingLot");
    }

    @Bean
    public Binding bindingDeadLetter1(Queue queueDeadLetter, DirectExchange directExchangeDeadLetter) {
        return BindingBuilder.bind(queueDeadLetter).to(directExchangeDeadLetter).with("primaryRoutingKey");
    }

    @Bean
    public Binding bindingDeadLetter2(Queue deadLetterHandling, DirectExchange directExchangeDeadLetter) {
        return BindingBuilder.bind(deadLetterHandling).to(directExchangeDeadLetter).with("primaryWorkerQueue.parkingLot");
    }
}
