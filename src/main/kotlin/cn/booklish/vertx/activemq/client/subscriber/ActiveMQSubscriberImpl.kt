package cn.booklish.vertx.activemq.client.subscriber

import cn.booklish.vertx.activemq.client.cache.ActiveMQCacheManager
import cn.booklish.vertx.activemq.client.util.getUUID
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import org.apache.activemq.command.ActiveMQTextMessage
import java.util.concurrent.atomic.AtomicReference
import javax.jms.Session
import javax.jms.TopicSubscriber


class ActiveMQSubscriberImpl(override val key:String, private val vertx: Vertx, private val session: Session, destination: String) : ActiveMQSubscriber {

    private val topic = session.createTopic(destination)

    private var subscriberRef: AtomicReference<TopicSubscriber> = AtomicReference()

    /**
     * 开启订阅监听
     */
    override fun listen(messageHandler: Handler<AsyncResult<JsonObject>>) {
        vertx.executeBlocking(Handler<Future<JsonObject>>{
            try{
                val subscriber = this.subscriberRef.get()
                if(subscriber == null){
                    val newSubscriber = session.createDurableSubscriber(topic, getUUID())
                    //根据set结果判断subscriberRef是否已被并发更新
                    if(this.subscriberRef.compareAndSet(null,newSubscriber)){
                        //set成功
                        //加入缓存管理器
                        ActiveMQCacheManager.cacheSubscriber(this)
                        //设置消息监听,将消息传给handler
                        newSubscriber.setMessageListener {
                            messageHandler.handle(Future.succeededFuture(JsonObject((it as ActiveMQTextMessage).text)))
                        }
                    }else{
                        //set失败,说明subscriberRef已被其他线程更新,那么关闭新创建的newSubscriber释放资源
                        newSubscriber.close()
                        messageHandler.handle(Future.failedFuture(IllegalStateException("${this.subscriberRef.get()} had started, you should " +
                                "not call this method more than one time!")))
                    }
                }else{
                    messageHandler.handle(Future.failedFuture(IllegalStateException("$subscriber had started, you should " +
                            "not call this method more than one time!")))
                }
            }catch (e: Exception){
                messageHandler.handle(Future.failedFuture(e))
            }
        }, null)
    }

    /**
     * 关闭订阅
     */
    override fun close() {
        val subscriber = this.subscriberRef.get()
        if(subscriber != null){
            if(this.subscriberRef.compareAndSet(subscriber, null)){
                subscriber.close()
                ActiveMQCacheManager.removeSubscriber(this.key)
            }
        }
    }

    /**
     * 关闭订阅
     */
    override fun close(handler: Handler<AsyncResult<Void>>) {
        try {
            this.close()
            handler.handle(Future.succeededFuture())
        }catch (e: Exception){
            handler.handle(Future.failedFuture(e))
        }
    }

}