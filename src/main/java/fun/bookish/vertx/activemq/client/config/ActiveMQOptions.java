package fun.bookish.vertx.activemq.client.config;

import fun.bookish.vertx.activemq.client.util.ExtUtils;

import javax.jms.Session;

public class ActiveMQOptions {

    private String clientId = ExtUtils.getUUID();
    private String broker = "tcp://127.0.0.1:61616";
    private String username = "admin";
    private String password = "admin";
    private int retryTimes = 5;
    private boolean transacted = false;
    private int acknowledgeMode = Session.AUTO_ACKNOWLEDGE;

    public String getClientId() {
        return clientId;
    }

    public ActiveMQOptions setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    public String getBroker() {
        return broker;
    }

    public ActiveMQOptions setBroker(String broker) {
        this.broker = broker;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public ActiveMQOptions setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public ActiveMQOptions setPassword(String password) {
        this.password = password;
        return this;
    }

    public int getRetryTimes() {
        return retryTimes;
    }

    public ActiveMQOptions setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
        return this;
    }

    public boolean isTransacted() {
        return transacted;
    }

    public ActiveMQOptions setTransacted(boolean transacted) {
        this.transacted = transacted;
        return this;
    }

    public int getAcknowledgeMode() {
        return acknowledgeMode;
    }

    public ActiveMQOptions setAcknowledgeMode(int acknowledgeMode) {
        this.acknowledgeMode = acknowledgeMode;
        return this;
    }
}
