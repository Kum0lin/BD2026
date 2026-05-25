Feature: Booking API contract

  Scenario: Create booking when Hotel-service accepts the referenced hotel
    When Booking-service receives a valid booking request for hotel 1
    Then Booking-service returns status 200
    And Booking-service response contains customer "Ivan Ivanov" and hotel 1
    And Booking-service calls Hotel-service with "GET" "/hotels/1"

  Scenario: Reject booking when Hotel-service cannot find the referenced hotel
    When Booking-service receives a booking request for missing hotel 999
    Then Booking-service returns status 400
    And Booking-service calls Hotel-service with "GET" "/hotels/999"
    And Booking-service does not save a booking
