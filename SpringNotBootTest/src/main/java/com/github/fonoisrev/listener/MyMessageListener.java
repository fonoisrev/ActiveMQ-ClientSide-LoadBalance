package com.github.fonoisrev.listener;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

public class MyMessageListener implements MessageListener {
    
    @Override
    public void onMessage(Message message) {
        if (message instanceof TextMessage) {
            try {
                System.out.println(Thread.currentThread().getName() + " Receive:" +
                                   ((TextMessage) message).getText());
            } catch (JMSException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println(message);
        }
    }
}
