package cn.booklish.vertx.activemq.client.subscriber

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject


interface ActiveMQSubscriber {

    fun listen(messageHandler: Handler<AsyncResult<JsonObject>>)

    fun close()

    fun close(handler: Handler<AsyncResult<Void>>)

}