package com.booking.hotelservice;

import com.booking.hotelservice.model.Hotel;
import com.booking.hotelservice.repository.HotelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:hotel-contract-test;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class HotelContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private HotelRepository hotelRepository;

    @BeforeEach
    void setUp() {
        hotelRepository.deleteAll();
    }

    @Test
    void getHotelByIdContractReturns200WithHotelPayload() throws Exception {
        Hotel hotel = new Hotel();
        hotel.setName("Hilton");
        hotel.setLocation("Riga");
        Hotel savedHotel = hotelRepository.save(hotel);

        mockMvc.perform(get("/hotels/{id}", savedHotel.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedHotel.getId()))
                .andExpect(jsonPath("$.name").value("Hilton"))
                .andExpect(jsonPath("$.location").value("Riga"));
    }

    @Test
    void getHotelByIdContractReturns404WhenHotelIsMissing() throws Exception {
        mockMvc.perform(get("/hotels/{id}", 999999))
                .andExpect(status().isNotFound());
    }
}
