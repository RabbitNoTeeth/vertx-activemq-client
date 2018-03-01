package fun.bookish.vertx.activemq.client.core;

import fun.bookish.vertx.activemq.client.cache.ActiveMQCacheManager;
import fun.bookish.vertx.activemq.client.config.ActiveMQClientConfigKey;
import fun.bookish.vertx.activemq.client.constants.ActiveMQClientConstants;
import fun.bookish.vertx.activemq.client.consumer.ActiveMQConsumer;
import fun.bookish.vertx.activemq.client.consumer.ActiveMQConsumerImpl;
import fun.bookish.vertx.activemq.client.pool.ActiveMQSessionPool;
import fun.bookish.vertx.activemq.client.producer.ActiveMQProducer;
import fun.bookish.vertx.activemq.client.producer.ActiveMQProducerImpl;
import fun.bookish.vertx.activemq.client.strategy.CreateFailureStrategyImpl;
import fun.bookish.vertx.activemq.client.subscriber.ActiveMQSubscriber;
import fun.bookish.vertx.activemq.client.subscriber.ActiveMQSubscriberImpl;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.apache.activemq.ActiveMQConnectionConsumer;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.Connection;
import javax.jms.JMSException;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;


public class ActiveMQClientImpl implements ActiveMQClient {

    private final Vertx vertx;
    private final JsonObject config;
    private final AtomicReference<Connection> connectionRef = new AtomicReference<>();
    private final ActiveMQSessionPool sessionPool;
    private final ActiveMQCacheManager cacheManager;
    private final CreateFailureStrategyImpl createFailureStrategy;


    public ActiveMQClientImpl(Vertx vertx,JsonObject config){
        try {
            this.vertx = vertx;
            this.config = config;
            ActiveMQConnectionFactory connectionFactory =
                    new ActiveMQConnectionFactory(config.getString(ActiveMQClientConfigKey.USERNAME.value()),
                            config.getString(ActiveMQClientConfigKey.PASSWORD.value()),
                            config.getString(ActiveMQClientConfigKey.BROKER_URL.value()));
            Connection connection = connectionFactory.createConnection();
            String clientID = config.getString(ActiveMQClientConfigKey.CLIENT_ID.value());
            connection.setClientID(clientID==null?"vertx-activemq-client:"+ LocalDateTime.now():clientID);
            connection.start();
            this.connectionRef.set(connection);
            Integer poolSize = config.getInteger(ActiveMQClientConfigKey.SESSION_POOL_SIZE.value());
            this.sessionPool = new ActiveMQSessionPool(connection,poolSize==null?0:poolSize);
            this.cacheManager = new ActiveMQCacheManager();
            this.createFailureStrategy = new CreateFailureStrategyImpl(connectionFactory,this.sessionPool,connection,this.config);
            this.vertx.getOrCreateContext().put(ActiveMQClientConstants.VERTX_CTX_KEY,this);
        } catch (JMSException e) {
            throw new IllegalArgumentException("failed creating connection for:" + config);
        }


    }

    @Override
    public ActiveMQConsumer createConsumer(String key, String destination) {
        return new ActiveMQConsumerImpl(key,vertx,sessionPool.getSession(),destination,cacheManager,createFailureStrategy);
    }

    @Override
    public ActiveMQSubscriber createSubscriber(String key, String destination) {
        return new ActiveMQSubscriberImpl(key,vertx,sessionPool.getSession(),destination,cacheManager,createFailureStrategy);
    }

    @Override
    public ActiveMQProducer createProducer(String key, DestinationType destinationType, String destination) {
        return new ActiveMQProducerImpl(key,vertx,sessionPool.getSession(),destinationType,destination,cacheManager,createFailureStrategy);
    }

    @Override
    public ActiveMQConsumer getConsumer(String key) {
        return this.cacheManager.getConsumer(key);
    }

    @Override
    public ActiveMQSubscriber getSubscriber(String key) {
        return this.cacheManager.getSubscriber(key);
    }

    @Override
    public ActiveMQProducer getProducer(String key) {
        return this.cacheManager.getProducer(key);
    }

    @Override
    public void clear(String keyPrefix) {
        this.cacheManager.clear(keyPrefix);
    }

    public boolean setConnection(Connection old,Connection newOne){
        return this.connectionRef.compareAndSet(old,newOne);
    }
}
