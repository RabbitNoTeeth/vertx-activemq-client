package fun.bookish.vertx.activemq.client.pool;



import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Session;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicReference;

public class ActiveMQSessionPool {

    private final AtomicReference<Connection> connection = new AtomicReference<>(null);
    private Session session;

    private final ConcurrentHashMap<Integer,Future<Session>> pool = new ConcurrentHashMap<>();

    public ActiveMQSessionPool(Connection connection){
        this.connection.set(connection);
        createSession();
    }

    public Session getSession() {
        return session;
    }

    public Session reConnect(Connection oldCon,Connection newCon){
        try {
            if(this.connection.compareAndSet(oldCon,newCon)){
                this.session = newCon.createSession(false,Session.AUTO_ACKNOWLEDGE);
            }
            return this.session;
        } catch (JMSException e) {
            return null;
        }
    }

    private void createSession(){
        try {
            this.session = connection.get().createSession(false,Session.AUTO_ACKNOWLEDGE);
        } catch (JMSException e) {
            throw new IllegalArgumentException("failed creating session of connection:" + connection);
        }
    }
}
