package cn.booklish.vertx.activemq.examples;

import cn.booklish.vertx.activemq.client.consumer.ActiveMQConsumer;
import cn.booklish.vertx.activemq.client.core.ActiveMQClient;
import cn.booklish.vertx.activemq.client.core.DestinationType;
import cn.booklish.vertx.activemq.client.producer.ActiveMQProducer;
import cn.booklish.vertx.activemq.client.subscriber.ActiveMQSubscriber;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;


public class JavaExample {

    public static void main(String[] args) {

        Vertx vertx = Vertx.vertx();

        JsonObject config = new JsonObject().put("username","xxx")
                                .put("password","xxx").put("brokerURL","tcp://127.0.0.1:61616");

        // create a client
        ActiveMQClient client = ActiveMQClient.create(vertx,config);

        //------------------ queue ---------------------

        // get a consumer of queue
        ActiveMQConsumer consumer = client.createConsumer("myKey","vertx-test-queue");

        // start the consumer
        consumer.listen(res -> {
            if(res.succeeded()){
                System.out.println("consumer - receive:"+res.result());
            }else{
                res.cause().printStackTrace();
            }
        });

        // get a producer of queue
        ActiveMQProducer producer = client.createProducer("myKey",DestinationType.QUEUE, "vertx-test-queue");

        // send a message
        JsonObject message = new JsonObject().put("msg", "this is a test queue message!");
        producer.send(message,res -> {
            if(res.succeeded()){
                System.out.println("send successful!");
            }else{
                res.cause().printStackTrace();
            }
        });

        //------------------ topic ---------------------

        // get a subscriber of topic
        ActiveMQSubscriber subscriber = client.createSubscriber("myKey","vertx-test-topic");

        // start the consumer
        subscriber.listen(res -> {
            if(res.succeeded()){
                System.out.println("subscriber - receive:"+res.result());
            }else{
                res.cause().printStackTrace();
            }
        });

        // get a producer of topic
        ActiveMQProducer producer2 = client.createProducer("myKey",DestinationType.TOPIC, "vertx-test-topic");

        // send a message
        JsonObject message2 = new JsonObject().put("msg", "this is a test topic message!");
        producer2.send(message2,res -> {
            if(res.succeeded()){
                System.out.println("send successful!");
            }else{
                res.cause().printStackTrace();
            }
        });
    }
}
