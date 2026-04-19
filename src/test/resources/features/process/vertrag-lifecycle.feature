@process
Feature: Vertrag Lifecycle (BPF)

  Scenario: Goldener Pfad bis Freigegeben
    Given eine BPF-Instanz fuer Vertrag im Stage "ENTWURF"
    When der Trigger "EINREICHEN" ausgeloest wird
    Then ist der Stage "IN_PRUEFUNG"
    When der Trigger "GENEHMIGEN" ausgeloest wird
    Then ist der Stage "FREIGEGEBEN"

  Scenario: Verbotener Uebergang
    Given eine BPF-Instanz fuer Vertrag im Stage "ENTWURF"
    When der Trigger "GENEHMIGEN" ausgeloest wird
    Then liefert das BPF einen Fehler mit Code "MDA-BPF-001"
