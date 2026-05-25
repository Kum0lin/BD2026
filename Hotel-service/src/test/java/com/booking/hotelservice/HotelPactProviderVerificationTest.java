package com.booking.hotelservice;

import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
import au.com.dius.pact.provider.spring.junit5.PactVerificationSpringProvider;
import com.booking.hotelservice.model.Hotel;
import com.booking.hotelservice.repository.HotelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

@Provider("Hotel-service")
@PactFolder("src/test/resources/pacts")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:hotel-pact-provider-test;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class HotelPactProviderVerificationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp(PactVerificationContext context) {
        context.setTarget(new HttpTestTarget("localhost", port));
    }

    @TestTemplate
    @ExtendWith(PactVerificationSpringProvider.class)
    void verifyPact(PactVerificationContext context) {
        context.verifyInteraction();
    }

    @State("hotel with id 1 exists")
    void hotelWithIdOneExists() {
        resetHotels();

        Hotel hotel = new Hotel();
        hotel.setName("Hilton");
        hotel.setLocation("Riga");
        hotelRepository.save(hotel);
    }

    @State("hotel with id 999 is missing")
    void hotelWithIdNineHundredNinetyNineIsMissing() {
        resetHotels();
    }

    private void resetHotels() {
        hotelRepository.deleteAll();
        jdbcTemplate.execute("ALTER TABLE hotel ALTER COLUMN id RESTART WITH 1");
    }
}
