Feature: Case retrieval and access logging over real HTTP

  # Base64 of "attorney:demo" and "paralegal:demo" - fixed demo credentials for
  # the in-memory users defined in SecurityConfig, not real secrets.
  Background:
    * url baseUrl
    * def attorney = { Authorization: 'Basic YXR0b3JuZXk6ZGVtbw==' }
    * def paralegal = { Authorization: 'Basic cGFyYWxlZ2FsOmRlbW8=' }

  Scenario: fetching an existing case returns its details
    Given path 'cases/REF-001'
    And headers paralegal
    When method get
    Then status 200
    And match response.reference == 'REF-001'
    And match response.matterName == 'Matter A'

  Scenario: fetching a case with no authentication is rejected
    Given path 'cases/REF-001'
    When method get
    Then status 401
    And match response.errorId == 'UNAUTHORIZED'

  Scenario: fetching a case with a wrong password is rejected
    Given path 'cases/REF-001'
    And header Authorization = 'Basic YXR0b3JuZXk6d3Jvbmc='
    When method get
    Then status 401

  Scenario: a paralegal cannot record an access event
    Given path 'cases/REF-001/access'
    And headers paralegal
    And request { category: 'document.view', channel: 'PORTAL' }
    When method post
    Then status 403
    And match response.errorId == 'FORBIDDEN'

  Scenario: recording an access event with an invalid channel is rejected with a clear error
    Given path 'cases/REF-001/access'
    And headers attorney
    And request { category: 'document.view', channel: 'not-a-channel' }
    When method post
    Then status 400
    And match response.errorId == 'BAD_REQUEST'
    And match response.location == 'channel'

  Scenario: recording a valid access event succeeds
    Given path 'cases/REF-001/access'
    And headers attorney
    And request { category: 'document.view', channel: 'PORTAL' }
    When method post
    Then status 202

  Scenario: an unsupported HTTP method gets 405, not 500
    Given path 'cases/REF-001'
    And headers attorney
    When method put
    Then status 405
    And match response.errorId == 'METHOD_NOT_ALLOWED'
