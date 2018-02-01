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

    private var consumer: AtomicReference<MessageConsumer> = AtomicReference()

    override fun listen(messageHandler: Handler<AsyncResult<JsonObject>>) {
        vertx.executeBlocking(Handler{ future ->
            try{
                val consumer = this.consumer.get()
                if(consumer == null){
                    val newConsumer = session.createConsumer(queue)
                    this.consumer.compareAndSet(null,newConsumer)
                    newConsumer.setMessageListener {
                        when(it){
                            is ActiveMQTextMessage -> future.complete(JsonObject.mapFrom(it.text))
                            else -> future.fail(IllegalStateException("only support ActiveMQTextMessage type"))
                        }
                    }
                }
            }catch (e: Exception){
                future.fail(e)
            }
        }, messageHandler)
    }

    override fun close() {
        val consumer = this.consumer.get()
        if(consumer != null){
            consumer.close()
            this.consumer.compareAndSet(consumer, null)
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