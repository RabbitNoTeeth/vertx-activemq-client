package cn.booklish.vertx.activemq.client.cache

import cn.booklish.vertx.activemq.client.consumer.ActiveMQConsumer
import cn.booklish.vertx.activemq.client.producer.ActiveMQProducer
import cn.booklish.vertx.activemq.client.subscriber.ActiveMQSubscriber
import java.util.concurrent.ConcurrentHashMap

/**
 * 缓存管理器,用于缓存处于连接状态的消息消费者/订阅者/生产者
 */
object ActiveMQCacheManager {

    private val consumerCache = ConcurrentHashMap<String,ActiveMQConsumer>()

    private val subscriberCache = ConcurrentHashMap<String,ActiveMQSubscriber>()

    private val producerCache = ConcurrentHashMap<String,ActiveMQProducer>()

    fun cacheConsumer(consumer: ActiveMQConsumer):Boolean{
        return consumerCache.putIfAbsent(consumer.key,consumer) == null
    }

    fun getConsumer(key: String): ActiveMQConsumer?{
        return consumerCache[key]
    }

    fun removeConsumer(key: String){
        consumerCache.remove(key)
    }

    fun cacheSubscriber(subscriber: ActiveMQSubscriber):Boolean{
        return subscriberCache.putIfAbsent(subscriber.key,subscriber) == null
    }

    fun getSubscriber(key: String): ActiveMQSubscriber?{
        return subscriberCache[key]
    }

    fun removeSubscriber(key: String){
        subscriberCache.remove(key)
    }

    fun cacheProducer(producer: ActiveMQProducer):Boolean{
        return producerCache.putIfAbsent(producer.key,producer) == null
    }

    fun getProducer(key: String): ActiveMQProducer?{
        return producerCache[key]
    }

    fun removeProducer(key: String){
        producerCache.remove(key)
    }

}