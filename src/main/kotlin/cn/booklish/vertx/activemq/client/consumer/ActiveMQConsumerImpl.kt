package cn.booklish.vertx.activemq.client.consumer

import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import org.apache.activemq.command.ActiveMQTextMessage
import java.util.concurrent.atomic.AtomicReference
import javax.jms.MessageConsumer
import javax.jms.Session

class ActiveMQConsumerImpl(private val vertx: Vertx, private val session: Session, destination: String):ActiveMQConsumer {

    private val queue = session.createQueue(destination)

    private var consumerRef: AtomicReference<MessageConsumer> = AtomicReference()

    override fun listen(messageHandler: Handler<AsyncResult<JsonObject>>) {
        vertx.executeBlocking(Handler<Future<JsonObject>>{
            try{
                val consumer = this.consumerRef.get()
                if(consumer == null){
                    val newConsumer = session.createConsumer(queue)
                    if(this.consumerRef.compareAndSet(null,newConsumer)){
                        newConsumer.setMessageListener {
                            messageHandler.handle(Future.succeededFuture(JsonObject((it as ActiveMQTextMessage).text)))
                        }
                    }else{
                        newConsumer.close()
                        messageHandler.handle(Future.failedFuture(IllegalStateException("${this.consumerRef.get()} had started, you should " +
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

    override fun close() {
        val consumer = this.consumerRef.get()
        if(consumer != null){
            if(this.consumerRef.compareAndSet(consumer, null)){
                consumer.close()
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