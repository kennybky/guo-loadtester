package edu.csula.cs594.client;

import java.util.Map;
import java.util.concurrent.*;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The ServletContainer (Jersey, Tomcat, etc) will do a classpath scan for anything using the
 *
 * @WebListener annotation for any classes implementing the ServletContextListener interface
 *
 * (We could have also used Jersey's ResourceContext for this instead of ServletContextListner)
 *
 * The interface is used to initialize and destroy any application-level servlet class Rather than having a static
 * database instance, this method of instantiation is preferred in Servlet apps Part of Servlet 3.X specification
 *
 * All classes that should have only once instance should be instantiated here
 *
 */
@WebListener
public class ServletContextListenerImpl implements ServletContextListener {
    
    private static final Logger logger = LoggerFactory.getLogger(ServletContextListenerImpl.class);
    
    private DatabaseClient dbClient = null;
    private ExecutorService testers = null; //thread pool for CliClient testers
    private ExecutorService consumers = null; //thread pool for DataConsumers
    private ExecutorService asyncHandlers = null; //thread pool for AsyncHandlers from ning library
    private final Map<Integer, CliClient> cliClientMap = new ConcurrentHashMap<>();
    private final Map<Integer, DataConsumer> consumerMap = new ConcurrentHashMap<>();
    private final Map<Integer, TestContext> testContextMap = new ConcurrentHashMap<>();
    
    public ServletContextListenerImpl() {
        logger.info("*** ServletContextListenerImpl instantiated ***");
    }
    
    @Override
    public void contextInitialized(ServletContextEvent event) {
        
        // Used everywhere
        logger.info("Adding database client to ServletContext...");
        dbClient = new DatabaseClient();
        event.getServletContext().setAttribute("dbClient", dbClient);

        // Used by ProjectResource and TestResource
        logger.info("Adding cliClientMap to ServletContext");
        event.getServletContext().setAttribute("cliClientMap", cliClientMap);

        // Used by TestResource
        logger.info("Adding consumerMap to ServletContext");
        event.getServletContext().setAttribute("consumerMap", consumerMap);

        // Used by TestResource
        event.getServletContext().setAttribute("testContextMap", testContextMap);

        // Used by TestResource
        logger.info("Adding newCachedThread pool for ning AsyncHandlers to ServletContext");
        asyncHandlers = Executors.newCachedThreadPool();
        event.getServletContext().setAttribute("asyncHandlerThreadPool", asyncHandlers);

        // Used by TestResource
        logger.info("Adding ThreadPoolExecutor for TESTERS to ServletContext...");
        testers = new ThreadPoolExecutor(5, 5, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>()) {
            @Override
            protected void beforeExecute(Thread t, Runnable r) {
                CliClient client = (CliClient) r;
                client.setThread(t);
                cliClientMap.put(client.getTestContext().getProjectId(), client);
            }

            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                CliClient client = (CliClient) r;
                cliClientMap.remove(client.getTestContext().getProjectId());
            }
        };
        event.getServletContext().setAttribute("testThreadPool", testers);

        // Used by TestResource
        logger.info("Adding ThreadPoolExecutor for CONSUMERS to ServletContext");
        consumers =  new ThreadPoolExecutor(5, 5, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>()) {
            @Override
            protected void beforeExecute(Thread t, Runnable r) {
                DataConsumer consumer = (DataConsumer) r;
                consumer.setThread(t);
                consumerMap.put(consumer.getTestContext().getProjectId(), consumer);
            }

            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                logger.info("Consumer is done. Removing from hashmap.");
                DataConsumer consumer = (DataConsumer) r;
                consumerMap.remove(consumer.getTestContext().getProjectId());
            }
        };
        event.getServletContext().setAttribute("consumerThreadPool", consumers);

    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        logger.info("ending all running tests...");
        try {
            testers.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            // ignore me
        }
        event.getServletContext().removeAttribute("testThreadPool");
        testers = null;

        logger.info("ending all asynchandler threads...");
        try {
            asyncHandlers.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            // ignore me
        }
        event.getServletContext().removeAttribute("asyncHandlerThreadPool");
        asyncHandlers = null;

        logger.info("ending all consumer threads...");
        try {
            consumers.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            // ignore me
        }
        event.getServletContext().removeAttribute("consumerThreadPool");
        consumers = null;

        
        logger.info("closing database connection...");        
        if (dbClient != null) {
            try {
                dbClient.close();                
            } catch (Exception e) {
                // ignore me
            }
            
            event.getServletContext().removeAttribute("dbClient");
            dbClient = null;
        }
                
    }
    
}
