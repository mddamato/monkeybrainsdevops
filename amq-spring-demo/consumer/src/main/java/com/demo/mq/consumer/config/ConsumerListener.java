package com.demo.mq.consumer.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

@Component("consumerListener")
public class ConsumerListener{
	Logger log = LogManager.getLogger(getClass());

	public void handleMessage(String message) {
		log.info("Triggered handleMessage: {}", message);
	}
}