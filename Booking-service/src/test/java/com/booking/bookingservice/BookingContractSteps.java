package com.booking.bookingservice;

import com.booking.bookingservice.repository.BookingRepository;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class BookingContractSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookingRepository bookingRepository;

    private ResultActions result;

    @Before
    public void setUp() {
        bookingRepository.deleteAll();
        CucumberSpringConfiguration.lastHotelRequestMethod.set(null);
        CucumberSpringConfiguration.lastHotelRequestPath.set(null);
    }

    @AfterAll
    public static void tearDown() {
        CucumberSpringConfiguration.hotelProviderStub.stop(0);
    }

    @When("Booking-service receives a valid booking request for hotel {long}")
    public void bookingServiceReceivesAValidBookingRequestForHotel(long hotelId) throws Exception {
        result = mockMvc.perform(post("/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "customerName": "Ivan Ivanov",
                          "roomType": "DELUXE",
                          "dateFrom": "2026-04-10",
                          "dateTo": "2026-04-15",
                          "hotel": {
                            "id": %d
                          }
                        }
                        """.formatted(hotelId)));
    }

    @When("Booking-service receives a booking request for missing hotel {long}")
    public void bookingServiceReceivesABookingRequestForMissingHotel(long hotelId) throws Exception {
        result = mockMvc.perform(post("/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "customerName": "Missing Hotel",
                          "roomType": "STANDARD",
                          "dateFrom": "2026-04-20",
                          "dateTo": "2026-04-21",
                          "hotel": {
                            "id": %d
                          }
                        }
                        """.formatted(hotelId)));
    }

    @Then("Booking-service returns status {int}")
    public void bookingServiceReturnsStatus(int expectedStatus) throws Exception {
        result.andExpect(status().is(expectedStatus));
    }

    @Then("Booking-service response contains customer {string} and hotel {long}")
    public void bookingServiceResponseContainsCustomerAndHotel(String customerName, long hotelId) throws Exception {
        result.andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.customerName").value(customerName))
                .andExpect(jsonPath("$.hotel.id").value(hotelId));
    }

    @Then("Booking-service calls Hotel-service with {string} {string}")
    public void bookingServiceCallsHotelServiceWith(String method, String path) {
        assertThat(CucumberSpringConfiguration.lastHotelRequestMethod.get()).isEqualTo(method);
        assertThat(CucumberSpringConfiguration.lastHotelRequestPath.get()).isEqualTo(path);
    }

    @Then("Booking-service does not save a booking")
    public void bookingServiceDoesNotSaveABooking() {
        assertThat(bookingRepository.count()).isZero();
    }
}
