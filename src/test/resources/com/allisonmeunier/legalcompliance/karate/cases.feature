Feature: Case retrieval and access logging over real HTTP

  # Demo users defined in SecurityConfig, not real accounts.
  Background:
    * url baseUrl
    * def basicAuth = read('basic-auth.js')
    * def attorney = { Authorization: '#(basicAuth({ username: "attorney", password: "demo" }))' }
    * def paralegal = { Authorization: '#(basicAuth({ username: "paralegal", password: "demo" }))' }

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

  Scenario: fetching a case with a wrong password is rejected
    Given path 'cases/REF-001'
    And header Authorization = basicAuth({ username: 'attorney', password: 'wrong' })
    When method get
    Then status 401

  Scenario: a paralegal cannot record an access event
    Given path 'cases/REF-001/access'
    And headers paralegal
    And request { category: 'document.view', channel: 'PORTAL' }
    When method post
    Then status 403

  Scenario: recording an access event with an invalid channel is rejected with a clear error
    Given path 'cases/REF-001/access'
    And headers attorney
    And request { category: 'document.view', channel: 'not-a-channel' }
    When method post
    Then status 400
    And match response.errorId == 'BAD_REQUEST'

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
