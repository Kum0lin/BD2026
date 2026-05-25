Feature: Hotel API contract

  Scenario: Return an existing hotel by id
    Given Hotel-service has a hotel named "Hilton" in "Riga"
    When a client requests that hotel from Hotel-service
    Then Hotel-service returns status 200
    And Hotel-service response contains hotel "Hilton" in "Riga"

  Scenario: Return not found for a missing hotel
    When a client requests missing hotel 999999 from Hotel-service
    Then Hotel-service returns status 404
