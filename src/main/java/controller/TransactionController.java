package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import dto.TransactionDto;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.TransactionService;
import util.HttpStatus;
import util.Utils;

class TransactionController {

    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);
    private static final String TRANSACTION_ID = "transactionId";
    private static TransactionController transactionController;
    private ObjectMapper objectMapper = new ObjectMapper();
    private TransactionService service;

    private TransactionController() {
        service = TransactionService.getTransactionService();
    }

    static TransactionController getTransactionController() {
        if (transactionController != null) {
            return transactionController;
        }
        transactionController = new TransactionController();
        return transactionController;
    }

    void transaction(HttpExchange httpExchange) throws IOException {
        Utils.logRequest(httpExchange);
        String requestMethod = httpExchange.getRequestMethod();
        Map<String, String> pathParameters = getMapOfPathParameters(httpExchange);

        if ("GET".equalsIgnoreCase(requestMethod) && !pathParameters.containsKey(TRANSACTION_ID)) {
            processGetAllRequest();
            return;
        }
        if ("GET".equalsIgnoreCase(requestMethod) && pathParameters.containsKey(TRANSACTION_ID)) {
            processGetRequest(pathParameters);
            return;
        }

        if ("POST".equalsIgnoreCase(requestMethod)) {
            processInsertRequest(httpExchange.getRequestBody());
            return;
        }
        if ("PUT".equalsIgnoreCase(requestMethod)) {
            processUpdateRequest(httpExchange.getRequestBody());
            return;
        }
        Utils.sendResponse(HttpStatus.METHOD_NOT_ALLOWED, null);
    }

    private void processGetAllRequest() {
        Set<Transaction> transactions = getAllTransaction();
        Utils.sendResponse(HttpStatus.OK, transactions);
    }

    private void processGetRequest(Map<String, String> pathParameters) {
        int transactionId = Integer.parseInt(pathParameters.get(TRANSACTION_ID));
        Transaction transaction = getTransaction(transactionId);
        Utils.sendResponse(HttpStatus.OK, transaction);
    }

    private void processInsertRequest(InputStream requestBodyStream) throws IOException {
        String requestBody = Utils.getRequestBodyFrom(requestBodyStream);
        TransactionDto transactionDto = objectMapper.readValue(requestBody, TransactionDto.class);
        Transaction transaction = initiateTransaction(transactionDto);
        Utils.sendResponse(HttpStatus.CREATED, transaction);
    }

    private void processUpdateRequest(InputStream requestBodyStream) throws IOException {
        String requestBody = Utils.getRequestBodyFrom(requestBodyStream);
        Transaction transaction = objectMapper.readValue(requestBody, Transaction.class);
        updateTransactionStatus(transaction);
        Utils.sendResponse(HttpStatus.OK, getTransaction(transaction.getId()));
    }

    private Map<String, String> getMapOfPathParameters(HttpExchange httpExchange) {
        Map<String, String> pathParameters = new HashMap<>();
        if (httpExchange.getRequestURI().toString().split("/").length > 2) {
            long transactionID = Long.parseLong(httpExchange.getRequestURI().toString().split("/")[2]);
            pathParameters.put(TRANSACTION_ID, String.valueOf(transactionID));

        }
        return pathParameters;
    }

    private Transaction getTransaction(long transactionId) {
        return service.getTransaction(transactionId);
    }

    private Set<Transaction> getAllTransaction() {
        return service.getAllTransaction();
    }

    private void updateTransactionStatus(Transaction transaction) {
        service.updateTransactionStatus(transaction);
    }

    private Transaction initiateTransaction(TransactionDto transactionDto) {
        return service.initiateTransaction(transactionDto);
    }
}
