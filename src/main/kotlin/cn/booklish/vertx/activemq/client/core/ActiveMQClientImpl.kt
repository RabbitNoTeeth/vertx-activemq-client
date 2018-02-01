package cn.booklish.vertx.activemq.client.core

import cn.booklish.vertx.activemq.client.consumer.ActiveMQConsumer
import cn.booklish.vertx.activemq.client.consumer.ActiveMQConsumerImpl
import cn.booklish.vertx.activemq.client.pool.ActiveMQSessionPool
import cn.booklish.vertx.activemq.client.producer.ActiveMQProducer
import cn.booklish.vertx.activemq.client.producer.ActiveMQProducerImpl
import cn.booklish.vertx.activemq.client.subscriber.ActiveMQSubscriber
import cn.booklish.vertx.activemq.client.subscriber.ActiveMQSubscriberImpl
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import org.apache.activemq.ActiveMQConnectionFactory
import javax.jms.Connection


class ActiveMQClientImpl(private val vertx: Vertx, config: JsonObject):ActiveMQClient{

    private val connection: Connection = ActiveMQConnectionFactory(config.getString("username"),
                                            config.getString("password"),config.getString("brokerURL"))
                    .createConnection()

    private val sessionPool = ActiveMQSessionPool(connection,config.getInteger("sessionPoolSize")?:0)

    init {
        connection.clientID = "vertx-activemq-client"
        connection.start()
    }

    override fun createConsumer(destination: String): ActiveMQConsumer {
        return ActiveMQConsumerImpl(vertx,sessionPool.getSession(),destination)
    }

    override fun createSubscriber(destination: String): ActiveMQSubscriber {
        return ActiveMQSubscriberImpl(vertx,sessionPool.getSession(),destination)
    }

    override fun createProducer(destinationType: DestinationType,destination: String): ActiveMQProducer {
        return ActiveMQProducerImpl(vertx,sessionPool.getSession(),destinationType,destination)
    }

}