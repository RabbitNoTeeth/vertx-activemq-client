package fun.bookish.vertx.activemq.client.core;

import fun.bookish.vertx.activemq.client.consumer.ActiveMQConsumer;
import fun.bookish.vertx.activemq.client.consumer.ActiveMQConsumerImpl;
import fun.bookish.vertx.activemq.client.pool.ActiveMQSessionPool;
import fun.bookish.vertx.activemq.client.producer.ActiveMQProducer;
import fun.bookish.vertx.activemq.client.producer.ActiveMQProducerImpl;
import fun.bookish.vertx.activemq.client.subscriber.ActiveMQSubscriber;
import fun.bookish.vertx.activemq.client.subscriber.ActiveMQSubscriberImpl;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.Connection;
import javax.jms.JMSException;
import java.time.LocalDateTime;


public class ActiveMQClientImpl implements ActiveMQClient {

    private final Vertx vertx;
    private final JsonObject config;
    private Connection connection;
    private ActiveMQSessionPool sessionPool;

    public ActiveMQClientImpl(Vertx vertx,JsonObject config){
        this.vertx = vertx;
        this.config = config;

        try {
            this.connection = new ActiveMQConnectionFactory(config.getString("username"),
                    config.getString("password"),config.getString("brokerURL"))
                    .createConnection();
            String clientID = config.getString("clientID");
            this.connection.setClientID(clientID==null?"vertx-activemq-client:"+ LocalDateTime.now():clientID);
            this.connection.start();
            Integer poolSize = config.getInteger("sessionPoolSize");
            this.sessionPool = new ActiveMQSessionPool(this.connection,poolSize==null?0:poolSize);
        } catch (JMSException e) {
            throw new IllegalArgumentException("failed creating connection for:" + config);
        }
    }

    @Override
    public ActiveMQConsumer createConsumer(String key, String destination) {
        return new ActiveMQConsumerImpl(key,vertx,sessionPool.getSession(),destination);
    }

    @Override
    public ActiveMQSubscriber createSubscriber(String key, String destination) {
        return new ActiveMQSubscriberImpl(key,vertx,sessionPool.getSession(),destination);
    }

    @Override
    public ActiveMQProducer createProducer(String key, DestinationType destinationType, String destination) {
        return new ActiveMQProducerImpl(key,vertx,sessionPool.getSession(),destinationType,destination);
    }
}
