package fun.bookish.vertx.activemq.client.util;


import java.util.UUID;

public class ExtUtils {

    private ExtUtils(){}

    public static String getUUID(){
        return UUID.randomUUID().toString().replace("-","");
    }
}
