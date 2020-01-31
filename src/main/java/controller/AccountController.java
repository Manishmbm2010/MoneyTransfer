package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import dto.AccountDto;
import exception.GenericException;
import exception.InvalidInputException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import model.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.AccountService;
import util.HttpStatus;
import util.Utils;

public class AccountController {

    private static final String ACCOUNT_ID = "accountId";
    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);
    private static AccountController accountController;
    private AccountService service;

    private AccountController() {
        service = AccountService.getAccountService();
    }

    static AccountController getAccountController() {
        if (accountController != null) {
            return accountController;
        }
        accountController = new AccountController();
        return accountController;
    }

    public void account(HttpExchange httpExchange) {
        Utils.logRequest(httpExchange);
        String requestMethod = httpExchange.getRequestMethod();
        //todo if time remain write it genericly to handle all the cases
        Map<String, String> pathParameters = getMapOfPathParameters(httpExchange);

        if ("GET".equalsIgnoreCase(requestMethod) && !pathParameters.containsKey(ACCOUNT_ID)) {
            processGetRequest();
            return;
        }
        if ("GET".equalsIgnoreCase(requestMethod) && pathParameters.containsKey(ACCOUNT_ID)) {
            processGetAllRequest(pathParameters);
            return;
        }
        if ("DELETE".equalsIgnoreCase(requestMethod)) {
            processDeleteRequest(pathParameters);
            return;
        }
        if ("POST".equalsIgnoreCase(requestMethod)) {
            processCreateRequest(httpExchange.getRequestBody());
            return;
        }
        if ("PUT".equalsIgnoreCase(requestMethod)) {
            processUpdateRequest(httpExchange.getRequestBody(), pathParameters);
            return;
        }
        Utils.sendResponse(HttpStatus.METHOD_NOT_ALLOWED, null);
    }

    private void processGetAllRequest(Map<String, String> pathParameters) {
        Long accountId = Long.parseLong(pathParameters.get(ACCOUNT_ID));
        Account account = getAccount(accountId);
        Utils.sendResponse(HttpStatus.OK, account);
    }

    private void processGetRequest() {
        Set<Account> allAccount = getAllAccount();
        Utils.sendResponse(HttpStatus.OK, allAccount);
    }

    private void processDeleteRequest(Map<String, String> pathParameters) {
        if (!pathParameters.containsKey(ACCOUNT_ID)) {
            throw new InvalidInputException("AccountId path variable is missing in request uri");
        }
        Long accountId = Long.parseLong(pathParameters.get(ACCOUNT_ID));
        deleteAccount(accountId);
        Utils.sendResponse(HttpStatus.NO_CONTENT, null);
    }

    private void processCreateRequest(InputStream requestBodyStream) {
        String requestBody = Utils.getRequestBodyFrom(requestBodyStream);
        AccountDto accountDto = mapRequestBodyToAccountDto(requestBody);
        Account account = createAccount(accountDto);
        Utils.sendResponse(HttpStatus.CREATED, account);
    }

    private void processUpdateRequest(InputStream requestBodyStream, Map<String, String> pathParameters) {
        if (!pathParameters.containsKey(ACCOUNT_ID)) {
            throw new InvalidInputException("AccountId path variable is missing in request uri");
        }
        Long accountId = Long.parseLong(pathParameters.get(ACCOUNT_ID));
        String requestBody = Utils.getRequestBodyFrom(requestBodyStream);
        AccountDto accountDto = mapRequestBodyToAccountDto(requestBody);
        Account account = getAccount(accountId, accountDto);
        updateAccount(account);
        Utils.sendResponse(HttpStatus.OK, getAccount(account.getId()));
    }

    private AccountDto mapRequestBodyToAccountDto(String requestBody) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(requestBody, AccountDto.class);
        } catch (Exception ex) {
            logger.error("Exception occurred while mapping the request body tp Account Dto", ex);
            throw new GenericException(ex.getMessage());
        }
    }

    private Map<String, String> getMapOfPathParameters(HttpExchange httpExchange) {
        Map<String, String> pathParameters = new HashMap<>();
        if (httpExchange.getRequestURI().toString().split("/").length > 2) {
            long accountId = Long.parseLong(httpExchange.getRequestURI().toString().split("/")[2]);
            pathParameters.put(ACCOUNT_ID, String.valueOf(accountId));

        }
        return pathParameters;
    }

    private Account getAccount(Long accountId, AccountDto accountDto) {
        Account account = new Account();
        account.setId(accountId);
        account.setBalance(accountDto.getBalance());
        return account;
    }

    private Account createAccount(AccountDto accountDto) {
        return service.create(accountDto);
    }

    private void updateAccount(Account account) {
        service.update(account);
    }

    private Account getAccount(Long accountId) {
        return service.get(accountId);
    }

    private Set<Account> getAllAccount() {
        return service.getAll();
    }

    private void deleteAccount(Long accountId) {
        service.delete(accountId);
    }
}
