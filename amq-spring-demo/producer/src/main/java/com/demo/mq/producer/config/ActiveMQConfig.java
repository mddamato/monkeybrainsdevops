package com.demo.mq.producer.config;

import javax.jms.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.jms.core.JmsTemplate;
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

	public PooledConnectionFactory connectionFactory(ActiveMQConnectionFactory connectionFactory) {
		PooledConnectionFactory pool = new PooledConnectionFactory(connectionFactory);
		pool.setMaxConnections(5);
		return pool;
	}

	@Bean(name="jmsTemplate")
	public JmsTemplate jmsTemplate(ActiveMQConnectionFactory connectionFactory) {
		JmsTemplate template = new JmsTemplate(connectionFactory(connectionFactory));
		template.setDefaultDestination(onDemandQueue());
		template.setMessageConverter(new SimpleMessageConverter());
        template.setReceiveTimeout(3000);
		return template;
	}
}
