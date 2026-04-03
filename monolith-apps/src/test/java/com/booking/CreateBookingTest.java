package com.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class CreateBookingTest {

    @Autowired
    private MockMvc mockMvc;

    private String authHeader;

    @BeforeEach
    void setup() throws Exception {

//        // Create user
//        mockMvc.perform(post("/auth/register")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("""
//                                    {
//                                      "username": "hotel3",
//                                      "password": "123456"
//                                    }
//                                """))
//                .andExpect(status().isOk());

        // Basic auth
        String credentials = "auth:test";
        String encoded = Base64.getEncoder()
                .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        authHeader = "Basic " + encoded;
    }

    @Test
    void createBookingWithHotelTest() throws Exception {

        // Create Hotel
        String hotelResponse = mockMvc.perform(post("/hotels")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    {
                                      "name": "Hilton",
                                      "location": "Riga"
                                    }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // get hotel id
        Long hotelId = Long.parseLong(
                hotelResponse.split("\"id\":")[1].split(",")[0]
        );

        // create booking
        mockMvc.perform(post("/bookings")
                        .header("Authorization", authHeader)
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
                                """.formatted(hotelId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.hotel.id").value(hotelId))
                .andExpect(jsonPath("$.customerName").value("Ivan Ivanov"));
    }

    @Test
    void createBookingWithInvalidHotelTest() throws Exception {
        Long invalidHotelId = 9999L;

        mockMvc.perform(post("/bookings")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    {
                                      "customerName": "Test User",
                                      "roomType": "STANDARD",
                                      "dateFrom": "2026-04-10",
                                      "dateTo": "2026-04-15",
                                      "hotel": {
                                        "id": %d
                                      }
                                    }
                                """.formatted(invalidHotelId)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void invalidDateRangeTest() throws Exception {

        mockMvc.perform(post("/bookings")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    {
                                      "customerName": "Test",
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
}
