package dev.semenchenko.jetty;

import javax.naming.NamingException;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.tomcat.InstanceManager;
import org.apache.tomcat.SimpleInstanceManager;
import org.eclipse.jetty.annotations.ServletContainerInitializersStarter;
import org.eclipse.jetty.apache.jsp.JettyJasperInitializer;
import org.eclipse.jetty.jsp.JettyJspServlet;
import org.eclipse.jetty.plus.annotation.ContainerInitializer;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by a.semenchenko on 19.06.2017.
 */
public class SimpleServer {
    private static final Logger logger = LoggerFactory.getLogger(SimpleServer.class);
    private static final String WAR_FILE = "warFile";
    private static final String CONTEXT = "context";
    private static final String PORT = "port";

    public static void startServer(Map<String, String> args) throws Exception {
        if (args.size() == 0)
            throw new RuntimeException("Please specify warFile parameter in format -warFile=<path>");
        int port = 8081;
        String warFile = "../protocol-converter-war/target/protocol-converter-war.war";
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
    private URI serverURI;

    private SimpleServer(int port, String warFile, String contextPath) {
        super();
        this.port = port;
        this.warFile = warFile;
        this.contextPath = contextPath;
    }

    public URI getServerURI() {
        return serverURI;
    }

    private void start() throws Exception {
        server = new Server();

        ServerConnector connector = connector();
        server.addConnector(connector);

        System.setProperty("org.apache.jasper.compiler.disablejsr199", "false");
        WebAppContext webAppContext = getWebAppContext(getScratchDir(), warFile, contextPath);

        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setDirectoriesListed(true);
        resourceHandler.setWelcomeFiles(new String[]{"index.html"});
        resourceHandler.setResourceBase("..\\product-selection-ui\\dist/");

        HandlerList handlerList = new HandlerList();
        handlerList.setHandlers(new Handler[]{resourceHandler, webAppContext});
        server.setHandler(handlerList);


        server.start();
        this.serverURI = getServerUri(connector);
    }

    private ServerConnector connector() {
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(port);
        return connector;
    }

    /**
     * Establish Scratch directory for the servlet context (used by JSP
     * compilation)
     */

    private File getScratchDir() throws IOException {
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        File scratchDir = new File(tempDir.toString(), "embedded-jetty-jsp");
        if (!scratchDir.exists()) {
            if (!scratchDir.mkdirs()) {
                throw new IOException("Unable to create scratch directory: " + scratchDir);
            }
        }
        return scratchDir;
    }

    /**
     * Setup the basic application "context" for this application at "/"
     * <p>
     * <p>
     * This is also known as the handler tree (in jetty speak)
     *
     * @throws NamingException
     * @throws PropertyVetoException
     */
    private WebAppContext getWebAppContext(File scratchDir, String warFile, String contextPath)
            throws PropertyVetoException, NamingException {
        WebAppContext context = new WebAppContext();
        context.setContextPath(contextPath);
        context.setWar(warFile);
        context.setAttribute("javax.servlet.context.tempdir", scratchDir);
        context.setAttribute("org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern",
                ".*/[^/]*servlet-api-[^/]*\\.jar$|.*/javax.servlet.jsp.jstl-.*\\.jar$|.*/.*taglibs.*\\.jar$");
        context.setAttribute("org.eclipse.jetty.containerInitializers", jspInitializers());
        context.setAttribute(InstanceManager.class.getName(), new SimpleInstanceManager());
        context.addBean(new ServletContainerInitializersStarter(context), true);
        context.addServlet(jspServletHolder(), "*.jsp");
        return context;

    }

    /**
     * Ensure the jsp engine is initialized correctly
     */

    private List<ContainerInitializer> jspInitializers() {
        JettyJasperInitializer sci = new JettyJasperInitializer();
        ContainerInitializer initializer = new ContainerInitializer(sci, null);
        List<ContainerInitializer> initializers = new ArrayList<>();
        initializers.add(initializer);
        return initializers;
    }

    /**
     * Create JSP Servlet (must be named "jsp")
     */

    private ServletHolder jspServletHolder() {
        ServletHolder holderJsp = new ServletHolder("jsp", JettyJspServlet.class);
        holderJsp.setInitOrder(0);
        holderJsp.setInitParameter("logVerbosityLevel", "DEBUG");
        holderJsp.setInitParameter("fork", "false");
        holderJsp.setInitParameter("xpoweredBy", "false");
        holderJsp.setInitParameter("compilerTargetVM", "1.7");
        holderJsp.setInitParameter("compilerSourceVM", "1.7");
        holderJsp.setInitParameter("keepgenerated", "true");
        return holderJsp;
    }

    /**
     * Establish the Server URI
     */

    private URI getServerUri(ServerConnector connector) throws URISyntaxException {
        String scheme = "http";
        for (ConnectionFactory connectFactory : connector.getConnectionFactories()) {
            if (connectFactory.getProtocol().equals("SSL-http")) {
                scheme = "https";
            }
        }
        String host = connector.getHost();
        if (host == null) {
            host = "localhost";
        }
        int port = connector.getLocalPort();
        serverURI = new URI(String.format("%s://%s:%d/", scheme, host, port));
        logger.info("Server URI: " + serverURI);
        return serverURI;
    }

    public void stop() throws Exception {
        server.stop();
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
