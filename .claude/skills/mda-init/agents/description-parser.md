# Agent: description-parser

Extrahiert aus einer Markdown-Beschreibung (oder Freitext) die fachlichen Modellelemente, die `mda-init` braucht, um ein hexagonales Quarkus-Projekt zu generieren.

## Eingabe

- `specs/description.md` im Ziel-Repo ODER Freitext im Prompt.
- Die Beschreibung kann unstrukturiert sein (Kapitel + Fliesstext), oder mit Ueberschriften wie "Bounded Contexts", "Aggregates", "Prozesse", "Rollen" strukturiert.

Wenn die Eingabe Freitext im Prompt war, legt der Agent **zuerst** `specs/description.md` im Zielrepo an (damit die Quelle versioniert bleibt).

## Ausgabe

Drei JSON-Dateien in `/tmp/` (gleiche Shape wie die PlantUML-Extraktoren — somit austauschbar):

- `/tmp/mda-domain-model.json`
- `/tmp/mda-process-model.json` (leer, wenn keine Prozesse erkannt)
- `/tmp/mda-rule-model.json` (leer, wenn keine Regeln erkannt)

### Schema `mda-domain-model.json`

Beispiel mit neutraler Demo-Domaene (`Auftrag`/`order`):

```json
{
  "root_package": "com.example.app",
  "bounded_contexts": [
    {
      "name": "order",
      "aggregates": [
        {
          "name": "Auftrag",
          "id_type": "AuftragId",
          "value_objects": [
            { "name": "Geldbetrag", "fields": [{ "name": "wert", "type": "Decimal" }, { "name": "waehrung", "type": "String" }] }
          ],
          "fields": [
            { "name": "titel", "type": "String", "constraints": { "notBlank": true, "maxLength": 160 } },
            { "name": "status", "type": "Enum:AuftragStatus" }
          ],
          "use_cases": [
            { "kind": "command", "name": "AuftragErstellen",   "inputs": ["titel", "bearbeiterId"], "outputs": ["auftragId"] },
            { "kind": "command", "name": "AuftragBestaetigen", "inputs": ["auftragId", "bearbeiterId"], "outputs": [] },
            { "kind": "query",   "name": "AuftragDetail",      "inputs": ["auftragId"], "outputs": ["AuftragDto"] }
          ],
          "events": ["AuftragErstellt", "AuftragBestaetigt"]
        }
      ]
    }
  ]
}
```

### Schema `mda-process-model.json`

```json
{
  "processes": [
    {
      "name": "AuftragLifecycle",
      "aggregate": "Auftrag",
      "bounded_context": "order",
      "stages": ["ENTWURF", "BESTAETIGT", "VERSANDT", "STORNIERT"],
      "transitions": [
        { "from": "ENTWURF",    "trigger": "bestaetigen", "to": "BESTAETIGT" },
        { "from": "BESTAETIGT", "trigger": "versenden",   "to": "VERSANDT" },
        { "from": "BESTAETIGT", "trigger": "stornieren",  "to": "STORNIERT" }
      ],
      "initial": "ENTWURF"
    }
  ]
}
```

### Schema `mda-rule-model.json`

```json
{
  "rules": [
    {
      "aggregate": "Auftrag",
      "field": "betrag",
      "condition": { "gt": ["{{betrag.wert}}", 100000] },
      "actions": [
        { "type": "showRecommendation", "message": "Ab 100k zweites Review einholen." }
      ]
    }
  ]
}
```

## Heuristiken

Der Agent extrahiert nach folgenden Heuristiken (in dieser Reihenfolge; konfligierende Treffer → `TODO` + Rueckfrage an Nutzer):

| Erkennungsmerkmal | Folgerung |
|---|---|
| Ueberschrift oder Absatz wie "Bounded Context: …" | neuer BC |
| Substantivgruppen mit Lebenszyklus (Singular + Plural + "erstellen/bearbeiten/loeschen") | Aggregate |
| Typisierte Felder ("Geldbetrag", "IBAN", "E-Mail", "Datum") | Value Object |
| Zahlen-/Bereichs-Angaben ("max. 160 Zeichen", "0 ≤ x ≤ 100") | Attribut-Constraint |
| "Als <Rolle> moechte ich …" | AuthZ-Rolle + UseCase |
| Verben + Substantive (z. B. "Auftrag bestaetigen", "Rechnung stellen") | UseCase (Command) |
| "Liste der …" / "Uebersicht" | Query / View |
| Statuswoerter in Aufzaehlungen ("Entwurf → In Pruefung → Aktiv") | BPF-Stages + Transition |
| Vergangenheitsformen (z. B. "Auftrag bestaetigt", "Lieferung versandt") | Domain Event |
| Konditionalsaetze ("wenn … dann …") in Fachsprache | BusinessRule |
| "muss", "darf nicht", "maximal" | Validation-Constraint oder Rule |

## Regeln fuer den Agent

- **Nicht raten**. Unsichere Treffer → `TODO` im Ausgabe-JSON (`"todo": "unklar ob Aggregate oder VO: ‚Beilage'"`), plus Nutzerfrage am Phasenende.
- **Deutsch als Fachsprache**: Namen in der Sprache der Beschreibung uebernehmen. Nur Technik-Namen (z. B. Java-Typ `UUID`) sind englisch.
- **Keine Template-Entscheidungen**. Dieser Agent schreibt nur JSON. Das Rendern uebernimmt `hexagonal-architect`.
- **Konflikt mit PlantUML**: Wenn zusaetzlich `.puml`-Inputs vorliegen, konfligiert: PlantUML hat Vorrang bei Struktur, Beschreibung hat Vorrang bei Intent (UseCase-Namen, Rollen, Fachregeln).
- **Tenant-Felder nicht duplizieren**. `id`, `tenant_id`, `created_at`, `created_by`, `modified_at`, `modified_by`, `version_number` sind immer vorhanden — nicht in das JSON schreiben.

## Output an den Nutzer (am Ende)

Kurzes Summary als Textantwort:

- Wie viele BCs / Aggregates / UseCases / Prozesse / Regeln wurden erkannt?
- Welche Elemente sind `TODO` und brauchen Input?
- Wo liegen die JSON-Dateien?
