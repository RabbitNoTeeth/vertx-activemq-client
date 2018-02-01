package cn.booklish.vertx.activemq.client.pool

import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.jms.Connection
import javax.jms.Session


class ActiveMQSessionPool(private val connection: Connection, poolSize: Int) {

    private val random = Random()

    private var DEFAULT_POOL_SIZE = 16

    private val poolMap:ConcurrentHashMap<Int,Session>

    init {
        if(poolSize > 1) {
            DEFAULT_POOL_SIZE = poolSize
        }
        poolMap = ConcurrentHashMap(DEFAULT_POOL_SIZE)
    }

    fun getSession(): Session {
        val key = random.nextInt(DEFAULT_POOL_SIZE)
        var session = poolMap[key]
        if(session == null){
            val newSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)
            if(poolMap.putIfAbsent(key,newSession) == null){
                session = newSession
            }else{
                newSession.close()
            }
        }
        return session!!
    }


}