package cn.booklish.vertx.activemq.client.core

import cn.booklish.vertx.activemq.client.consumer.ActiveMQConsumer
import cn.booklish.vertx.activemq.client.consumer.ActiveMQConsumerImpl
import cn.booklish.vertx.activemq.client.pool.ActiveMQSessionPool
import cn.booklish.vertx.activemq.client.producer.ActiveMQProducer
import cn.booklish.vertx.activemq.client.subscriber.ActiveMQSubscriber
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import org.apache.activemq.ActiveMQConnectionFactory
import javax.jms.Connection


class ActiveMQClientImpl(private val vertx: Vertx, config: JsonObject): ActiveMQClient {

    private val connection: Connection = ActiveMQConnectionFactory(config.getString(""),config.getString(""),config.getString(""))
                    .createConnection()

    private val sessionPool = ActiveMQSessionPool(connection,config.getInteger("sessionPoolSize")?:0)

    init {
        connection.clientID = "vertx-activemq-client"
    }

    override fun createConsumer(destination: String): ActiveMQConsumer {
        return ActiveMQConsumerImpl(vertx,sessionPool.getSession(),destination)
    }

    override fun createSubscriber(destination: String): ActiveMQSubscriber {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createProducer(destination: String): ActiveMQProducer {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}