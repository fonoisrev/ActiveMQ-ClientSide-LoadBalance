package com.github.fonoisrev.jms.connection;

import com.github.fonoisrev.jms.configuration.LoadBalanceActiveMQProperties;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.jms.connection.SingleConnectionFactory;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SharedMultiConnectionFactory implements ConnectionFactory {
    
    /**
     * url -> connectionFactory
     */
    private Map<String, ConnectionFactory> connectionFactories = new HashMap<>();
    
    public SharedMultiConnectionFactory(LoadBalanceActiveMQProperties properties) {
        List<String> urls = properties.getUrls();
        urls.forEach(url -> {
            ConnectionFactory factory =
                    new SingleConnectionFactory(new ActiveMQConnectionFactory(url));
            connectionFactories.put(url, factory);
        });
    }
    
    public Map<String, ConnectionFactory> getConnectionFactories() {
        return Collections.unmodifiableMap(connectionFactories);
    }
    
    @Override
    public Connection createConnection() throws JMSException {
        throw new UnsupportedOperationException(
                "SharedMultiConnectionFactory does not support createConnection");
    }
    
    @Override
    public Connection createConnection(String userName, String password) throws JMSException {
        throw new UnsupportedOperationException(
                "SharedMultiConnectionFactory does not support createConnection");
    }
}
