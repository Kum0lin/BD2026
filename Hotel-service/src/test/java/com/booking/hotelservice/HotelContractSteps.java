package com.booking.hotelservice;

import com.booking.hotelservice.model.Hotel;
import com.booking.hotelservice.repository.HotelRepository;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class HotelContractSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private HotelRepository hotelRepository;

    private Hotel savedHotel;
    private ResultActions result;

    @Before
    public void setUp() {
        hotelRepository.deleteAll();
        savedHotel = null;
    }

    @Given("Hotel-service has a hotel named {string} in {string}")
    public void hotelServiceHasAHotelNamedIn(String name, String location) {
        Hotel hotel = new Hotel();
        hotel.setName(name);
        hotel.setLocation(location);
        savedHotel = hotelRepository.save(hotel);
    }

    @When("a client requests that hotel from Hotel-service")
    public void aClientRequestsThatHotelFromHotelService() throws Exception {
        result = mockMvc.perform(get("/hotels/{id}", savedHotel.getId()));
    }

    @When("a client requests missing hotel {long} from Hotel-service")
    public void aClientRequestsMissingHotelFromHotelService(long hotelId) throws Exception {
        result = mockMvc.perform(get("/hotels/{id}", hotelId));
    }

    @Then("Hotel-service returns status {int}")
    public void hotelServiceReturnsStatus(int expectedStatus) throws Exception {
        result.andExpect(status().is(expectedStatus));
    }

    @Then("Hotel-service response contains hotel {string} in {string}")
    public void hotelServiceResponseContainsHotelIn(String name, String location) throws Exception {
        result.andExpect(jsonPath("$.id").value(savedHotel.getId()))
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.location").value(location));
    }
}
