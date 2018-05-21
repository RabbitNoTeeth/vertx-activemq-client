package fun.bookish.vertx.activemq.client.core;

import fun.bookish.vertx.activemq.client.config.ActiveMQOptions;
import fun.bookish.vertx.activemq.client.consumer.ActiveMQConsumer;
import fun.bookish.vertx.activemq.client.producer.ActiveMQProducer;
import fun.bookish.vertx.activemq.client.subscriber.ActiveMQSubscriber;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;


public interface ActiveMQClient {

    static ActiveMQClient create(Vertx vertx, ActiveMQOptions options){
        if(options.getUsername() == null){
            throw new IllegalArgumentException("username不能为null");
        }
        if(options.getPassword() == null){
            throw new IllegalArgumentException("password不能为null");
        }
        if(options.getBroker() == null){
            throw new IllegalArgumentException("broker不能为null");
        }
        return new ActiveMQClientImpl(vertx,options);
    }

    ActiveMQConsumer createConsumer(String key, String destination);

    ActiveMQSubscriber createSubscriber(String key, String destination);

    ActiveMQProducer createProducer(String key, DestinationType destinationType, String destination);

    ActiveMQConsumer getConsumer(String key);

    ActiveMQSubscriber getSubscriber(String key);

    ActiveMQProducer getProducer(String key);

    void clear(String keyPrefix);
}
