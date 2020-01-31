package initialization;

import com.sun.net.httpserver.HttpServer;
import config.CustomDataSource;
import controller.ControllerOrchestrator;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Container {

    ControllerOrchestrator orchestrator = ControllerOrchestrator.getOrchestrator();

    public Container() throws IOException {
        int serverPort = 8000;
        HttpServer server = HttpServer.create(new InetSocketAddress(serverPort), 0);
        createContexts(server);
        initializeThreadPool(server);
        initializeDataSource();
        server.start();
    }

    private void initializeDataSource() {
        CustomDataSource dataSource = CustomDataSource.getDataSource();
        dataSource.initializeJdbcConnectionPool();
    }

    private void initializeThreadPool(HttpServer server) {
        ExecutorService executorService = Executors.newFixedThreadPool(200);
        server.setExecutor(executorService);
    }

    private void createContexts(HttpServer server) {
        server.createContext("/account/")
              .setHandler(exchange -> orchestrator.callAccountController(exchange));
        server.createContext("/transaction/")
              .setHandler(exchange -> orchestrator.callTransactionController(exchange));
    }
}
