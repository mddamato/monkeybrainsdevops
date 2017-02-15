package com.demo.mq.consumer.config;

import javax.jms.Queue;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.listener.MessageListenerContainer;
import org.springframework.jms.listener.adapter.MessageListenerAdapter;
import org.springframework.jms.support.converter.SimpleMessageConverter;

@Configuration
@Description(value = "ActiveMQ Configuration")
public class ActiveMQConfig {

    private final String QUEUE_ON_DEMAND;
    private final String QUEUE_STREAM;

    @Autowired
    public ActiveMQConfig(@Value("${queue.ondemand.name}") String onDemandQueueName,
                          @Value("${queue.stream.name}") String streamQueueName){
        QUEUE_ON_DEMAND = onDemandQueueName;
        QUEUE_STREAM = streamQueueName;
    }

    @Bean(name="onDemandQueue")
	public Queue onDemandQueue() {
	    return new ActiveMQQueue(QUEUE_ON_DEMAND);
	}

	@Bean(name="streamQueue")
    public Queue streamQueue() {
	    return new ActiveMQQueue(QUEUE_STREAM);
    }

	@Bean(name="textMessageListenerAdapter")
	public MessageListenerAdapter messageListenerAdapter() {
		MessageListenerAdapter adapter = new MessageListenerAdapter();
		adapter.setMessageConverter(new SimpleMessageConverter());
		adapter.setDelegate(textConsumerListener());
		return adapter;
	}

	public CachingConnectionFactory connectionFactory(ActiveMQConnectionFactory connectionFactory) {
		CachingConnectionFactory factory = new CachingConnectionFactory(connectionFactory);
		return factory;
	}

	@Bean
	public MessageListenerContainer messageListenerContainer(ActiveMQConnectionFactory connectionFactory) {
		DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();
		container.setConnectionFactory(connectionFactory(connectionFactory));
		container.setDestination(streamQueue());
		container.setMessageListener(messageListenerAdapter());
		container.setConcurrency("10-50");
		return container;
	}

	@Bean(name="jmsTemplate")
	public JmsTemplate jmsTemplate(ActiveMQConnectionFactory connectionFactory) {
		JmsTemplate template = new JmsTemplate(connectionFactory(connectionFactory));
		template.setDefaultDestination(onDemandQueue());
		template.setMessageConverter(new SimpleMessageConverter());
		template.setReceiveTimeout(3000);
		return template;
	}

	@Bean
	public ConsumerListener textConsumerListener() {
		return new ConsumerListener();
	}
}