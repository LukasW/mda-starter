@service
Feature: Vertrag erfassen und einreichen

  Scenario: Antragsteller erfasst einen Vertrag
    Given ein neuer Vertragsentwurf mit Titel "Rahmenvertrag IT" und Vertragsart "DIENSTLEISTUNG"
    When der Antragsteller den Vertrag erfasst
    Then ist der Vertrag im Status "ENTWURF"

  Scenario: Einreichen ohne hochgeladene Version schlaegt fehl
    Given ein neuer Vertragsentwurf mit Titel "Rahmenvertrag HR" und Vertragsart "ARBEIT"
    And der Antragsteller hat den Vertrag erfasst
    When der Antragsteller den Vertrag einreicht
    Then liefert der Service einen Fehler mit Code "MDA-CON-031"
