package cn.booklish.vertx.activemq.client.producer

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject


interface ActiveMQProducer {

    fun send(message: JsonObject)

    fun send(message: JsonObject,handler:Handler<AsyncResult<Void>>?)

    fun close()

    fun close(handler:Handler<AsyncResult<Void>>)

}