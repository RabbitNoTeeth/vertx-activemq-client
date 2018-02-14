package fun.bookish.vertx.activemq.client.consumer;


import fun.bookish.vertx.activemq.client.producer.ActiveMQProducer;
import fun.bookish.vertx.activemq.client.subscriber.ActiveMQSubscriber;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

public interface ActiveMQConsumer {

    String getKey();

    void listen(Handler<AsyncResult<JsonObject>> messageHandler);

    void close();

    void close(Handler<AsyncResult<Void>> handler);

}
