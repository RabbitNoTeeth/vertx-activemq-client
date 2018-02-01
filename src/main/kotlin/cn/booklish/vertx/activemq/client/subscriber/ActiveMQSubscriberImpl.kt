package cn.booklish.vertx.activemq.client.subscriber

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


class ActiveMQSubscriberImpl(private val vertx: Vertx, private val session: Session, destination: String) : ActiveMQSubscriber {

    private val topic = session.createTopic(destination)

    private var subscriberRef: AtomicReference<TopicSubscriber> = AtomicReference()

    override fun listen(messageHandler: Handler<AsyncResult<JsonObject>>) {
        vertx.executeBlocking(Handler<Future<JsonObject>>{
            try{
                val subscriber = this.subscriberRef.get()
                if(subscriber == null){
                    val newSubscriber = session.createDurableSubscriber(topic, getUUID())
                    if(this.subscriberRef.compareAndSet(null,newSubscriber)){
                        newSubscriber.setMessageListener {
                            messageHandler.handle(Future.succeededFuture(JsonObject((it as ActiveMQTextMessage).text)))
                        }
                    }else{
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

    override fun close() {
        val subscriber = this.subscriberRef.get()
        if(subscriber != null){
            if(this.subscriberRef.compareAndSet(subscriber, null)){
                subscriber.close()
            }
        }
    }

    override fun close(handler: Handler<AsyncResult<Void>>) {
        try {
            this.close()
            handler.handle(Future.succeededFuture())
        }catch (e: Exception){
            handler.handle(Future.failedFuture(e))
        }
    }

}