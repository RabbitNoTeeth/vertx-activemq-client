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

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getBroker() {
        return broker;
    }

    public void setBroker(String broker) {
        this.broker = broker;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getRetryTimes() {
        return retryTimes;
    }

    public void setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
    }

    public boolean isTransacted() {
        return transacted;
    }

    public void setTransacted(boolean transacted) {
        this.transacted = transacted;
    }

    public int getAcknowledgeMode() {
        return acknowledgeMode;
    }

    public void setAcknowledgeMode(int acknowledgeMode) {
        this.acknowledgeMode = acknowledgeMode;
    }
}
