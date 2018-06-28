package com.github.fonoisrev;

import org.apache.activemq.xbean.BrokerFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Controller;

@SpringBootApplication
@EnableJms
@Controller
public class JmsApplication {
    
    @Value("${spring.activemq.broker-url}")
    String url;
    
    @Autowired
    JmsTemplate jmsTemplate;
    
    public static void main(String[] args) {
        new SpringApplicationBuilder(JmsApplication.class)
                .web(false).build().run(args);
    }
    
    @Bean
    public CommandLineRunner testSend() {
        return new CommandLineRunner() {
            @Override
            public void run(String... args) throws Exception {
                for (int i = 0; i < 10; ++i) {
                    jmsTemplate.convertAndSend("test", "message");
                }
            }
        };
    }
    
    @Bean
    public ActiveMQReceiver activeMQReciever() {
        return new ActiveMQReceiver();
    }
    
    @Bean
    public BrokerFactoryBean activemq() throws Exception {
        BrokerFactoryBean broker = new BrokerFactoryBean();
        broker.setConfig(new ClassPathResource("activemq.xml"));
        broker.setStart(true);
        return broker;
    }
    
    
}
