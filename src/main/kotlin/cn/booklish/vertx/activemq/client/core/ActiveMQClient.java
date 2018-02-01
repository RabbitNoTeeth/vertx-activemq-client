package cn.booklish.vertx.activemq.client.core;

import cn.booklish.vertx.activemq.client.consumer.ActiveMQConsumer;
import cn.booklish.vertx.activemq.client.producer.ActiveMQProducer;
import cn.booklish.vertx.activemq.client.subscriber.ActiveMQSubscriber;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;


public interface ActiveMQClient {

    static ActiveMQClient create(Vertx vertx, JsonObject config){
        if(config.getString("username") == null){
            throw new IllegalArgumentException("username can not be null");
        }
        if(config.getString("password") == null){
            throw new IllegalArgumentException("password can not be null");
        }
        if(config.getString("brokerURL") == null){
            throw new IllegalArgumentException("brokerURL can not be null");
        }
        return new ActiveMQClientImpl(vertx,config);
    }

    ActiveMQConsumer createConsumer(String destination);

    ActiveMQSubscriber createSubscriber(String destination);

    ActiveMQProducer createProducer(DestinationType destinationType, String destination);
}
