package fun.bookish.vertx.activemq.client.pool;



import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Session;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class ActiveMQSessionPool {

    private final Connection connection;
    private final int poolSize;
    private final ConcurrentHashMap<Integer,Session> pool = new ConcurrentHashMap<>();


    public ActiveMQSessionPool(Connection connection,int poolSize){
        this.connection = connection;
        if(poolSize >= 1) {
            this.poolSize = poolSize;
        }else{
            this.poolSize = DEFAULT_POOL_SIZE;
        }

    }

    private final static int DEFAULT_POOL_SIZE = 6;
    private final static Random random = new Random();


    public Session getSession(){
        int randomKey = random.nextInt(DEFAULT_POOL_SIZE);
        Session session = pool.get(randomKey);
        try {
            if(session == null){
                Session newSession = this.connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                if(pool.putIfAbsent(randomKey,newSession) == null){
                    session = newSession;
                }else{
                    newSession.close();
                }
            }
        }catch (JMSException e){
            throw new IllegalArgumentException("failed creating session of connection:" + this.connection);
        }
        return session;
    }
}
