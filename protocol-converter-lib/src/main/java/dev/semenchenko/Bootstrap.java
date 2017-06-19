package dev.semenchenko;

import dev.semenchenko.jetty.SimpleServer;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by a.semenchenko on 19.06.2017.
 */
public class Bootstrap {
    private Properties properties = null;
    private static final String HOST_PORT = "http.port";
    private static final String TCP_DEST_ADDR = "tcp.dest.addr";
    private static final String TCP_DEST_PORT = "tcp.dest.port";
    private static final String PROPERTIES_FILENAME = "config.properties";

    public void start() {
        Map<String, String> args = new HashMap<>();
        args.put("port", readProperty(HOST_PORT));
        try {
            SimpleServer.startServer(args);
        } catch (Exception e) {
            //Ignore
        }
    }

    /**
     * Чтение и, при необходимости, загрузка из ресурса нового свойства
     * @param key имя свойства
     * @return значение свойства, или null, если свойство не прописано
     */
    private synchronized String readProperty(String key) {
        if (loadConfig()) {
            return null;
        }
        return properties.getProperty(key);
    }

    /**
     * Загрузка конфига с нужными свойствами.
     * @return true, если загрузить не удалось
     */
    private boolean loadConfig() {
        if (properties == null) {
            properties = new Properties();
            InputStream stream = null;
            try {
                stream = Bootstrap.class.getClassLoader().getResourceAsStream(PROPERTIES_FILENAME);
                properties.load(stream);
            } catch (IOException e) {
                return true;
            } finally {
                IOUtils.closeQuietly(stream);
            }
        }
        return false;
    }
}