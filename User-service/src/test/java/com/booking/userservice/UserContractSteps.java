package com.booking.userservice;

import com.booking.userservice.model.User;
import com.booking.userservice.repository.UserRepository;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserContractSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private ResultActions result;

    @Before
    public void setUp() {
        userRepository.deleteAll();
    }

    @Given("User-service has registered user {string} with password {string}")
    public void userServiceHasRegisteredUserWithPassword(String username, String password) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("ROLE_USER");
        userRepository.save(user);
    }

    @When("a client registers user {string} with password {string}")
    public void aClientRegistersUserWithPassword(String username, String password) throws Exception {
        result = mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "username": "%s",
                          "password": "%s"
                        }
                        """.formatted(username, password)));
    }

    @When("a client requests current user as {string} with password {string}")
    public void aClientRequestsCurrentUserAsWithPassword(String username, String password) throws Exception {
        result = mockMvc.perform(get("/users/me")
                .header("Authorization", basicAuth(username, password)));
    }

    @When("an anonymous client requests current user")
    public void anAnonymousClientRequestsCurrentUser() throws Exception {
        result = mockMvc.perform(get("/users/me"));
    }

    @Then("User-service returns status {int}")
    public void userServiceReturnsStatus(int expectedStatus) throws Exception {
        result.andExpect(status().is(expectedStatus));
    }

    @Then("User-service response contains username {string} and role {string}")
    public void userServiceResponseContainsUsernameAndRole(String username, String role) throws Exception {
        result.andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.role").value(role));
    }

    @Then("User-service response contains current username {string}")
    public void userServiceResponseContainsCurrentUsername(String username) throws Exception {
        result.andExpect(jsonPath("$.username").value(username));
    }

    private String basicAuth(String username, String password) {
        String credentials = username + ":" + password;
        return "Basic " + Base64.getEncoder()
                .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }
}
