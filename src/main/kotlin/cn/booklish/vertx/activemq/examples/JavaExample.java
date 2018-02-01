package cn.booklish.vertx.activemq.examples;

import cn.booklish.vertx.activemq.client.consumer.ActiveMQConsumer;
import cn.booklish.vertx.activemq.client.core.ActiveMQClient;
import cn.booklish.vertx.activemq.client.core.DestinationType;
import cn.booklish.vertx.activemq.client.producer.ActiveMQProducer;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * @author Don9
 * @create 2018-02-01-13:39
 **/
public class JavaExample {

    public static void main(String[] args) {

        Vertx vertx = Vertx.vertx();

        JsonObject config = new JsonObject().put("username","xxx")
                                .put("password","xxx").put("brokerURL","tcp://127.0.0.1:61616");

        // create a client
        ActiveMQClient client = ActiveMQClient.create(vertx,config);

        // get a consumer of queue
        ActiveMQConsumer consumer = client.createConsumer("vertx-test-queue");

        // start the consumer
        consumer.listen(res -> {
            if(res.succeeded()){
                System.out.println("consumer - receive:"+res.result());
            }else{
                res.cause().printStackTrace();
            }
        });

        // get a producer
        ActiveMQProducer producer = client.createProducer(DestinationType.QUEUE, "vertx-test-queue");

        // send a message
        JsonObject message = new JsonObject().put("msg", "this is a test message!");
        producer.send(message,res -> {
            if(res.succeeded()){
                System.out.println("send successful!");
            }else{
                res.cause().printStackTrace();
            }
        });
    }
}
