package fun.bookish.vertx.activemq.examples;

import fun.bookish.vertx.activemq.client.config.ActiveMQOptions;
import fun.bookish.vertx.activemq.client.consumer.ActiveMQConsumer;
import fun.bookish.vertx.activemq.client.core.ActiveMQClient;
import fun.bookish.vertx.activemq.client.core.DestinationType;
import fun.bookish.vertx.activemq.client.producer.ActiveMQProducer;
import fun.bookish.vertx.activemq.client.subscriber.ActiveMQSubscriber;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;


public class JavaExample {

    public static void main(String[] args) {

        Vertx vertx = Vertx.vertx();

        //配置active基础属性
        ActiveMQOptions options = new ActiveMQOptions().setUsername("xxx").setPassword("xxx").setBroker("tcp://127.0.0.1:61616");

        /**
         * 配置扩展属性
         * 1.配置session连接池大小（默认大小为3）
         *      blog.bookish.main.config.put(ActiveMQClientConfigKey.SESSION_POOL_SIZE.value(),10);
         * 2.配置重连次数（应用在运行过程中，如果与active节点异常中断，
         *   那么在下一次创建消息消费者/监听者或者生产者时将尝试重新连接）
         *      blog.bookish.main.config.put(ActiveMQClientConfigKey.RETRY_TIMES.value(),5);
         */


        // 创建客户端（create a client）
        ActiveMQClient client = ActiveMQClient.create(vertx,options);

        /*---------------------------- queue ----------------------------*/

        // 创建queue的消费者（create a consumer of a queue）
        ActiveMQConsumer consumer = client.createConsumer("myKey","vertx-test-queue");

        // 启动消费者监听（start the consumer）
        consumer.listen(res -> {
            if(res.succeeded()){
                System.out.println("consumer - receive:"+res.result());
            }else{
                res.cause().printStackTrace();
            }
        });

        // 创建queue的消息生产者（create a producer of a queue）
        ActiveMQProducer producer = client.createProducer("myKey", DestinationType.QUEUE, "vertx-test-queue");

        // 发送消息（send a message）
        JsonObject message = new JsonObject().put("msg", "this is a test queue message!");
        producer.send(message,res -> {
            if(res.succeeded()){
                System.out.println("send successful!");
            }else{
                res.cause().printStackTrace();
            }
        });

        /*---------------------------- topic ----------------------------*/

        // 创建消息订阅者（create a subscriber of topic）
        ActiveMQSubscriber subscriber = client.createSubscriber("myKey","vertx-test-topic");

        // 启动订阅者监听（start the subscriber）
        subscriber.listen(res -> {
            if(res.succeeded()){
                System.out.println("subscriber - receive:"+res.result());
            }else{
                res.cause().printStackTrace();
            }
        });

        // 创建topic的消息生产者（create a producer of topic）
        ActiveMQProducer producer2 = client.createProducer("myKey",DestinationType.TOPIC, "vertx-test-topic");

        // 发送消息（send a message）
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
