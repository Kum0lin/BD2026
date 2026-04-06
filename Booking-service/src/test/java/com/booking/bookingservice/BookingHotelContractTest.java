package com.booking.bookingservice;

import com.booking.bookingservice.repository.BookingRepository;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BookingHotelContractTest {

    private static final AtomicReference<String> lastMethod = new AtomicReference<>();
    private static final AtomicReference<String> lastPath = new AtomicReference<>();
    private static final HttpServer hotelProviderStub = createHotelProviderStub();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookingRepository bookingRepository;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:h2:mem:booking-contract-test;DB_CLOSE_DELAY=-1");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("hotel.service.validation-enabled", () -> "true");
        registry.add("hotel.service.base-url", () -> "http://localhost:" + hotelProviderStub.getAddress().getPort());
    }

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        lastMethod.set(null);
        lastPath.set(null);
    }

    @AfterAll
    static void tearDown() {
        hotelProviderStub.stop(0);
    }

    @Test
    void createBookingContractAcceptsProvider200AndCallsExpectedEndpoint() throws Exception {
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerName": "Ivan Ivanov",
                                  "roomType": "DELUXE",
                                  "dateFrom": "2026-04-10",
                                  "dateTo": "2026-04-15",
                                  "hotel": {
                                    "id": 1
                                  }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerName").value("Ivan Ivanov"))
                .andExpect(jsonPath("$.hotel.id").value(1));

        assertThat(lastMethod.get()).isEqualTo("GET");
        assertThat(lastPath.get()).isEqualTo("/hotels/1");
    }

    @Test
    void createBookingContractRejectsProvider404AsBadRequest() throws Exception {
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerName": "Missing Hotel",
                                  "roomType": "STANDARD",
                                  "dateFrom": "2026-04-20",
                                  "dateTo": "2026-04-21",
                                  "hotel": {
                                    "id": 999
                                  }
                                }
                                """))
                .andExpect(status().isBadRequest());

        assertThat(lastMethod.get()).isEqualTo("GET");
        assertThat(lastPath.get()).isEqualTo("/hotels/999");
        assertThat(bookingRepository.count()).isZero();
    }

    private static HttpServer createHotelProviderStub() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
            server.createContext("/hotels", BookingHotelContractTest::handleHotelLookup);
            server.start();
            return server;
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to start hotel provider stub", exception);
        }
    }

    private static void handleHotelLookup(HttpExchange exchange) throws IOException {
        lastMethod.set(exchange.getRequestMethod());
        lastPath.set(exchange.getRequestURI().getPath());

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
