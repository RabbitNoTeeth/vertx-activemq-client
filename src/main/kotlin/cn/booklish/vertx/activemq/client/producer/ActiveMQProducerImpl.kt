package cn.booklish.vertx.activemq.client.producer

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


class ActiveMQProducerImpl(private val vertx: Vertx, private val session: Session, destinationType: DestinationType, destination: String):ActiveMQProducer {

    private val destination = when(destinationType){
        DestinationType.QUEUE -> session.createQueue(destination)
        DestinationType.TOPIC -> session.createTopic(destination)
    }

    private val producer = AtomicReference<MessageProducer>()

    override fun send(message: JsonObject) {
        this.send(message,null)
    }

    override fun send(message: JsonObject, handler: Handler<AsyncResult<Void>>?) {
        vertx.executeBlocking(Handler { future ->
            try{
                var producer = this.producer.get()
                if(producer == null){
                    val newProducer = session.createProducer(destination)
                    if(this.producer.compareAndSet(null,newProducer)){
                        producer = newProducer
                    }else{
                        newProducer.close()
                    }
                }
                val textMessage = ActiveMQTextMessage()
                textMessage.text = message.toString()
                producer.send(textMessage)
                future.complete()
            }catch (e:Exception){
                future.fail(e)
            }
        },handler)
    }

    override fun close() {
        val producer = this.producer.get()
        if(producer != null){
            producer.close()
            this.producer.compareAndSet(producer, null)
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