package fun.bookish.vertx.activemq.client.strategy;

import fun.bookish.vertx.activemq.client.core.DestinationType;

import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;

/**
 * 失败策略接口
 **/
public interface CreateFailureStrategy {

    /**
     * 重新尝试创建Queue
     * @param session
     * @param destination
     * @return
     */
    Queue retryCreateQueue(Session session,String destination);

    /**
     * 重新尝试创建Producer
     * @param session
     * @param destinationType
     * @param destination
     * @return
     */
    MessageProducer retryCreateProducer(Session session,DestinationType destinationType,String destination);

    /**
     * 重新尝试创建Topic
     * @param session
     * @param destination
     * @return
     */
    Topic retryCreateTopic(Session session,String destination);

}
