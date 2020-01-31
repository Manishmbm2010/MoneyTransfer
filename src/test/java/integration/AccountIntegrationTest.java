package integration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import initialization.Container;
import java.io.IOException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AccountIntegrationTest {

    @BeforeAll
    static void init() throws IOException {
        new Container();
    }

    @BeforeEach
    void setUp() {
    }

    @Test
    void shouldGetAccount() throws IOException {
        HttpUriRequest request = new HttpGet("http://localhost:8000/account/" + 1);
        HttpResponse httpResponse = HttpClientBuilder
            .create()
            .build()
            .execute(request);

        assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine().getStatusCode());
        assertEquals("Content-type: application/json", httpResponse.getEntity().getContentType().toString());
    }

    @Test
    void getShouldRespondWith404WhenNoAccountFound() throws IOException {
        HttpUriRequest request = new HttpGet("http://localhost:8000/account/" + 5);
        HttpResponse httpResponse = HttpClientBuilder
            .create()
            .build()
            .execute(request);

        assertEquals(HttpStatus.SC_NOT_FOUND, httpResponse.getStatusLine().getStatusCode());
    }

    @Test
    void deleteShouldRespondWith204WhenNoError() throws IOException {
        HttpUriRequest request = new HttpDelete("http://localhost:8000/account/" + 5);
        HttpResponse httpResponse = HttpClientBuilder
            .create()
            .build()
            .execute(request);

        assertEquals(HttpStatus.SC_NO_CONTENT, httpResponse.getStatusLine().getStatusCode());
    }

    @Test
    void deleteShouldRespondWithErrorCodeWhenRequestUriDoesnotContainId() throws IOException {
        HttpUriRequest request = new HttpDelete("http://localhost:8000/account/");
        HttpResponse httpResponse = HttpClientBuilder
            .create()
            .build()
            .execute(request);

        assertEquals(HttpStatus.SC_BAD_REQUEST, httpResponse.getStatusLine().getStatusCode());
    }


}

