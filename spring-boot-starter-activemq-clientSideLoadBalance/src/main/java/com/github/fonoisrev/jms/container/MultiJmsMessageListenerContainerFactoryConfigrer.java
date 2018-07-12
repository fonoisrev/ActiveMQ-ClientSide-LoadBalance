package com.github.fonoisrev.jms.container;

import org.springframework.boot.autoconfigure.jms.JmsProperties;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.destination.DestinationResolver;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.util.Assert;

import javax.jms.ConnectionFactory;

// todo javadoc
public class MultiJmsMessageListenerContainerFactoryConfigrer {
    
    private DestinationResolver destinationResolver;
    
    private MessageConverter messageConverter;
    
    private JtaTransactionManager transactionManager;
    
    private JmsProperties jmsProperties;
    
    /**
     * Set the {@link DestinationResolver} to use or {@code null} if no destination
     * resolver should be associated with the factory by default.
     * @param destinationResolver the {@link DestinationResolver}
     */
    public void setDestinationResolver(DestinationResolver destinationResolver) {
        this.destinationResolver = destinationResolver;
    }
    
    /**
     * Set the {@link MessageConverter} to use or {@code null} if the out-of-the-box
     * converter should be used.
     * @param messageConverter the {@link MessageConverter}
     */
    public void setMessageConverter(MessageConverter messageConverter) {
        this.messageConverter = messageConverter;
    }
    
    /**
     * Set the {@link JtaTransactionManager} to use or {@code null} if the JTA support
     * should not be used.
     * @param transactionManager the {@link JtaTransactionManager}
     */
    public void setTransactionManager(JtaTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }
    
    /**
     * Set the {@link JmsProperties} to use.
     * @param jmsProperties the {@link JmsProperties}
     */
    public void setJmsProperties(JmsProperties jmsProperties) {
        this.jmsProperties = jmsProperties;
    }
    
    /**
     * Configure the specified jms listener container factory. The factory can be further
     * tuned and default settings can be overridden.
     * @param factory the {@link MultiJmsMessageListenerContainerFactory} instance to configure
     * @param connectionFactory the {@link ConnectionFactory} to use
     */
    public void configure(MultiJmsMessageListenerContainerFactory factory,
                          ConnectionFactory connectionFactory) {
        Assert.notNull(factory, "Factory must not be null");
        Assert.notNull(connectionFactory, "ConnectionFactory must not be null");
        factory.setConnectionFactory(connectionFactory);
        factory.setPubSubDomain(this.jmsProperties.isPubSubDomain());
        if (this.transactionManager != null) {
            factory.setTransactionManager(this.transactionManager);
        }
        else {
            factory.setSessionTransacted(true);
        }
        if (this.destinationResolver != null) {
            factory.setDestinationResolver(this.destinationResolver);
        }
        if (this.messageConverter != null) {
            factory.setMessageConverter(this.messageConverter);
        }
        JmsProperties.Listener listener = this.jmsProperties.getListener();
        factory.setAutoStartup(listener.isAutoStartup());
        if (listener.getAcknowledgeMode() != null) {
            factory.setSessionAcknowledgeMode(listener.getAcknowledgeMode().getMode());
        }
        String concurrency = listener.formatConcurrency();
        if (concurrency != null) {
            factory.setConcurrency(concurrency);
        }
    }
    
}
