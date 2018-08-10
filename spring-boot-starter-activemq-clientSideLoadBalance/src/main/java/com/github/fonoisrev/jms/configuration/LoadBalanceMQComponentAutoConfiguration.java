package com.github.fonoisrev.jms.configuration;

import com.github.fonoisrev.jms.connection.LoadBalanceJmsConnectionFactory;
import com.github.fonoisrev.jms.connection.SharedMultiConnectionFactory;
import com.github.fonoisrev.jms.container.MultiJmsMessageListenerContainerFactory;
import com.github.fonoisrev.jms.container.MultiJmsMessageListenerContainerFactoryConfigrer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration;
import org.springframework.boot.autoconfigure.jms.JmsProperties;
import org.springframework.boot.autoconfigure.jms.JndiConnectionFactoryAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.destination.DestinationResolver;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.jta.JtaTransactionManager;

import javax.jms.ConnectionFactory;

/**
 * autoconfig class
 *
 * @author fonoisrev
 */
@Configuration
@AutoConfigureBefore({JmsAutoConfiguration.class})
@AutoConfigureAfter({JndiConnectionFactoryAutoConfiguration.class})
@ConditionalOnProperty(name = "activemq.loadbalance.enabled", havingValue = "true")
@EnableConfigurationProperties(LoadBalanceActiveMQProperties.class)
public class LoadBalanceMQComponentAutoConfiguration {
    
    @Bean
    public SharedMultiConnectionFactory sharedMultiConnectionFactory(
            LoadBalanceActiveMQProperties activeMQProperties) {
        return new SharedMultiConnectionFactory(activeMQProperties);
    }
    
    @Primary
    @Bean
    public LoadBalanceJmsConnectionFactory loadBalanceJmsConnectionFactory(
            SharedMultiConnectionFactory connectionFactory) {
        return new LoadBalanceJmsConnectionFactory(connectionFactory);
    }
    
    /**
     * construct a bean named jmsListenerContainerFactoryConfigurer
     */
    @Bean
    public MultiJmsMessageListenerContainerFactoryConfigrer
    multiJmsMessageListenerContainerFactoryConfigrer(
            ObjectProvider<DestinationResolver> destinationResolver,
            ObjectProvider<JtaTransactionManager> transactionManager,
            ObjectProvider<MessageConverter> messageConverter,
            JmsProperties properties) {
        MultiJmsMessageListenerContainerFactoryConfigrer configurer =
                new MultiJmsMessageListenerContainerFactoryConfigrer();
        configurer.setDestinationResolver(destinationResolver.getIfUnique());
        configurer.setTransactionManager(transactionManager.getIfUnique());
        configurer.setMessageConverter(messageConverter.getIfUnique());
        configurer.setJmsProperties(properties);
        return configurer;
    }
    
    /**
     * construct a bean named jmsListenerContainerFactory
     */
    @Bean("jmsListenerContainerFactory")
    public MultiJmsMessageListenerContainerFactory jmsListenerContainerFactory(
            MultiJmsMessageListenerContainerFactoryConfigrer configurer,
            @Qualifier("sharedMultiConnectionFactory") ConnectionFactory connectionFactory) {
        MultiJmsMessageListenerContainerFactory factory =
                new MultiJmsMessageListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        return factory;
    }
    
    @EnableScheduling
    static class EnableSchedulerConfiguration {
    }
}
