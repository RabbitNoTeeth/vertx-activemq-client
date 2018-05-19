package fun.bookish.vertx.activemq.client.core;

import fun.bookish.vertx.activemq.client.cache.ActiveMQCacheManager;
import fun.bookish.vertx.activemq.client.constants.ActiveMQClientConstants;
import fun.bookish.vertx.activemq.client.consumer.ActiveMQConsumer;
import fun.bookish.vertx.activemq.client.consumer.ActiveMQConsumerImpl;
import fun.bookish.vertx.activemq.client.pool.ActiveMQSessionPool;
import fun.bookish.vertx.activemq.client.producer.ActiveMQProducer;
import fun.bookish.vertx.activemq.client.producer.ActiveMQProducerImpl;
import fun.bookish.vertx.activemq.client.subscriber.ActiveMQSubscriber;
import fun.bookish.vertx.activemq.client.subscriber.ActiveMQSubscriberImpl;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;


public class ActiveMQClientImpl implements ActiveMQClient {

    private final Vertx vertx;
    private final ActiveMQSessionPool sessionPool;
    private final ActiveMQCacheManager cacheManager;


    ActiveMQClientImpl(Vertx vertx,JsonObject config){
        this.vertx = vertx;
        this.sessionPool = new ActiveMQSessionPool(config);
        this.cacheManager = new ActiveMQCacheManager();
        this.vertx.getOrCreateContext().put(ActiveMQClientConstants.VERTX_CTX_KEY,this);
    }

    @Override
    public ActiveMQConsumer createConsumer(String key, String destination) {
        return new ActiveMQConsumerImpl(key,vertx,sessionPool.getSession(),destination,cacheManager);
    }

    @Override
    public ActiveMQSubscriber createSubscriber(String key, String destination) {
        return new ActiveMQSubscriberImpl(key,vertx,sessionPool.getSession(),destination,cacheManager);
    }

    @Override
    public ActiveMQProducer createProducer(String key, DestinationType destinationType, String destination) {
        return new ActiveMQProducerImpl(key,vertx,sessionPool.getSession(),destinationType,destination,cacheManager);
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

}
