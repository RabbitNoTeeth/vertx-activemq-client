package cn.booklish.vertx.activemq.client.core

import cn.booklish.vertx.activemq.client.consumer.ActiveMQConsumer
import cn.booklish.vertx.activemq.client.producer.ActiveMQProducer
import cn.booklish.vertx.activemq.client.subscriber.ActiveMQSubscriber
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject


interface ActiveMQClient {

    companion object {
        fun create(vertx: Vertx,config: JsonObject):ActiveMQClient {
            when{
                config.getString("username")==null -> throw IllegalArgumentException("username can not be null")
                config.getString("password")==null -> throw IllegalArgumentException("password can not be null")
                config.getString("brokerURL")==null -> throw IllegalArgumentException("brokerURL can not be null")
                else -> return ActiveMQClientImpl(vertx,config)
            }
        }
    }

    fun createConsumer(destination: String): ActiveMQConsumer

    fun createSubscriber(destination: String): ActiveMQSubscriber

    fun createProducer(destination: String): ActiveMQProducer
}