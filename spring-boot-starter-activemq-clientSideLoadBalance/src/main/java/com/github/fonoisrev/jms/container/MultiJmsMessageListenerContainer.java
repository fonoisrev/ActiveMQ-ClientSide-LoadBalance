package com.github.fonoisrev.jms.container;

import com.github.fonoisrev.jms.connection.SharedMultiConnectionFactory;
import org.springframework.jms.JmsException;
import org.springframework.jms.config.AbstractJmsListenerContainerFactory;
import org.springframework.jms.config.AbstractJmsListenerEndpoint;
import org.springframework.jms.config.JmsListenerEndpointRegistry;
import org.springframework.jms.config.MethodJmsListenerEndpoint;
import org.springframework.jms.listener.AbstractJmsListeningContainer;
import org.springframework.jms.listener.AbstractMessageListenerContainer;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.destination.DestinationResolver;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.Assert;
import org.springframework.util.ErrorHandler;
import org.springframework.util.backoff.BackOff;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class is a JmsMessageListenerContainer to replace the {@link
 * DefaultMessageListenerContainer}
 * <p>
 * <p>
 * In order to achieve this, functions which belong to a listenerContainer and being called in
 * classes as follow should be re-implement.
 * <p>
 * <p>
 * ListenerContainer's functions has been called in {@link JmsListenerEndpointRegistry}, {@link
 * AbstractJmsListenerContainerFactory}, {@link MultiJmsMessageListenerContainerFactory}, {@link
 * AbstractJmsListenerEndpoint}, {@link MethodJmsListenerEndpoint}
 * <p>
 *
 * @author fonoisrev
 * @see JmsListenerEndpointRegistry
 * @see AbstractJmsListenerContainerFactory
 * @see MultiJmsMessageListenerContainerFactory
 * @see AbstractJmsListenerEndpoint
 * @see MethodJmsListenerEndpoint
 */
// todo
public class MultiJmsMessageListenerContainer extends AbstractMessageListenerContainer {
    
    /**
     * Multi-DefaultMessageListenerContainer holder
     */
    private final Map<String, DefaultMessageListenerContainer> listenerContainers = new HashMap<>();
    
    private Collection<DefaultMessageListenerContainer> getListenerContainers() {
        return Collections.unmodifiableCollection(listenerContainers.values());
    }
    
    @Override
    public void afterPropertiesSet() {
        getListenerContainers().forEach(AbstractJmsListeningContainer::afterPropertiesSet);
    }
    
    @Override
    public void start() throws JmsException {
        getListenerContainers().forEach(DefaultMessageListenerContainer::start);
    }
    
    @Override
    protected void doShutdown() throws JMSException {
    }
    
    @Override
    public void shutdown() throws JmsException {
        getListenerContainers().forEach(lc -> {lc.shutdown();});
    }
    
    @Override
    public void stop() throws JmsException {
        getListenerContainers().forEach(DefaultMessageListenerContainer::stop);
    }
    
    /**
     * Work like {@link JmsListenerEndpointRegistry#stop(Runnable)}
     *
     * @see JmsListenerEndpointRegistry
     */
    @Override
    public void stop(Runnable callback) throws JmsException {
        Collection<DefaultMessageListenerContainer> listenerContainers = getListenerContainers();
        AggregatingCallback aggregatingCallback =
                new AggregatingCallback(listenerContainers.size(), callback);
        listenerContainers.forEach(lc -> lc.stop(aggregatingCallback));
    }
    
    private static class AggregatingCallback implements Runnable {
        
        private final AtomicInteger count;
        
        private final Runnable finishCallback;
        
        public AggregatingCallback(int count, Runnable finishCallback) {
            this.count = new AtomicInteger(count);
            this.finishCallback = finishCallback;
        }
        
        @Override
        public void run() {
            if (this.count.decrementAndGet() == 0) {
                this.finishCallback.run();
            }
        }
    }
    
    /**
     * the function {@link #isRunning()} is final.
     */
    @Override
    protected boolean runningAllowed() {
        return getListenerContainers().stream().anyMatch(AbstractJmsListeningContainer::isRunning);
    }
    
    @Override
    public void destroy() {
        getListenerContainers().forEach(AbstractJmsListeningContainer::destroy);
    }
    
    @Override
    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        if (!(connectionFactory instanceof SharedMultiConnectionFactory)) {
            throw new IllegalArgumentException(
                    "MultiJmsMessageListenerContainer need connectionFactory with type " +
                    "SharedMultiConnectionFactory");
        }
        
