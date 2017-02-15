package com.demo.mq.producer.service;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import com.demo.mq.producer.info.OrderInfo;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class ProducerService {

	Logger log = LogManager.getLogger(getClass());
	public static final String APP_NAME = "application";


	@Autowired
	private JmsTemplate jmsTemplate;
	@Autowired
	private ActiveMQQueue onDemandQueue;


	public void sendStringToQueue(final String message) {
		jmsTemplate.send(onDemandQueue, new MessageCreator(){
			@Override
			public Message createMessage(Session session) throws JMSException {
				return session.createTextMessage(message);
			}
		});
	}

    public void sendObjectToQueue(OrderInfo info) throws JMSException {
        jmsTemplate.send(onDemandQueue, new MessageCreator(){
            @Override
            public Message createMessage(Session session) throws JMSException {
                return session.createObjectMessage(info);

            }
        });
    }


	public String sendTextQueueMessageAndReceive(final String message) throws JMSException {
		Message replyMessage = jmsTemplate.sendAndReceive(onDemandQueue, new MessageCreator(){
			@Override
			public Message createMessage(Session session) throws JMSException {

				return session.createTextMessage(message);
			}
		});
		log.debug("sendTextAndReceive: {}", JSON.toJSONString(replyMessage));
		TextMessage textMessage = (TextMessage)replyMessage;
		return textMessage.getText();
	}


	public String sendObjectQueueMessageAndRec(final OrderInfo info) throws JMSException {
		Message message = jmsTemplate.sendAndReceive(onDemandQueue, new MessageCreator() {
			@Override
			public Message createMessage(Session session) throws JMSException {
				return session.createObjectMessage(info);
			}
		});
		log.debug("sendTextAndReceive: {}", JSON.toJSONString(message));
		TextMessage textMessage = (TextMessage)message;
		return textMessage.getText();
	}

	public Map getMachineInformation() {
		Map<String, Object> model = new HashMap<>();
		String[] cmd = { "/bin/sh", "-c", "uname -a" };
		log.debug("sanity-check endpoint accessed");
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
