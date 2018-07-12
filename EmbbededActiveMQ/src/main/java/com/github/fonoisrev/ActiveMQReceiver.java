package com.github.fonoisrev;

import org.apache.activemq.util.HexSupport;
import org.springframework.jms.annotation.JmsListener;

public class ActiveMQReceiver {
    
    @JmsListener(destination = "test")
    public void receive1(String message) {
        System.out.println("One Receive: " + message);
    }
    
    @JmsListener(destination = "test")
    public void receive2(String message) {
        System.out.println("Two Receive: " + message);
    }
    
    @JmsListener(destination = "test")
    public void receive3(String message) {
        System.out.println("Three Receive: " + message);
    }
}
