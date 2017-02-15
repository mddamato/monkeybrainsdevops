package com.demo.mq.consumer.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.demo.mq.consumer.service.ConsumerService;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ConsumerController {

	Logger logger = LogManager.getLogger(getClass());

	@Autowired
	private ConsumerService consumerService;
	


	@RequestMapping(value="/message", method = RequestMethod.GET)
	public String message(String message) {
		logger.info("Request for a message received");
		return consumerService.getMessage().toString();
	}

    @RequestMapping("/sanity-check")
    @ResponseBody
    public Map<String, Object> sanityCheck() {
        return consumerService.getMachineInformation();
    }
}