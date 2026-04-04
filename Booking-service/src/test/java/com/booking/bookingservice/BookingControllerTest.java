package com.booking.bookingservice;

import com.booking.bookingservice.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:booking-service-test;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "hotel.service.validation-enabled=false"
})
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookingRepository bookingRepository;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
    }

    @Test
    void createBookingReturnsSavedBooking() throws Exception {
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
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.customerName").value("Ivan Ivanov"))
                .andExpect(jsonPath("$.hotel.id").value(1));
    }

    @Test
    void createBookingWithInvalidDateRangeReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerName": "Test User",
                                  "roomType": "STANDARD",
                                  "dateFrom": "2026-04-20",
                                  "dateTo": "2026-04-10",
                                  "hotel": {
                                    "id": 1
                                  }
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllBookingsReturnsCreatedBooking() throws Exception {
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerName": "Alice",
                                  "roomType": "STANDARD",
                                  "dateFrom": "2026-05-01",
                                  "dateTo": "2026-05-03",
                                  "hotel": {
                                    "id": 2
                                  }
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/bookings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].customerName").value("Alice"))
                .andExpect(jsonPath("$[0].hotel.id").value(2));
    }
}
