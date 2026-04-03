package com.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private String authHeader;

    @BeforeEach
    void setup() throws Exception {


        // Basic Authenthication
        String credentials = "auth:test";
        String encoded = Base64.getEncoder()
                .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        authHeader = "Basic " + encoded;
    }

    //    No auth test
    @Test
    void getBookingsWihtoutAuthTest() throws Exception {
        mockMvc.perform(get("/bookings"))
                .andExpect(status().isUnauthorized());
    }

    //    Auth test
    @Test
    void getBookingWithAuthTest() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk());
    }
}
