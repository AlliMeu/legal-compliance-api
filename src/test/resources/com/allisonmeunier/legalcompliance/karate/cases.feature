Feature: Case retrieval and access logging - end-to-end, against a real running server

  # Basic YXR0b3JuZXk6ZGVtbw== decodes to "attorney:demo" - a fixed demo credential
  # for the in-memory user defined in SecurityConfig, not a real secret.
  Background:
    * url baseUrl
    * def authHeader = { Authorization: 'Basic YXR0b3JuZXk6ZGVtbw==' }

  Scenario: fetching an existing case returns its details
    Given path 'cases/REF-001'
    And headers authHeader
    When method get
    Then status 200
    And match response.reference == 'REF-001'
    And match response.matterName == 'Matter A'

  Scenario: fetching a case with no authentication is rejected
    Given path 'cases/REF-001'
    When method get
    Then status 401

  Scenario: recording an access event with an invalid channel is rejected with a clear error
    Given path 'cases/REF-001/access'
    And headers authHeader
    And request { category: 'document.view', channel: 'not-a-channel' }
    When method post
    Then status 400
    And match response.errorId == 'BAD_REQUEST'

  Scenario: recording a valid access event succeeds
    Given path 'cases/REF-001/access'
    And headers authHeader
    And request { category: 'document.view', channel: 'PORTAL' }
    When method post
    Then status 202
