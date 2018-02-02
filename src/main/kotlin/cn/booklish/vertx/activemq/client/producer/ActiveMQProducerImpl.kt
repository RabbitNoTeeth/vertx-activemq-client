package cn.booklish.vertx.activemq.client.producer

import cn.booklish.vertx.activemq.client.cache.ActiveMQCacheManager
import cn.booklish.vertx.activemq.client.core.DestinationType
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import org.apache.activemq.command.ActiveMQTextMessage
import java.util.concurrent.atomic.AtomicReference
import javax.jms.MessageProducer
import javax.jms.Session


class ActiveMQProducerImpl(override val key:String, private val vertx: Vertx, session: Session, destinationType: DestinationType, destination: String):ActiveMQProducer {

    private val destinationBean = when(destinationType){
        DestinationType.QUEUE -> session.createQueue(destination)
        DestinationType.TOPIC -> session.createTopic(destination)
    }

    private val producer:MessageProducer

    init {
        producer = session.createProducer(destinationBean)
        //放入缓存
        ActiveMQCacheManager.cacheProducer(this)
    }

    /**
     * 发送消息
     */
    override fun send(message: JsonObject) {
        this.send(message,null)
    }

    /**
     * 发送消息
     */
    override fun send(message: JsonObject, handler: Handler<AsyncResult<Void>>?) {
        vertx.executeBlocking(Handler { future ->
            try{
                val textMessage = ActiveMQTextMessage()
                textMessage.text = message.toString()
                this.producer.send(textMessage)
                future.complete()
            }catch (e:Exception){
                future.fail(e)
            }
        },handler)
    }

    /**
     * 关闭生产者
     */
    override fun close() {
        this.producer.close()
    }

    /**
     * 关闭生产者
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