        Map<String, ConnectionFactory> allConnectionFactory =
                ((SharedMultiConnectionFactory) connectionFactory).getConnectionFactories();
        allConnectionFactory.forEach((url, factory) -> {
            if (!listenerContainers.containsKey(url)) {
                listenerContainers.put(url, new DefaultMessageListenerContainer());
            }
            DefaultMessageListenerContainer defaultMessageListenerContainer =
                    listenerContainers.get(url);
            defaultMessageListenerContainer.setConnectionFactory(factory);
        });
    }
    
    @Override
    public void setDestinationResolver(DestinationResolver destinationResolver) {
        getListenerContainers().forEach(lc -> lc.setDestinationResolver(destinationResolver));
        super.setDestinationResolver(destinationResolver);
    }
    
    @Override
    public void setErrorHandler(ErrorHandler errorHandler) {
        getListenerContainers().forEach(lc -> lc.setErrorHandler(errorHandler));
        super.setErrorHandler(errorHandler);
    }
    
    @Override
    public void setMessageConverter(MessageConverter messageConverter) {
        getListenerContainers().forEach(lc -> lc.setMessageConverter(messageConverter));
        super.setMessageConverter(messageConverter);
    }
    
    @Override
    public void setSessionTransacted(boolean sessionTransacted) {
        getListenerContainers().forEach(lc -> lc.setSessionTransacted(sessionTransacted));
        super.setSessionTransacted(sessionTransacted);
    }
    
    @Override
    public void setSessionAcknowledgeMode(int sessionAcknowledgeMode) {
        getListenerContainers().forEach(lc -> lc.setSessionAcknowledgeMode(sessionAcknowledgeMode));
        super.setSessionAcknowledgeMode(sessionAcknowledgeMode);
    }
    
    @Override
    public void setPubSubDomain(boolean pubSubDomain) {
        getListenerContainers().forEach(lc -> lc.setPubSubDomain(pubSubDomain));
        super.setPubSubDomain(pubSubDomain);
    }
    
    @Override
    public void setReplyPubSubDomain(boolean replyPubSubDomain) {
        getListenerContainers().forEach(lc -> lc.setReplyPubSubDomain(replyPubSubDomain));
        super.setReplyPubSubDomain(replyPubSubDomain);
    }
    
    @Override
    public void setSubscriptionDurable(boolean subscriptionDurable) {
        getListenerContainers().forEach(lc -> lc.setSubscriptionDurable(subscriptionDurable));
        super.setSubscriptionDurable(subscriptionDurable);
    }
    
    @Override
    public void setSubscriptionShared(boolean subscriptionShared) {
        getListenerContainers().forEach(lc -> lc.setSubscriptionShared(subscriptionShared));
        super.setSubscriptionShared(subscriptionShared);
    }
    
    @Override
    public void setClientId(String clientId) {
        getListenerContainers().forEach(lc -> lc.setClientId(clientId));
        super.setClientId(clientId);
    }
    
    @Override
    public void setPhase(int phase) {
        getListenerContainers().forEach(lc -> lc.setPhase(phase));
        super.setPhase(phase);
    }
    
    @Override
    public void setAutoStartup(boolean autoStartup) {
        getListenerContainers().forEach(lc -> lc.setAutoStartup(autoStartup));
        super.setAutoStartup(autoStartup);
    }
    
    // TODO
    public void setTaskExecutor(Executor taskExecutor) {
        getListenerContainers().forEach(lc -> lc.setTaskExecutor(taskExecutor));
    }
    
    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        getListenerContainers().forEach(lc -> lc.setTransactionManager(transactionManager));
    }
    
    public void setCacheLevel(int cacheLevel) {
        getListenerContainers().forEach(lc -> lc.setCacheLevel(cacheLevel));
    }
    
    public void setCacheLevelName(String cacheLevelName) throws IllegalArgumentException {
        getListenerContainers().forEach(lc -> lc.setCacheLevelName(cacheLevelName));
    }
    
    @Override
    public void setConcurrency(String concurrency) {
        getListenerContainers().forEach(lc -> lc.setConcurrency(concurrency));
    }
    
    public void setMaxMessagesPerTask(int maxMessagesPerTask) {
        getListenerContainers().forEach(lc -> lc.setMaxMessagesPerTask(maxMessagesPerTask));
    }
    
    public void setReceiveTimeout(long receiveTimeout) {
        getListenerContainers().forEach(lc -> lc.setReceiveTimeout(receiveTimeout));
    }
    
    public void setBackOff(BackOff backOff) {
        getListenerContainers().forEach(lc -> lc.setBackOff(backOff));
    }
    
    public void setRecoveryInterval(long recoveryInterval) {
        getListenerContainers().forEach(lc -> lc.setRecoveryInterval(recoveryInterval));
    }
    
    @Override
    public void setDestinationName(String destinationName) {
        getListenerContainers().forEach(lc -> lc.setDestinationName(destinationName));
        super.setDestinationName(destinationName);
    }
    
    @Override
    public void setSubscriptionName(String subscriptionName) {
        getListenerContainers().forEach(lc -> lc.setSubscriptionName(subscriptionName));
        super.setSubscriptionName(subscriptionName);
    }
    
    @Override
    public void setMessageSelector(String messageSelector) {
        getListenerContainers().forEach(lc -> lc.setMessageSelector(messageSelector));
        super.setMessageSelector(messageSelector);
    }
    
    @Override
    public void setupMessageListener(Object messageListener) {
        getListenerContainers().forEach(lc -> lc.setupMessageListener(messageListener));
        super.setupMessageListener(messageListener);
    }
    
    @Override
    public void initialize() {
        getListenerContainers().forEach(lc -> lc.initialize());
    }
    
    
    @Override
    protected boolean sharedConnectionEnabled() {
        return false;
    }
    
    @Override
    protected void doInitialize() throws JMSException {
    }
    
    
}
