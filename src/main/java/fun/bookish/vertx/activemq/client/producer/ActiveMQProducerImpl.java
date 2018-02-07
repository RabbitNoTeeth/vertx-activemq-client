package fun.bookish.vertx.activemq.client.producer;


import fun.bookish.vertx.activemq.client.cache.ActiveMQCacheManager;
import fun.bookish.vertx.activemq.client.core.DestinationType;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.apache.activemq.command.ActiveMQTextMessage;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;

public class ActiveMQProducerImpl implements ActiveMQProducer{

    private final String key;
    private final Vertx vertx;
    private final Session session;
    private final DestinationType destinationType;
    private final String destination;
    private MessageProducer producer;

    public ActiveMQProducerImpl(String key,Vertx vertx,Session session,DestinationType destinationType,String destination){
        this.key = key;
        this.vertx = vertx;
        this.session = session;
        this.destinationType = destinationType;
        this.destination = destination;

        try {

            switch (destinationType){
                case QUEUE:
                    this.producer = session.createProducer(session.createQueue(destination));
                    break;
                case TOPIC:
                    this.producer = session.createProducer(session.createTopic(destination));
                    break;
            }

            ActiveMQCacheManager.cacheProducer(this);

        }catch (JMSException e){
            throw new IllegalArgumentException("failed creating a destination for:"+destination);
        }


    }


    @Override
    public String getKey() {
        return this.key;
    }

    /**
     * 发送消息
     */
    @Override
    public void send(JsonObject message) {
        this.send(message,null);
    }

    /**
     * 发送消息
     */
    @Override
    public void send(JsonObject message, Handler<AsyncResult<Void>> handler) {
        vertx.executeBlocking(future -> {
            try{
                ActiveMQTextMessage textMessage = new ActiveMQTextMessage();
                textMessage.setText(message.toString());
                this.producer.send(textMessage);
                future.complete();
            }catch (Exception e){
                future.fail(e);
            }
        },handler);
    }


    /**
     * 关闭消息生产者
     */
    @Override
    public void close() {
        try {
            this.producer.close();
        } catch (JMSException ignore) {
            //ignore this exception
        }finally {
            ActiveMQCacheManager.removeProducer(this.key);
        }
    }

    /**
     * 关闭消息生产者
     */
    @Override
    public void close(Handler<AsyncResult<Void>> handler) {
        try {
            this.close();
            handler.handle(Future.succeededFuture());
        }catch (Exception e){
            handler.handle(Future.failedFuture(e));
        }
    }
}
