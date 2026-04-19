@epic:identity @api
Feature: User login

  Backend login service issues a session token for valid credentials and rejects invalid ones.

  @severity:critical @TMS-1234 @smoke
  Scenario: Successful login returns a session token
    Given a registered user with username "alice" and password "correct-horse-battery-staple"
    When the user logs in with username "alice" and password "correct-horse-battery-staple"
    Then the response status is 200
    And the response contains a non-empty session token

  @severity:normal @TMS-1235 @regression
  Scenario Outline: Invalid credentials are rejected
    Given a registered user with username "alice" and password "correct-horse-battery-staple"
    When the user logs in with username "<username>" and password "<password>"
    Then the response status is <status>
    And the response error code is "<errorCode>"

    Examples:
      | username | password         | status | errorCode         |
      | alice    | wrong-password   | 401    | INVALID_PASSWORD  |
      | ghost    | anything         | 404    | USER_NOT_FOUND    |
      |          | anything         | 400    | MISSING_USERNAME  |
      | alice    |                  | 400    | MISSING_PASSWORD  |
