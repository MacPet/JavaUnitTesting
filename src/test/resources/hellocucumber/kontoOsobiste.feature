Feature: Money transfer

  Scenario: User cant transfer money he doesnt have
    Given Account has 0 zloty
    When User transfers 10 zloty to a different account
    Then Different account has 0 zloty

  Scenario: User can transfer money
    Given Account has 100 zloty
    When User transfers 10 zloty to a different account
    Then Different account has 10 zloty
    And Account has 90 zloty

  Scenario: User has a history of transactions
    Given Account has 100 zloty
    When User transfers 10 zloty to a different account
    Then Last entry in account's history is -10

  Scenario: User can make an express transfer
    Given Account has 100 zloty
    When User express transfers 100 zloty to a different account
    Then Account has -1 zloty







