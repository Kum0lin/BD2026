package com.booking.bookingservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.client.RestClientBuilderConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class HotelValidationClient {

    private final RestClient restClient;
    private final boolean validationEnabled;

    public HotelValidationClient(
            RestClientBuilderConfigurer restClientBuilderConfigurer,
            @Value("${hotel.service.validation-enabled:false}") boolean validationEnabled,
            @Value("${hotel.service.base-url:http://localhost:8082}") String hotelServiceBaseUrl
    ) {
        this.restClient = restClientBuilderConfigurer.configure(RestClient.builder())
                .baseUrl(hotelServiceBaseUrl)
                .build();
        this.validationEnabled = validationEnabled;
    }

    public boolean hotelExists(Long hotelId) {
        if (!validationEnabled) {
            return true;
        }

        try {
            restClient.get()
                    .uri("/hotels/{id}", hotelId)
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (RestClientException exception) {
            return false;
        }
    }
}



