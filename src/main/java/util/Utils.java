package util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import controller.ControllerOrchestrator;
import exception.GenericException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utils {

    private static final Logger logger = LoggerFactory.getLogger(Utils.class);
    private static Utils utils;


    private Utils() {
    }

    public static Utils getUtils() {
        if (utils != null) {
            return utils;
        }
        utils = new Utils();
        return utils;
    }

    public static void sendResponse(HttpStatus httpStatus, Object object) {
        HttpExchange httpExchange = ControllerOrchestrator.getThreadLocal().get();
        ControllerOrchestrator.getThreadLocal().remove();
        try {
            if (object == null) {
                httpExchange.sendResponseHeaders(httpStatus.getCode(), 0);
                httpExchange.close();
                return;
            }
            byte[] response = stringifyResponse(object).getBytes();
            httpExchange.getResponseHeaders().add("Content-type", "application/json");
            httpExchange.getResponseHeaders().add("Content-length", Integer.toString(response.length));
            httpExchange.sendResponseHeaders(httpStatus.getCode(), response.length);
            httpExchange.getResponseBody().write(response);
            httpExchange.close();
        } catch (Exception ex) {
            logger.error("Exception occurred while preparing the response ", ex);
            throw new GenericException(ex.getMessage());
        }
    }

    public static String stringifyResponse(Object object) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String response = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
            logger.info("Response is \n{}", response);
            return response;
        } catch (JsonProcessingException ex) {
            logger.error("Exception occurred while preparing the json string ", ex);
            throw new GenericException(ex.getMessage());
        }
    }

    public static void logRequest(HttpExchange httpExchange) {
        logger.info("Request is {} {}", httpExchange.getRequestURI());
        logger.info("Processing {} request", httpExchange.getRequestMethod());

    }

    public static String getRequestBodyFrom(InputStream requestBodyStream) {
        try {
            InputStreamReader isr = new InputStreamReader(requestBodyStream, "utf-8");
            BufferedReader br = new BufferedReader(isr);

            int b;
            StringBuilder buf = new StringBuilder(512);
            while ((b = br.read()) != -1) {
                buf.append((char) b);
            }
            br.close();
            isr.close();
            return buf.toString();
        } catch (IOException ex) {
            throw new GenericException(ex.getMessage());
        }
    }
}
