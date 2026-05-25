Feature: User API contract

  Scenario: Register a new user
    When a client registers user "contract-user" with password "test"
    Then User-service returns status 200
    And User-service response contains username "contract-user" and role "ROLE_USER"

  Scenario: Return current user for a valid Basic Auth request
    Given User-service has registered user "secured" with password "pass123"
    When a client requests current user as "secured" with password "pass123"
    Then User-service returns status 200
    And User-service response contains current username "secured"

  Scenario: Require authentication for current user
    When an anonymous client requests current user
    Then User-service returns status 401
