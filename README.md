# vertx-activemq-client
vertx版本的ActiveMq客户端<br>


## 配置使用（how to use）

<br><br><br>
1.创建客户端(create a client)

<pre><code>

Vertx vertx = Vertx.vertx();

//配置active基础属性
JsonObject config = new JsonObject()
                        .put(ActiveMQClientConfigKey.USERNAME.value(),"xxx")
                        .put(ActiveMQClientConfigKey.PASSWORD.value(),"xxx")
                        .put(ActiveMQClientConfigKey.BROKER_URL.value(),"tcp://127.0.0.1:61616");

/**
 * 配置扩展属性
 * 1.配置session连接池大小（默认大小为3）
 *      config.put(ActiveMQClientConfigKey.SESSION_POOL_SIZE.value(),10);
 * 2.配置重连次数（应用在运行过程中，如果与active节点异常中断，
 *   那么在下一次创建消息消费者/监听者或者生产者时将尝试重新连接）
 *      config.put(ActiveMQClientConfigKey.RETRY_TIMES.value(),5);
 */


// 创建客户端（create a client）
ActiveMQClient client = ActiveMQClient.create(vertx,config);

</code></pre>

<br><br><br>
2.创建消息生产者（create a producer）

<pre><code>

/**
 * 创建queue的消息生产者（create a producer of a queue）
 *   1. 参数"myKey"表示用户自定义的该producer的唯一标识，该标识会作为键值
 *      存储在缓存管理器ActiveMQCacheManager中，所以一定要保证该标识唯一，
 *      可以将用户名来作为标识，不仅唯一，也能直观地表示该producer是用户在
 *      queue上的生产者
 *   2. 参数""vertx-test-queue""是queue的destination
 */
ActiveMQProducer producer = client.createProducer("myKey", DestinationType.QUEUE, "vertx-test-queue");

/**
 * 创建topic的消息生产者（create a producer of topic）
 *   1. 参数"myKey"表示用户自定义的该producer的唯一标识，该标识会作为键值
 *      存储在缓存管理器ActiveMQCacheManager中，所以一定要保证该标识唯一，
 *      可以将用户名来作为标识，不仅唯一，也能直观地表示该producer是用户在
 *      topic上的生产者
 *   2. 参数""vertx-test-topic""是topic的destination
 */
ActiveMQProducer producer = client.createProducer("myKey",DestinationType.TOPIC, "vertx-test-topic");

// 发送消息（send a message）
JsonObject message = new JsonObject().put("msg", "this is a test queue message!");
producer.send(message,res -> {
    if(res.succeeded()){
        System.out.println("send successful!");
    }else{
        res.cause().printStackTrace();
    }
});

</code></pre>

<br><br><br>
3.创建消息消费者（create a consumer of queue）

<pre><code>

/**
 * 创建queue的消费者（create a consumer of a queue）
 *   1. 参数"myKey"表示用户自定义的该consumer的唯一标识，该标识会作为键值
 *      存储在缓存管理器ActiveMQCacheManager中，所以一定要保证该标识唯一，
 *      可以将用户名来作为标识，不仅唯一，也能直观地表示该consumer是用户在
 *      queue上的消费者
 *   2. 参数""vertx-test-queue""是queue的destination
 */   
ActiveMQConsumer consumer = client.createConsumer("myKey","vertx-test-queue");

// 启动消费者监听（start the consumer）
consumer.listen(res -> {
    if(res.succeeded()){
        System.out.println("consumer - receive:"+res.result());
    }else{
        res.cause().printStackTrace();
    }
});

</code></pre>

<br><br><br>
4.创建消息订阅者（create a subscriber of topic）

<pre><code>

/**
 * 创建消息订阅者（create a subscriber of topic）
 *   1. 参数"myKey"表示用户自定义的该subscriber的唯一标识，该标识会作为键值
 *      存储在缓存管理器ActiveMQCacheManager中，所以一定要保证该标识唯一，
 *      可以将用户名来作为标识，不仅唯一，也能直观地表示该subscriber是用户在
 *      topic上的消费者
 *   2. 参数""vertx-test-topic""是topic的destination
 */   
ActiveMQSubscriber subscriber = client.createSubscriber("myKey","vertx-test-topic");

// 启动订阅者监听（start the subscriber）
subscriber.listen(res -> {
    if(res.succeeded()){
        System.out.println("subscriber - receive:"+res.result());
    }else{
        res.cause().printStackTrace();
    }
});

</code></pre>





