# What i do
Althongh ActiveMQ provide some way (see [this](http://activemq.apache.org/clustering.html)) to build a jms-broker cluster,  unfortunately, there are a lot of limit with the cluster mode, for example, performance problems and linear expansibility problems.
Actually, we have a cheaper way to bring along both performance and expansibility below.
```
                      +------------+
                 +---->   broker   +---+
                 |    +------------+   |
                 |                     |
+------------+   |    +------------+   |   +------------+
|  Producer  +-------->   broker   +------->  Consumer  |
+------------+   |    +------------+   |   +------------+
                 |                     |
                 |    +------------+   |
                 +---->   broker   +---+
                      +------------+
```

In this way , brokers are independent that do not know each other. Producer and consumer run over the client-side load-balance machine.

I implement this client-side load-balance machine base on spring-boot's activemq auto configuration. And make it easy to use, which users just only need to configure the broker's address.


# Usage

1. Import maven dependency as below

   ```xml
   <dependency>
       <groupId>com.github.fonoisrev</groupId>
       <artifactId>spring-boot-starter-activemq-clientSideLoadBalance</artifactId>
       <version>1.0.0</version>
   </dependency>
   ```

   

2. Add broker urls into the configuration file (.properties or .yml) as below 

   ```properties
   activemq.loadbalance.enabled=true
   activemq.loadbalance.urls[0]=tcp://localhost:61616
   activemq.loadbalance.urls[1]=tcp://localhost:61617
   ...(more urls)
   ```

   or

   ```yaml
   activemq:
     loadbalance:
       enabled: true
       urls:
         - tcp://localhost:61616
         - tcp://localhost:61617
         ...(more urls)
   ```

3. Use Spring framework tools **JmsTemplate** or **JmsMessageTemplate**(4.0 and later) to send message. And use **@JmsListener** to handle messages recieved. See Spring's website for more features.


# How To 
In short, I hack the Spring-Boot's autoConfiguration about ActiveMQ, do something before configure the *org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration*.
More details are in the *spring-boot-starter-activemq-clientSideLoadBalance* folder. Or follow with my blog in [here](https://www.jianshu.com/p/3f34ddf62044).

