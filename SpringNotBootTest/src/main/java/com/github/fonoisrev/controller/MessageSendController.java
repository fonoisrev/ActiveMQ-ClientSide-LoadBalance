package com.github.fonoisrev.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.UUID;

@Controller
public class MessageSendController {
    
    @Autowired
    JmsMessagingTemplate jmsMessagingTemplate;
    
    @RequestMapping("/send")
    @ResponseBody
    public String doSend() {
        String toSend = UUID.randomUUID().toString();
        jmsMessagingTemplate.convertAndSend("test", toSend);
        return toSend;
    }
}
