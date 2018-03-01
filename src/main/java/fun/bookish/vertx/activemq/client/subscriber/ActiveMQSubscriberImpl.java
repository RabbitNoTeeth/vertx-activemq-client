package fun.bookish.vertx.activemq.client.subscriber;

import fun.bookish.vertx.activemq.client.cache.ActiveMQCacheManager;
import fun.bookish.vertx.activemq.client.strategy.CreateFailureStrategyImpl;
import fun.bookish.vertx.activemq.client.util.ExtUtils;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.apache.activemq.command.ActiveMQTextMessage;

import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;
import java.util.concurrent.atomic.AtomicReference;


public class ActiveMQSubscriberImpl implements ActiveMQSubscriber {

    private final String key;
    private final Vertx vertx;
    private final ActiveMQCacheManager cacheManager;
    private final Session session;
    private final String destination;
    private final CreateFailureStrategyImpl createFailureStrategy;
    private Topic topic;

    public ActiveMQSubscriberImpl(String key,Vertx vertx,Session session,String destination,
                                  ActiveMQCacheManager cacheManager,CreateFailureStrategyImpl createFailureStrategy){
        this.key = key;
        this.vertx = vertx;
        this.session = session;
        this.destination = destination;
        this.cacheManager = cacheManager;
        this.createFailureStrategy = createFailureStrategy;
        try {
            this.topic = session.createTopic(destination);
        }catch (JMSException e){
            this.topic = this.createFailureStrategy.retryCreateTopic(this.session,this.destination);
            if(this.topic == null){
                throw new IllegalArgumentException("failed creating a topic of destination:"+destination);
            }
        }
    }

    private final AtomicReference<Future<TopicSubscriber>> subscriberRef = new AtomicReference<>();

    @Override
    public String getKey() {
        return this.key;
    }

    /**
     * 开启订阅监听
     */
    @SuppressWarnings("Duplicates")
    @Override
    public void listen(Handler<AsyncResult<JsonObject>> messageHandler) {
        this.vertx.executeBlocking(res -> {
        try{
            Future<TopicSubscriber> future = this.subscriberRef.get();
            if(future == null){
                Future<TopicSubscriber> newFuture = Future.future();
                //根据set结果判断subscriberRef是否已被并发更新
                if(this.subscriberRef.compareAndSet(null,newFuture) &&
                        this.cacheManager.cacheSubscriber(this)){
                    TopicSubscriber newSubscriber = session.createDurableSubscriber(topic, this.key);
                    newFuture.complete(newSubscriber);
                    //设置消息监听,将消息传给handler
                    newSubscriber.setMessageListener( message -> {
                        try {
                            String msg = ((ActiveMQTextMessage)message).getText();
                            messageHandler.handle(Future.succeededFuture(new JsonObject(msg)));
                        }catch (JMSException e){
                            messageHandler.handle(Future.failedFuture(e));
                        }
                    });
                }else{
                    //set失败,说明subscriberRef已被其他线程更新
                    messageHandler.handle(Future.failedFuture(new IllegalStateException(this.getClass().getTypeName()+":"+this.key+" had started, you cant " +
                            "not call this method more than one time!")));
                }
            }else{
                messageHandler.handle(Future.failedFuture(new IllegalStateException(future.result() + " had started, you should " +
                        "not call this method more than one time!")));
            }
        }catch (Exception e){
            messageHandler.handle(Future.failedFuture(e));
        }
        }, null);
    }

    /**
     * 关闭topic监听
     */
    @Override
    public void close() {
        Future<TopicSubscriber> future = this.subscriberRef.get();
        if(future != null){
            if(this.subscriberRef.compareAndSet(future, null)){
                try {
                    future.result().close();
                } catch (JMSException ignore) {
                    //ignore this exception
                } finally {
                    this.cacheManager.removeSubscriber(this.key);
                }
            }
        }
    }

    /**
     * 关闭topic监听
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
