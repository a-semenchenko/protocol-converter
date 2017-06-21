package dev.semenchenko;

import dev.semenchenko.http.XmlParserServlet;
import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Created by a.semenchenko on 19.06.2017.
 */
public class Bootstrap {
    private static Properties properties = null;
    private static final String HOST_PORT = "http.port";
    private static final String TCP_DEST_ADDR = "tcp.dest.addr";
    private static final String TCP_DEST_PORT = "tcp.dest.port";
    private static final String PROPERTIES_FILENAME = "config.properties";

    private static final Logger logger = LoggerFactory.getLogger(Bootstrap.class);

    public static void main(String[] args) {
        Map<String, String> params = new HashMap<>();
        params.put("port", readProperty(HOST_PORT));
        logger.debug("in main");
        try {
            SimpleServer.startServer(params);
            logger.debug("Server started");
        } catch (Exception e) {
            //Ignore
        }
    }

    /**
     * Чтение и, при необходимости, загрузка из ресурса нового свойства
     * @param key имя свойства
     * @return значение свойства, или null, если свойство не прописано
     */
    private static synchronized String readProperty(String key) {
        if (loadConfig()) {
            return null;
        }
        return properties.getProperty(key);
    }

    /**
     * Загрузка конфига с нужными свойствами.
     * @return true, если загрузить не удалось
     */
    private static boolean loadConfig() {
        if (properties == null) {
            properties = new Properties();
            InputStream stream = null;
            try {
                stream = new FileInputStream(PROPERTIES_FILENAME);
                properties.load(stream);
            } catch (IOException e) {
                return true;
            } finally {
                IOUtils.closeQuietly(stream);
            }
        }
        return false;
    }

    public static class SimpleServer {
        private static final String WAR_FILE = "warFile";
        private static final String CONTEXT = "context";
        private static final String PORT = "port";

        public static void startServer(Map<String, String> args) throws Exception {
            if (args.size() == 0)
                throw new RuntimeException("Please specify warFile parameter in format -warFile=<path>");
            int port = 8081;
            String warFile = "protocol-converter-war/target/protocol-converter-war.war";
            String contextPath = "/";
            for (Map.Entry<String, String> param : args.entrySet()) {
                if (param.getKey().equalsIgnoreCase(WAR_FILE)) {
                    warFile = param.getValue();
                    logger.info("War file: " + warFile);
                } else if (param.getKey().equalsIgnoreCase(PORT)) {
                    port = Integer.parseInt(param.getValue());
                    logger.info("port: " + port);
                } else if (param.getKey().equalsIgnoreCase(CONTEXT)) {
                    contextPath = param.getValue();
                    logger.info("contextPath: " + contextPath);
                } else {
                    logger.info("skipping: " + param.getKey());
                }
            }
            SimpleServer main = new SimpleServer(port, warFile, contextPath);
            main.start();
            main.waitForInterrupt();
        }

        private final int port;
        private final String warFile;
        private final String contextPath;
        private Server server;

        private SimpleServer(int port, String warFile, String contextPath) {
            super();
            this.port = port;
            this.warFile = warFile;
            this.contextPath = contextPath;
        }

        private void start() throws Exception {
            server = new Server(port);

            WebAppContext webappcontext = new WebAppContext();
            webappcontext.setContextPath(contextPath);
            webappcontext.setWar(warFile);

            HandlerList handlers = new HandlerList();
            webappcontext.addServlet(new ServletHolder(new XmlParserServlet()), "/qwerty");

            handlers.setHandlers(new Handler[] { webappcontext, new DefaultHandler() });
            server.setHandler(handlers);
            server.start();
        }

        /**
         * Cause server to keep running until it receives a Interrupt.
         * <p>
         * <p>
         * Interrupt Signal, or SIGINT (Unix Signal), is typically seen as a result
         * of a kill -TERM {pid} or Ctrl+C
         *
         * @throws InterruptedException if interrupted
         */
        private void waitForInterrupt() throws InterruptedException {
            server.join();
        }
    }
}