package controller;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ControllerOrchestrator {

    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);
    private static ControllerOrchestrator orchestrator;
    private static ThreadLocal<HttpExchange> threadLocal;

    private ControllerOrchestrator() {
    }

    public static ControllerOrchestrator getOrchestrator() {
        if (orchestrator == null) {
            orchestrator = new ControllerOrchestrator();
        }
        return orchestrator;
    }

    public static ThreadLocal<HttpExchange> getThreadLocal() {
        return threadLocal;
    }

    public void callAccountController(HttpExchange httpExchange) throws IOException {
        threadLocal = new ThreadLocal<>();
        threadLocal.set(httpExchange);
        AccountController.getAccountController().account(httpExchange);
    }

    public void callTransactionController(HttpExchange httpExchange) throws IOException {
        threadLocal = new ThreadLocal<>();
        threadLocal.set(httpExchange);
        TransactionController.getTransactionController().transaction(httpExchange);
    }
}
