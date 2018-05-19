package fun.bookish.vertx.activemq.client.pool;



import fun.bookish.vertx.activemq.client.config.ActiveMQClientConfigKey;
import io.vertx.core.json.JsonObject;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Session;
import java.time.LocalDateTime;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;

public class ActiveMQSessionPool {

    private static final Logger logger = LoggerFactory.getLogger(ActiveMQSessionPool.class);

    private final ActiveMQConnectionFactory connectionFactory;
    private final AtomicReference<Connection> connection = new AtomicReference<>(null);
    private final JsonObject config;
    private AtomicReference<Session> session = new AtomicReference<>(null);
    private final int retryTimes;

    public ActiveMQSessionPool(JsonObject config) {

        this.config = config;

        Integer retryTimes = config.getInteger(ActiveMQClientConfigKey.RETRY_TIMES.value());
        this.retryTimes =  (retryTimes == null || retryTimes < 1)? 5 : retryTimes;

        this.connectionFactory = new ActiveMQConnectionFactory(
                        config.getString(ActiveMQClientConfigKey.USERNAME.value()),
                        config.getString(ActiveMQClientConfigKey.PASSWORD.value()),
                        config.getString(ActiveMQClientConfigKey.BROKER_URL.value())
        );
        Connection connection;

        try {
            connection = connectionFactory.createConnection();
            String clientID = config.getString(ActiveMQClientConfigKey.CLIENT_ID.value());
            connection.setClientID(clientID==null?"vertx-activemq-client:"+ LocalDateTime.now():clientID);
            connection.start();
            this.connection.set(connection);
            createSession();
            logger.info("ActiveMQ连接成功,broker=" + config.getString(ActiveMQClientConfigKey.BROKER_URL.value()));
        } catch (JMSException e) {
            reConnect();
        }
    }

    private void reConnect(){
        logger.warn("创建ActiveMQ连接失败,broker=" + config.getString(ActiveMQClientConfigKey.BROKER_URL.value()) + ", 尝试重新连接");
        Semaphore access = new Semaphore(this.retryTimes);
        while (true){
            if (access.tryAcquire()){
                try {
                    Connection oldConnection = this.connection.get();
                    Connection newConnection = this.connectionFactory.createConnection();
                    String clientID = config.getString(ActiveMQClientConfigKey.CLIENT_ID.value());
                    newConnection.setClientID(clientID==null?"vertx-activemq-client:"+ LocalDateTime.now():clientID);
                    newConnection.start();
                    if(this.connection.compareAndSet(oldConnection,newConnection)){
                        //当前线程重连成功，更新session
                        createSession();
                    }else{
                        //其他线程已经并发重连成功，那么关闭当前线程创建的多余的连接
                        newConnection.close();
                    }
                    break;
                }catch (JMSException e){
                    //ignore the exception and continue the loop
                }
            }else {
                throw new IllegalStateException("ActiveMQ重连失败,broker=" + config.getString(ActiveMQClientConfigKey.BROKER_URL.value()) + "，请检查配置或者服务器");
            }
        }
    }

    public Session getSession() {
        if(((ActiveMQSession)this.session.get()).isClosed()){
            reConnect();
        }
        return this.session.get();
    }

    private void createSession(){
        try {
            Session session = connection.get().createSession(false,Session.AUTO_ACKNOWLEDGE);
            this.session.set(session);
        } catch (JMSException e) {
            throw new IllegalArgumentException("failed creating session of connection:" + connection);
        }
    }
}
