package cn.booklish.vertx.activemq.client.consumer

import cn.booklish.vertx.activemq.client.cache.ActiveMQCacheManager
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import org.apache.activemq.command.ActiveMQTextMessage
import java.util.concurrent.atomic.AtomicReference
import javax.jms.MessageConsumer
import javax.jms.Session

class ActiveMQConsumerImpl(override val key: String, private val vertx: Vertx, private val session: Session, destination: String):ActiveMQConsumer {

    private val queue = session.createQueue(destination)

    private val consumerRef: AtomicReference<MessageConsumer> = AtomicReference()

    /**
     * 开启监听
     */
    override fun listen(messageHandler: Handler<AsyncResult<JsonObject>>) {
        vertx.executeBlocking(Handler<Future<JsonObject>>{
            try{
                val consumer = this.consumerRef.get()
                if(consumer == null){
                    val newConsumer = session.createConsumer(queue)
                    //根据set结果判断consumerRef是否已被并发更新
                    if(this.consumerRef.compareAndSet(null,newConsumer) &&
                        ActiveMQCacheManager.cacheConsumer(this)){
                        //设置消息监听,将消息传给handler
                        newConsumer.setMessageListener {
                            messageHandler.handle(Future.succeededFuture(JsonObject((it as ActiveMQTextMessage).text)))
                        }
                    }else{
                        //set失败,说明consumerRef已被其他线程更新,那么关闭新创建的newConsumer释放资源
                        newConsumer.close()
                        messageHandler.handle(Future.failedFuture(IllegalStateException("${this.javaClass.simpleName}:${this.key} had started, you should " +
                                "not call this method more than one time!")))
                    }
                }else{
                    messageHandler.handle(Future.failedFuture(IllegalStateException("$consumer had started, you should " +
                            "not call this method more than one time!")))
                }
            }catch (e: Exception){
                messageHandler.handle(Future.failedFuture(e))
            }
        }, null)
    }

    /**
     * 关闭监听
     */
    override fun close() {
        val consumer = this.consumerRef.get()
        if(consumer != null){
            if(this.consumerRef.compareAndSet(consumer, null)){
                consumer.close()
                ActiveMQCacheManager.removeConsumer(this.key)
            }
        }
    }

    /**
     * 关闭监听
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