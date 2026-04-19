# language: de
@process
Funktionalität: Vertragslebenszyklus (BPF)

  Szenario: Happy Path vom Entwurf bis zur Archivierung
    Gegeben sei ein neu angelegter Vertrag
    Wenn die Trigger "einreichen freigeben zurSignaturSenden unterzeichnen archivieren" nacheinander ausgefuehrt werden
    Dann erreicht der Vertrag die Stage "ARCHIVIERT"
    Und die BPF-Instanz hat 6 Transitions protokolliert

  Szenario: Korrekturpfad funktioniert
    Gegeben sei ein neu angelegter Vertrag
    Wenn die Trigger "einreichen korrekturbeantragen einreichen" nacheinander ausgefuehrt werden
    Dann erreicht der Vertrag die Stage "IN_PRUEFUNG"

  Szenario: Verbotener Uebergang wird abgewiesen
    Gegeben sei ein neu angelegter Vertrag
    Wenn ich den Trigger "archivieren" aufrufe
    Dann wirft das BPF einen Fehler mit Code "MDA-BPF-001"
