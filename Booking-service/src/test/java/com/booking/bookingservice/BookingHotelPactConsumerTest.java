package com.booking.bookingservice;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "Hotel-service", pactVersion = PactSpecVersion.V3)
class BookingHotelPactConsumerTest {

    @Pact(consumer = "Booking-service", provider = "Hotel-service")
    RequestResponsePact hotelLookupContract(PactDslWithProvider builder) {
        return builder
                .given("hotel with id 1 exists")
                .uponReceiving("request existing hotel by id")
                .method("GET")
                .path("/hotels/1")
                .willRespondWith()
                .status(200)
                .body(new PactDslJsonBody()
                        .integerType("id", 1)
                        .stringType("name", "Hilton")
                        .stringType("location", "Riga"))
                .given("hotel with id 999 is missing")
                .uponReceiving("request missing hotel by id")
                .method("GET")
                .path("/hotels/999")
                .willRespondWith()
                .status(404)
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "hotelLookupContract")
    void verifiesHotelLookupContract(MockServer mockServer) throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        HttpResponse<String> existingHotelResponse = client.send(
                HttpRequest.newBuilder(URI.create(mockServer.getUrl() + "/hotels/1")).GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertThat(existingHotelResponse.statusCode()).isEqualTo(200);
        assertThat(existingHotelResponse.body()).contains("\"id\":1");
        assertThat(existingHotelResponse.body()).contains("\"name\":\"Hilton\"");
        assertThat(existingHotelResponse.body()).contains("\"location\":\"Riga\"");

        HttpResponse<String> missingHotelResponse = client.send(
                HttpRequest.newBuilder(URI.create(mockServer.getUrl() + "/hotels/999")).GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertThat(missingHotelResponse.statusCode()).isEqualTo(404);
    }
}
