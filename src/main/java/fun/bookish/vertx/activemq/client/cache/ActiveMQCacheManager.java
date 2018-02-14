package fun.bookish.vertx.activemq.client.cache;

import fun.bookish.vertx.activemq.client.consumer.ActiveMQConsumer;
import fun.bookish.vertx.activemq.client.producer.ActiveMQProducer;
import fun.bookish.vertx.activemq.client.subscriber.ActiveMQSubscriber;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 缓存管理器,用于缓存处于连接状态的消息消费者/订阅者/生产者
 */
public class ActiveMQCacheManager {

    private final ConcurrentHashMap<String,ActiveMQConsumer> consumerCache = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String,ActiveMQSubscriber> subscriberCache = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String,ActiveMQProducer> producerCache = new ConcurrentHashMap<>();


    public boolean cacheConsumer(ActiveMQConsumer consumer){
        return consumerCache.putIfAbsent(consumer.getKey(),consumer) == null;
    }

    public boolean cacheSubscriber(ActiveMQSubscriber subscriber){
        return subscriberCache.putIfAbsent(subscriber.getKey(),subscriber) == null;
    }

    public boolean cacheProducer(ActiveMQProducer producer){
        return producerCache.putIfAbsent(producer.getKey(),producer) == null;
    }

    public ActiveMQConsumer getConsumer(String key){
        return consumerCache.get(key);
    }

    public ActiveMQSubscriber getSubscriber(String key){
        return subscriberCache.get(key);
    }

    public ActiveMQProducer getProducer(String key){
        return producerCache.get(key);
    }

    public void removeConsumer(String key){
        ActiveMQConsumer consumer = consumerCache.remove(key);
        if(consumer != null){
            consumer.close();
        }
    }

    public void removeSubscriber(String key){
        ActiveMQSubscriber subscriber = subscriberCache.remove(key);
        if(subscriber != null){
            subscriber.close();
        }
    }

    public void removeProducer(String key){
        ActiveMQProducer producer = producerCache.remove(key);
        if(producer != null){
            producer.close();
        }
    }

    /**
     * 根据键值前缀清空
     */
    public void clear(String keyPrefix){
        consumerCache.keySet().forEach(key -> {
            if(key.startsWith(keyPrefix)){
                removeConsumer(key);
            }
        });
        subscriberCache.keySet().forEach(key -> {
            if(key.startsWith(keyPrefix)){
                removeSubscriber(key);
            }
        });
        producerCache.keySet().forEach(key -> {
            if(key.startsWith(keyPrefix)){
                removeProducer(key);
            }
        });
    }

}


