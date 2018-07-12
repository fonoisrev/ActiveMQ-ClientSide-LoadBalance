package com.github.fonoisrev.jms.container;

import com.github.fonoisrev.jms.configuration.LoadBalanceActiveMQProperties;
import org.springframework.jms.config.AbstractJmsListenerContainerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.backoff.BackOff;

import java.util.concurrent.Executor;

/**
 * The factory class of {@code MultiJmsMessageListenerContainer}
 *
 * @author fonoisrev
 * @see MultiJmsMessageListenerContainer
 */
// todo
public class MultiJmsMessageListenerContainerFactory
        extends AbstractJmsListenerContainerFactory<MultiJmsMessageListenerContainer> {
    
    private Executor taskExecutor;
    
    private PlatformTransactionManager transactionManager;
    
    private Integer cacheLevel;
    
    private String cacheLevelName;
    
    private String concurrency;
    
    private Integer maxMessagesPerTask;
    
    private Long receiveTimeout;
    
    private Long recoveryInterval;
    
    private BackOff backOff;
    
    private LoadBalanceActiveMQProperties loadBalanceActiveMQProperties;
    
    public MultiJmsMessageListenerContainerFactory(LoadBalanceActiveMQProperties properties) {
        this.loadBalanceActiveMQProperties = properties;
    }
    
    /**
     * @see MultiJmsMessageListenerContainer#setTaskExecutor
     */
    public void setTaskExecutor(Executor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }
    
    /**
     * @see MultiJmsMessageListenerContainer#setTransactionManager
     */
    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }
    
    /**
     * @see MultiJmsMessageListenerContainer#setCacheLevel
     */
    public void setCacheLevel(Integer cacheLevel) {
        this.cacheLevel = cacheLevel;
    }
    
    /**
     * @see MultiJmsMessageListenerContainer#setCacheLevelName
     */
    public void setCacheLevelName(String cacheLevelName) {
        this.cacheLevelName = cacheLevelName;
    }
    
    /**
     * @see MultiJmsMessageListenerContainer#setConcurrency
     */
    public void setConcurrency(String concurrency) {
        this.concurrency = concurrency;
    }
    
    /**
     * @see MultiJmsMessageListenerContainer#setMaxMessagesPerTask
     */
    public void setMaxMessagesPerTask(Integer maxMessagesPerTask) {
        this.maxMessagesPerTask = maxMessagesPerTask;
    }
    
    /**
     * @see MultiJmsMessageListenerContainer#setReceiveTimeout
     */
    public void setReceiveTimeout(Long receiveTimeout) {
        this.receiveTimeout = receiveTimeout;
    }
    
    /**
     * @see MultiJmsMessageListenerContainer#setRecoveryInterval
     */
    public void setRecoveryInterval(Long recoveryInterval) {
        this.recoveryInterval = recoveryInterval;
    }
    
    /**
     * @see MultiJmsMessageListenerContainer#setBackOff
     */
    public void setBackOff(BackOff backOff) {
        this.backOff = backOff;
    }
    
    
    @Override
    protected void initializeContainer(MultiJmsMessageListenerContainer container) {
        if (this.taskExecutor != null) {
            container.setTaskExecutor(this.taskExecutor);
        }
        if (this.transactionManager != null) {
            container.setTransactionManager(this.transactionManager);
        }
        
        if (this.cacheLevel != null) {
            container.setCacheLevel(this.cacheLevel);
        } else if (this.cacheLevelName != null) {
            container.setCacheLevelName(this.cacheLevelName);
        }
        
        if (this.concurrency != null) {
            container.setConcurrency(this.concurrency);
        }
        if (this.maxMessagesPerTask != null) {
            container.setMaxMessagesPerTask(this.maxMessagesPerTask);
        }
        if (this.receiveTimeout != null) {
            container.setReceiveTimeout(this.receiveTimeout);
        }
        
        if (this.backOff != null) {
            container.setBackOff(this.backOff);
            if (this.recoveryInterval != null) {
                logger.warn(
                        "Ignoring recovery interval in DefaultJmsListenerContainerFactory in " +
                        "favor of BackOff");
            }
        } else if (this.recoveryInterval != null) {
            container.setRecoveryInterval(this.recoveryInterval);
        }
    }
    
    
    @Override
    protected MultiJmsMessageListenerContainer createContainerInstance() {
        return new MultiJmsMessageListenerContainer(this.loadBalanceActiveMQProperties);
    }
    
}
