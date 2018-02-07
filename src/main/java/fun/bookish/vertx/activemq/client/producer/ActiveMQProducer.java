package fun.bookish.vertx.activemq.client.producer;


import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

public interface ActiveMQProducer {

    String getKey();

    void send(JsonObject message);

    void send(JsonObject message,Handler<AsyncResult<Void>> handler);

    void close();

    void close(Handler<AsyncResult<Void>> handler);

}
