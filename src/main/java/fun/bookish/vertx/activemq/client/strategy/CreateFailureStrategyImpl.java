package fun.bookish.vertx.activemq.client.strategy;

import fun.bookish.vertx.activemq.client.core.DestinationType;
import fun.bookish.vertx.activemq.client.pool.ActiveMQSessionPool;
import io.vertx.core.json.JsonObject;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQSession;

import javax.jms.*;
import java.time.LocalDateTime;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 失败策略接口实现
 **/
public class CreateFailureStrategyImpl implements CreateFailureStrategy {

    private final ActiveMQConnectionFactory connectionFactory;
    private final ActiveMQSessionPool sessionPool;
    private final AtomicReference<Connection> connectionRef = new AtomicReference<>();
    private final JsonObject config;
    private final int retryTimes;

    public CreateFailureStrategyImpl(ActiveMQConnectionFactory connectionFactory, ActiveMQSessionPool sessionPool,
                                     Connection connection, JsonObject config){
        this.connectionFactory = connectionFactory;
        this.sessionPool = sessionPool;
        this.connectionRef.set(connection);
        this.config = config;
        Integer retryTimes = config.getInteger("CreateFailureStrategy.retryTimes");
        this.retryTimes =  (retryTimes == null || retryTimes < 1)? 3 : retryTimes;
    }

    /**
     * 重新尝试创建Queue
     * @param session
     * @param destination
     * @return
     */
    public Queue retryCreateQueue(Session session,String destination){

        session = checkSession(session);
        if(session == null){
            return null;
        }

        Semaphore access = new Semaphore(this.retryTimes);
        while (true){
            if (access.tryAcquire()){
                try {
                    return session.createQueue(destination);
                }catch (JMSException e){
                    //ignore the exception and continue the loop
                }
            }else {
                return null;
            }
        }
    }

    /**
     * 重新尝试创建Producer
     * @param session
     * @param destinationType
     * @param destination
     * @return
     */
    public MessageProducer retryCreateProducer(Session session,DestinationType destinationType,String destination){

        session = checkSession(session);
        if(session == null){
            return null;
        }

        Semaphore access = new Semaphore(this.retryTimes);
        while (true){
            if (access.tryAcquire()){
                try {
                    switch (destinationType){
                        case QUEUE:
                            return session.createProducer(session.createQueue(destination));
                        case TOPIC:
                            return session.createProducer(session.createTopic(destination));
                    }
                }catch (JMSException e){
                    //ignore the exception and continue the loop
                }
            }else {
                return null;
            }
        }
    }

    /**
     * 重新尝试创建Topic
     * @param session
     * @param destination
     * @return
     */
    public Topic retryCreateTopic(Session session,String destination){


        session = checkSession(session);
        if(session == null){
            return null;
        }

        Semaphore access = new Semaphore(this.retryTimes);
        while (true){
            if (access.tryAcquire()){
                try {
                    return session.createTopic(destination);
                }catch (JMSException e){
                    //ignore the exception and continue the loop
                }
            }else {
                return null;
            }
        }
    }

    private Connection newAndUpdateConnection(Connection old) {
        try {
            Connection newConnection = connectionFactory.createConnection();
            String clientID = config.getString("clientID");
            newConnection.setClientID(clientID==null?"vertx-activemq-client:"+ LocalDateTime.now():clientID);
            newConnection.start();
            if(this.connectionRef.compareAndSet(old,newConnection)){
                return newConnection;
            }else {
                return this.connectionRef.get();
            }
        }catch (JMSException e){
            return null;
        }
    }

    private Session checkSession(Session session){
        Connection connection = this.connectionRef.get();

        if(((ActiveMQConnection)connection).isClosed()){
            Connection newConnection = newAndUpdateConnection(connection);
            if(newConnection == null){
                return null;
            }
            this.sessionPool.setConnection(connection,newConnection);
            session = this.sessionPool.getSession();
        }

        if(((ActiveMQSession)session).isClosed()){
            session = this.sessionPool.getSession();
        }

        return session;
    }

}
