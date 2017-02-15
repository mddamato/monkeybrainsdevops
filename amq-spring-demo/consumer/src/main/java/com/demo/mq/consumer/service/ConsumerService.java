package com.demo.mq.consumer.service;

import javax.jms.JMSException;

import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@Component
public class ConsumerService {
	Logger log = LogManager.getLogger(getClass());
    public static final String APP_NAME = "application";
	@Autowired
	private JmsTemplate jmsTemplate;
	@Autowired
	private ActiveMQQueue onDemandQueue;

	public String getMessage() {
	    ActiveMQTextMessage message = (ActiveMQTextMessage) jmsTemplate.receive();
        String messageText;
        try {
            message.acknowledge();
            messageText = message.getText();
        } catch (JMSException e) {
            log.error(e.getStackTrace());
            return "Error: No messages available";
        }
        log.info("Got message: " + message.toString());

        return messageText;
    }

    public Map getMachineInformation() {
        Map<String, Object> model = new HashMap<>();
        String[] cmd = { "/bin/sh", "-c", "uname -a" };
        log.info("sanity-check endpoint accessed");
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            p.waitFor();
            String line;
            while ((line = in.readLine()) != null) {
                model.put("uname", line);
            }
            in.close();
        } catch (IOException | InterruptedException e) {
            log.error(e);
        }
        model.put("status", this.getApplicationName()+" is online!");
        return model;
    }

    public String getApplicationName() {
        return APP_NAME;
    }
}