# language: de
@service
Funktionalität: Personen-Stammdatenverwaltung

  Als Sachbearbeiter
  möchte ich Personen erfassen, ändern und löschen
  damit ich Vertragsparteien aus eigener Quelle pflegen kann.

  Szenario: Sachbearbeiter erfasst und ändert eine Person
    Wenn ich eine Person mit Vorname "Sara" Nachname "Beispiel" Email "sara.beispiel@example.ch" erfasse
    Dann wird die Person mit Versionsnummer 0 angelegt
    Wenn ich den Nachname der Person auf "Geändert" mit erwarteter Version 0 ändere
    Dann hat die Person den Nachname "Geändert" und Version 1

  Szenario: Person mit Quelle EXTERN_API kann nicht gelöscht werden
    Angenommen es existiert eine externe Personen-Snapshot-Person
    Wenn ich die externe Person löschen möchte
    Dann erhalte ich beim Löschen einen Fehler mit Code "MDA-PER-003"
