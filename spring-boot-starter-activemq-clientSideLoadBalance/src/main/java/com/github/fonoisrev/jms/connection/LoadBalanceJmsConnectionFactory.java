package com.github.fonoisrev.jms.connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.scheduling.annotation.Scheduled;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * JmsConnectionFactory that implement with loadBalance
 *
 * @author fonoisrev
 */
public class LoadBalanceJmsConnectionFactory implements ConnectionFactory {
    
    /* logger */
    Logger log = LoggerFactory.getLogger(LoadBalanceJmsConnectionFactory.class);
    
    /**
     * url -> connectionFactory
     */
    private Map<String, ConnectionFactory> connectionFactoryMap = new ConcurrentHashMap<>();
    
    private List<String> validKeys = new ArrayList<>();
    
    private List<String> invalidKeys = new ArrayList<>();
    
    private Random random = new Random();
    
    private ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    
    
    public LoadBalanceJmsConnectionFactory(SharedMultiConnectionFactory connectionFactoriesStore) {
        Map<String, ConnectionFactory> sharedConnectionFactories =
                connectionFactoriesStore.getConnectionFactories();
        sharedConnectionFactories.forEach((url, factory) -> {
            CachingConnectionFactory cachingConnectionFactory =
                    new CachingConnectionFactory(factory);
            cachingConnectionFactory.setSessionCacheSize(10);
            connectionFactoryMap.put(url, cachingConnectionFactory);
            validKeys.add(url);
        });
    }
    
    @Override
    public Connection createConnection() throws JMSException {
        if (validKeys.size() == 0) {
            throw new JMSException(
                    "Cannot create Connction! There is no available " +
                    "CachingConnectionFactorys.");
        }
        
        readWriteLock.readLock().lock();
        
        // 1.  choose a connectionFactory randomly , and try to get a connection
        int index = random.nextInt(this.validKeys.size());
        ConnectionFactory connFactory = connectionFactoryMap.get(validKeys.get(index));
        Connection connToUse = null;
        
        try {
            connToUse = connFactory.createConnection();
        } catch (JMSException jmse) {
            // 2. exception while getting connection ,the url is invalid
            readWriteLock.readLock().unlock();
            readWriteLock.writeLock().lock();
            // Double Check
            try {
                // 3. double check to ensure index not aut of bounds
                if (index < validKeys.size()) {
                    connFactory = connectionFactoryMap.get(validKeys.get(index));
                    connToUse = connFactory.createConnection();
                }
            } catch (JMSException e) {
                // 4. remove the invalid url and connectionFactory , put them
                // into a invalid list
                String invalidUrl = validKeys.remove(index);
                invalidKeys.add(invalidUrl);
                log.error("Can not establish connection to URL:{} !", invalidUrl);
            } finally {
                readWriteLock.readLock().lock();
                readWriteLock.writeLock().unlock();
            }
            // 5. through step 1,2,3,4 the connToUse still can be null, so
            // check and retry here
            if (connToUse == null) {
                connToUse = this.createConnection();
            }
            
        } finally {
            readWriteLock.readLock().unlock();
        }
        return connToUse;
    }
    
    @Override
    public Connection createConnection(String userName, String password)
            throws JMSException {
        throw new javax.jms.IllegalStateException(
                "LoadBalanceJmsConnectionFactory does not support custom " +
                "username and password");
    }
    
    public Map<String, ConnectionFactory> getAllConnectionFactory() {
        return Collections.unmodifiableMap(connectionFactoryMap);
    }
    
    
    @Scheduled(fixedRate = 300000, initialDelay = 300000)
    public void fixConnectionFactorys() {
        Iterator<String> it = invalidKeys.iterator();
        while (it.hasNext()) {
            String urlToTest = it.next();
            ConnectionFactory connFactoryToTest = connectionFactoryMap.get(urlToTest);
            try {
                Connection connection = connFactoryToTest.createConnection();
                connection.close();
                readWriteLock.writeLock().lock();
                validKeys.add(urlToTest);
                readWriteLock.writeLock().unlock();
                it.remove();
            } catch (JMSException jmse) {
                log.warn("URL:{} is still invaild!", urlToTest);
                continue;
            }
        }
    }
}
