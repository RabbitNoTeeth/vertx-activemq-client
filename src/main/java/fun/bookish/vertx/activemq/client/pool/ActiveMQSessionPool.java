package fun.bookish.vertx.activemq.client.pool;



import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Session;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class ActiveMQSessionPool {

    private final AtomicReference<Connection> connectionRef = new AtomicReference<>();
    private final int poolSize;

    private final ConcurrentHashMap<Integer,Session> pool = new ConcurrentHashMap<>();

    public ActiveMQSessionPool(Connection connection,int poolSize){
        this.connectionRef.set(connection);
        this.poolSize = (poolSize >= 1)? 3 : poolSize;
    }

    private final static Random random = new Random();

    public Session getSession(){
        int randomKey = random.nextInt(this.poolSize);
        Session session = pool.get(randomKey);
        try {
            if(session == null){
                Session newSession = this.connectionRef.get().createSession(false, Session.AUTO_ACKNOWLEDGE);
                if(pool.putIfAbsent(randomKey,newSession) == null){
                    session = newSession;
                }else{
                    newSession.close();
                }
            }
        }catch (JMSException e){
            throw new IllegalArgumentException("failed creating session of connection:" + this.connectionRef.get());
        }
        return session;
    }

    public boolean setConnection(Connection old,Connection newOne){
        return this.connectionRef.compareAndSet(old,newOne);
    }
}
