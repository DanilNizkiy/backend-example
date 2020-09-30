package com.nizkiyd.receiver.config;

import com.nizkiyd.receiver.exception.CustomFatalExceptionStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;
import org.springframework.amqp.rabbit.listener.FatalExceptionStrategy;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.util.ErrorHandler;

@Configuration
@Slf4j
@EnableRetry
public class RabbitConfiguration {

    public static final String CREATE_REQUISITION_EXCHANGE = "create-requisition.exchange";
    public static final String CREATE_REQUISITION_QUEUE = "create-requisition.queue";
    public static final String CREATE_REQUISITION_DLE = "create-requisition.dle";
    public static final String CREATE_REQUISITION_DLQ = "create-requisition.dlq";
    public static final int CREATE_REQUISITION_TTL = 100;
///////////////
    public static final String CREATE_DOUBLE_REQUISITION_EXCHANGE = "create-double-requisition.exchange";
    public static final String CREATE_DOUBLE_REQUISITION_QUEUE1 = "create-double-requisition.queue.first";
    public static final String CREATE_DOUBLE_REQUISITION_QUEUE2 = "create-double-requisition.queue.second";
    public static final String CREATE_DOUBLE_REQUISITION_DLQ1 = "create-double-requisition.dlq.first";
    public static final String CREATE_DOUBLE_REQUISITION_DLQ2 = "create-double-requisition.dlq.second";
    public static final int CREATE_DOUBLE_REQUISITION_TTL = 100;

    @Bean
    public ConnectionFactory connectionFactory() {
        return new CachingConnectionFactory("localhost");
    }

    @Bean
    public RabbitTemplate rabbitTemplate() {
        return new RabbitTemplate(connectionFactory());
    }

    @Bean
    public RabbitAdmin amqpAdmin() {
        return new RabbitAdmin(connectionFactory());
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            SimpleRabbitListenerContainerFactoryConfigurer configurer) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory());
        factory.setErrorHandler(errorHandler());
        return factory;
    }

    @Bean
    public ErrorHandler errorHandler() {
        return new ConditionalRejectingErrorHandler(customExceptionStrategy());
    }

    @Bean
    public FatalExceptionStrategy customExceptionStrategy() {
        return new CustomFatalExceptionStrategy();
    }

    //dead letter
    @Bean
    public Exchange createRequisitionExchange() {
        return ExchangeBuilder.directExchange(CREATE_REQUISITION_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public Queue createRequisitionQueue() {
        return QueueBuilder.durable(CREATE_REQUISITION_QUEUE)
                .withArgument("x-dead-letter-exchange", CREATE_REQUISITION_DLE)
                .withArgument("x-dead-letter-routing-key", CREATE_REQUISITION_DLQ)
                .build();
    }

    @Bean
    public Binding createRequisitionBinding() {
        return BindingBuilder
                .bind(createRequisitionQueue())
                .to(createRequisitionExchange()).with(CREATE_REQUISITION_QUEUE).noargs();
    }

    @Bean
    public Exchange createRequisitionDeadLetterExchange() {
        return ExchangeBuilder
                .directExchange(CREATE_REQUISITION_DLE)
                .durable(true)
                .build();
    }

    @Bean
    public Queue createRequisitionDeadLetterQueue() {
        return QueueBuilder
                .durable(CREATE_REQUISITION_DLQ)
                .withArgument("x-message-ttl", CREATE_REQUISITION_TTL)
                .withArgument("x-dead-letter-exchange", CREATE_REQUISITION_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", CREATE_REQUISITION_QUEUE)
                .build();
    }

    @Bean
    public Binding moveToNirvanaDeadLetterBinding() {
        return BindingBuilder
                .bind(createRequisitionDeadLetterQueue())
                .to(createRequisitionDeadLetterExchange()).with(CREATE_REQUISITION_DLQ)
                .noargs();
    }

    //fanout
    @Bean
    public FanoutExchange createDoubleRequisitionExchange() {
        return ExchangeBuilder.fanoutExchange(CREATE_DOUBLE_REQUISITION_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public Queue createDoubleRequisitionQueue1() {
        return QueueBuilder.durable(CREATE_DOUBLE_REQUISITION_QUEUE1)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", CREATE_DOUBLE_REQUISITION_DLQ1)
                .build();
    }

    @Bean
    public Binding createDoubleRequisitionBinding1() {
        return BindingBuilder
                .bind(createDoubleRequisitionQueue1())
                .to(createDoubleRequisitionExchange());
    }

    @Bean
    public Queue createDoubleRequisitionDeadLetterQueue1() {
        return QueueBuilder.durable(CREATE_DOUBLE_REQUISITION_DLQ1)
                .withArgument("x-message-ttl", CREATE_DOUBLE_REQUISITION_TTL)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", CREATE_DOUBLE_REQUISITION_QUEUE1)
                .build();
    }

    @Bean
    public Queue createDoubleRequisitionQueue2() {
        return QueueBuilder.durable(CREATE_DOUBLE_REQUISITION_QUEUE2)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", CREATE_DOUBLE_REQUISITION_DLQ2)
                .build();
    }

    @Bean
    public Queue createDoubleRequisitionDeadLetterQueue2() {
        return QueueBuilder.durable(CREATE_DOUBLE_REQUISITION_DLQ2)
                .withArgument("x-message-ttl", CREATE_DOUBLE_REQUISITION_TTL)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", CREATE_DOUBLE_REQUISITION_QUEUE2)
                .build();
    }

    @Bean
    public Binding createDoubleRequisitionBinding2() {
        return BindingBuilder
                .bind(createDoubleRequisitionQueue2())
                .to(createDoubleRequisitionExchange());
    }
}
