# language: de
@service
Funktionalität: Vertrag-Service

  Szenario: Sachbearbeiter legt einen neuen Vertrag an
    Gegeben sei ein angemeldeter Sachbearbeiter
    Wenn ich einen Vertrag mit Titel "Lieferabkommen 2026" vom Typ "LIEFERANTENVERTRAG" erstelle
    Dann wird der Vertrag mit Status "ENTWURF" angelegt
    Und eine VertragId wird zurückgegeben

  Szenario: Metadaten eines Entwurfs aktualisieren
    Gegeben sei ein Vertrag im Status "ENTWURF" mit Titel "Original"
    Wenn der Titel auf "Angepasst" geändert wird
    Dann trägt der Vertrag den Titel "Angepasst"

  Szenario: Vertrag einreichen wechselt die Stage
    Gegeben sei ein Vertrag im Status "ENTWURF"
    Wenn der Trigger "einreichen" ausgeloest wird
    Dann ist die Stage "IN_PRUEFUNG"

  Szenario: Unerlaubter Trigger liefert ein Problem+JSON
    Gegeben sei ein Vertrag im Status "ENTWURF"
    Wenn der Trigger "unterzeichnen" ausgeloest wird
    Dann erhalte ich einen Fehler mit Code "MDA-BPF-001"
