package com.booking.hotelservice;

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
        "spring.datasource.url=jdbc:h2:mem:hotel-service-test;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class HotelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createAndFetchHotel() throws Exception {
        String response = mockMvc.perform(post("/hotels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Hilton",
                                  "location": "Riga"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id = response.split("\"id\":")[1].split(",")[0].trim();

        mockMvc.perform(get("/hotels/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Hilton"))
                .andExpect(jsonPath("$.location").value("Riga"));
    }

    @Test
    void missingHotelReturnsNotFound() throws Exception {
        mockMvc.perform(get("/hotels/{id}", 999))
                .andExpect(status().isNotFound());
    }
}
