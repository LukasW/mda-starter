# language: de
@ui
Funktionalität: Vertrags-UI (Golden Path)

  Szenario: Sachbearbeiter sieht leere Vertragsliste
    Gegeben sei eine frische Installation
    Wenn ich die Vertragsliste abrufe
    Dann erhalte ich HTTP-Status 200
    Und die Antwort ist eine leere JSON-Liste
