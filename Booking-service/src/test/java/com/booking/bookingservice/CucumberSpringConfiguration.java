package com.booking.bookingservice;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

@CucumberContextConfiguration
@SpringBootTest
@AutoConfigureMockMvc
class CucumberSpringConfiguration {

    static final AtomicReference<String> lastHotelRequestMethod = new AtomicReference<>();
    static final AtomicReference<String> lastHotelRequestPath = new AtomicReference<>();
    static final HttpServer hotelProviderStub = createHotelProviderStub();

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:h2:mem:booking-cucumber-contract-test;DB_CLOSE_DELAY=-1");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("hotel.service.validation-enabled", () -> "true");
        registry.add("hotel.service.base-url", () -> "http://localhost:" + hotelProviderStub.getAddress().getPort());
    }

    private static HttpServer createHotelProviderStub() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
            server.createContext("/hotels", CucumberSpringConfiguration::handleHotelLookup);
            server.start();
            return server;
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to start hotel provider stub", exception);
        }
    }

    private static void handleHotelLookup(HttpExchange exchange) throws IOException {
        lastHotelRequestMethod.set(exchange.getRequestMethod());
        lastHotelRequestPath.set(exchange.getRequestURI().getPath());

        if ("GET".equals(exchange.getRequestMethod()) && "/hotels/1".equals(exchange.getRequestURI().getPath())) {
            writeResponse(exchange, 200, """
                    {
                      "id": 1,
                      "name": "Hilton",
                      "location": "Riga"
                    }
                    """);
            return;
        }

        writeResponse(exchange, 404, """
                {
                  "error": "Hotel not found"
                }
                """);
    }

    private static void writeResponse(HttpExchange exchange, int status, String body) throws IOException {
        byte[] payload = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, payload.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(payload);
        }
    }
}
