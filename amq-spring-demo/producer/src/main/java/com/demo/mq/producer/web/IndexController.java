package com.demo.mq.producer.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import com.demo.mq.producer.info.OrderInfo;
import com.demo.mq.producer.service.ProducerService;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import javax.jms.JMSException;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
public class IndexController {

    Logger logger = LogManager.getLogger(getClass());

    @Autowired
    private ProducerService producerService;



    @ResponseBody
    @RequestMapping(value = "/sendObject", method = GET)
    public void sendObject() throws JMSException {
        String message = "Hello Queue Message. Date: " + DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");
        logger.debug("attempting to send");
        OrderInfo info = new OrderInfo(new Random().nextInt(100), message, new Random().nextFloat());
        logger.debug(info);
        producerService.sendObjectToQueue(info);
    }

    @ResponseBody
    @RequestMapping(value = "/sendString/{string}", method = GET)
    public void send(@PathVariable("string") String string) throws JMSException {
        logger.debug("attempting to send: "+string);
        String message = "Message : "+string+". Date: " + DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");
        producerService.sendStringToQueue(message);
    }

    @ResponseBody
    @RequestMapping(value = "/sendString", method = POST)
    public String createBorder(final @RequestBody String paramObject) {

        logger.info("POST REQUEST FOR /sendString params:"+paramObject);
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        producerService.sendStringToQueue(paramObject.toString());

//        HttpEntity<String> entity = new HttpEntity<String>(
//                paramObject.toString(), headers);
//        restTemplate.exchange("http://"+ borderServiceLocation +"/border", HttpMethod.POST, entity,
//                String.class);
        return paramObject;
    }

    @RequestMapping("/sanity-check")
    @ResponseBody
    public Map<String, Object> sanityCheck() {
        return producerService.getMachineInformation();
    }


}
