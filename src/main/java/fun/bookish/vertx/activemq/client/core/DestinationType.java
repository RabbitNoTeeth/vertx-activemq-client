package fun.bookish.vertx.activemq.client.core;

/**
 * @author Don9
 * @create 2018-02-07-16:18
 **/
public enum DestinationType {

    QUEUE("queue"),TOPIC("topic");
    
    private final String value;
    
    DestinationType(String value){
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
