package fun.bookish.vertx.activemq.client.config;


public enum ActiveMQClientConfigKey {

    USERNAME("username"),
    PASSWORD("password"),
    BROKER_URL("brokerURL"),
    CLIENT_ID("clientID"),
    SESSION_POOL_SIZE("sessionPoolSize"),
    RETRY_TIMES("retryTimes");

    private final String value;
    ActiveMQClientConfigKey(String value){
        this.value = value;
    }

    public String value(){
        return this.value;
    }
}
