@ui
Feature: Vertrag UI Golden Path

  Scenario: Benutzer legt einen Vertrag ueber die UI an
    Given der Benutzer offnet die Vertragserfassung
    When der Benutzer einen neuen Vertrag mit Titel "Rahmenvertrag CI" und Vertragsart "LIZENZ" erfasst
    Then ist der Vertrag in der Uebersicht im Status "ENTWURF" sichtbar
