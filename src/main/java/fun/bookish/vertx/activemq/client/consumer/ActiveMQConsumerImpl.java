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

    private final Queue queue;

    public ActiveMQConsumerImpl(String key,Vertx vertx,Session session,String destination){
        this.key = key;
        this.vertx = vertx;
        this.session = session;
        this.destination = destination;
        try {
            this.queue = session.createQueue(destination);
        } catch (JMSException e) {
            throw new IllegalArgumentException("failed creating a queue of destination:"+destination);
        }
    }

    private final AtomicReference<MessageConsumer> consumerRef = new AtomicReference<>();


    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public void listen(Handler<AsyncResult<JsonObject>> messageHandler) {
        this.vertx.executeBlocking(res -> {
            try{
                MessageConsumer consumer = this.consumerRef.get();
                if(consumer == null){
                    MessageConsumer newConsumer = session.createConsumer(queue);
                    //根据set结果判断consumerRef是否已被并发更新
                    if(this.consumerRef.compareAndSet(null,newConsumer) &&
                            ActiveMQCacheManager.cacheConsumer(this)){
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
                        //set失败,说明consumerRef已被其他线程更新,那么关闭新创建的newConsumer释放资源
                        newConsumer.close();
                        messageHandler.handle(Future.failedFuture(new IllegalStateException("${this.javaClass.simpleName}:${this.key} had started, you should " +
                                "not call this method more than one time!")));
                    }
                }else{
                    messageHandler.handle(Future.failedFuture(new IllegalStateException("$consumer had started, you should " +
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
        MessageConsumer consumer = this.consumerRef.get();
        if(consumer != null){
            if(this.consumerRef.compareAndSet(consumer, null)){
                try {
                    consumer.close();
                } catch (JMSException ignore) {
                    //ignore this exception
                } finally {
                    ActiveMQCacheManager.removeConsumer(this.key);
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