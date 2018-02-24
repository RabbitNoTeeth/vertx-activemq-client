package fun.bookish.vertx.activemq.client.consumer;


import fun.bookish.vertx.activemq.client.cache.ActiveMQCacheManager;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.apache.activemq.command.ActiveMQTextMessage;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;
import java.util.concurrent.atomic.AtomicReference;

public class ActiveMQConsumerImpl implements ActiveMQConsumer {

    private final String key;
    private final Vertx vertx;
    private final Session session;
    private final String destination;
    private final ActiveMQCacheManager cacheManager;
    private final Queue queue;

    public ActiveMQConsumerImpl(String key,Vertx vertx,Session session,String destination,ActiveMQCacheManager cacheManager){
        this.key = key;
        this.vertx = vertx;
        this.session = session;
        this.destination = destination;
        this.cacheManager = cacheManager;
        try {
            this.queue = session.createQueue(destination);
        } catch (JMSException e) {
            throw new IllegalArgumentException("failed creating a queue of destination:"+destination);
        }
    }

    private final AtomicReference<Future<MessageConsumer>> consumerRef = new AtomicReference<>();


    @Override
    public String getKey() {
        return this.key;
    }

    @SuppressWarnings("Duplicates")
    @Override
    public void listen(Handler<AsyncResult<JsonObject>> messageHandler) {
        this.vertx.executeBlocking(res -> {
            try{
                Future<MessageConsumer> future = this.consumerRef.get();
                if(future == null){
                    Future<MessageConsumer> newFuture = Future.future();
                    //根据set结果判断consumerRef是否已被并发更新
                    if(this.consumerRef.compareAndSet(null,newFuture) &&
                            this.cacheManager.cacheConsumer(this)){
                        MessageConsumer newConsumer = session.createConsumer(queue);
                        newFuture.complete(newConsumer);
                        //设置消息监听,将消息传给handler
                        newConsumer.setMessageListener(message -> {
                            try {
                                String msg = ((ActiveMQTextMessage)message).getText();
                                messageHandler.handle(Future.succeededFuture(new JsonObject(msg)));
                            }catch (JMSException e){
                                messageHandler.handle(Future.failedFuture(e));
                            }
                        });
                    }else{
                        //set失败,说明consumerRef已被其他线程更新
                        messageHandler.handle(Future.failedFuture(new IllegalStateException(this.getClass().getTypeName()+":"+this.key+" had started, you should " +
                                "not call this method more than one time!")));
                    }
                }else{
                    messageHandler.handle(Future.failedFuture(new IllegalStateException(future.result() + " had started, you cant " +
                            "not call this method more than one time!")));
                }
            }catch (Exception e){
                messageHandler.handle(Future.failedFuture(e));
            }
        }, null);
    }

    /**
     * 关闭监听
     */
    @Override
    public void close() {
        Future<MessageConsumer> future = this.consumerRef.get();
        if(future != null){
            if(this.consumerRef.compareAndSet(future, null)){
                try {
                    future.result().close();
                } catch (JMSException ignore) {
                    //ignore this exception
                } finally {
                    this.cacheManager.removeConsumer(this.key);
                }
            }
        }
    }

    /**
     * 关闭监听
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
