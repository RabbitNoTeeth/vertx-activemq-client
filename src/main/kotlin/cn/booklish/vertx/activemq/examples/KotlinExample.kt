package cn.booklish.vertx.activemq.examples

import cn.booklish.vertx.activemq.client.core.ActiveMQClient
import cn.booklish.vertx.activemq.client.core.DestinationType
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject


fun main(args: Array<String>) {

    val config = JsonObject().apply {
        put("username","xxx")
        put("password","xxx")
        put("brokerURL","tcp://127.0.0.1:61616")
    }

    val vertx = Vertx.vertx()

    // create a client
    val activemqClient = ActiveMQClient.create(vertx,config)

    //------------------ queue ---------------------

    // get a consumer of queue
    val consumer = activemqClient.createConsumer("myKey","vertx-test-queue")

    // start the consumer
    consumer.listen(Handler {
        if(it.succeeded()){
            println("consumer - receive:"+it.result())
        }else{
            it.cause().printStackTrace()
        }
    })

    // get a producer of queue
    val producer = activemqClient.createProducer("myKey",DestinationType.QUEUE,"vertx-test-queue")

    // send a message
    val message = JsonObject().put("msg", "this is a test queue message!")
    producer.send(message, Handler {
        if (it.succeeded()) {
            println("send successful!")
        } else {
            it.cause().printStackTrace()
        }
    })

    //------------------ topic ---------------------

    // get a subscriber of topic
    val subscriber = activemqClient.createSubscriber("myKey","vertx-test-topic")

    // start the consumer
    subscriber.listen(Handler {
        if(it.succeeded()){
            println("subscriber - receive:"+it.result())
        }else{
            it.cause().printStackTrace()
        }
    })

    // get a producer of topic
    val producer2 = activemqClient.createProducer("myKey",DestinationType.TOPIC,"vertx-test-topic")

    // send a message
    val message2 = JsonObject().put("msg", "this is a test topic message!")
    producer2.send(message2, Handler {
        if (it.succeeded()) {
            println("send successful!")
        } else {
            it.cause().printStackTrace()
        }
    })






}
