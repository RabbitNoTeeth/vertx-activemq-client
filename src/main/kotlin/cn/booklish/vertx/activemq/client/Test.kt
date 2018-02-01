package cn.booklish.vertx.activemq.client

import org.apache.activemq.ActiveMQConnectionFactory
import javax.jms.Session


fun main(args: Array<String>) {

    val connectionFactory = ActiveMQConnectionFactory("admin","admin","tcp://45.77.6.109:61616")
    val connection = connectionFactory.createConnection()
    connection.clientID = "vertx测试"
    val session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)
    val queue = session.createQueue("vertx-测试-queue")
    val consumer = session.createConsumer(queue)
    consumer.setMessageListener {

    }

    connection.start()

    Thread.sleep(10000)



    val session2 = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)
    val queue2 = session2.createQueue("vertx-测试2-queue")
    val consumer2 = session2.createConsumer(queue2)
    consumer2.setMessageListener {

    }


}
