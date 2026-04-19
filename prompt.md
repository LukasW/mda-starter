# Fachliche Spezifikation – Contract Lifecycle Management (CLM)

**Dokument-ID:** CLM-SPEC-001  
**Version:** 1.1  
**Status:** Draft  
**Erstellt:** 2026-04-19  
**Letzte Änderung:** 2026-04-19 – Offene Punkte OP-01 bis OP-07 entschieden  
**Autoren:** [Architekt], [Fachbereich]

---

## Inhaltsverzeichnis

1. [Einleitung und Zweck](#1-einleitung-und-zweck)
2. [Systemabgrenzung](#2-systemabgrenzung)
3. [Akteure](#3-akteure)
4. [Funktionale Sicht – Use Cases](#4-funktionale-sicht--use-cases)
5. [Strukturelle Sicht – Domänenmodell](#5-strukturelle-sicht--domänenmodell)
6. [Prozessuale Sicht – Genehmigungsworkflow](#6-prozessuale-sicht--genehmigungsworkflow)
7. [Schnittstellen zu externen Systemen](#7-schnittstellen-zu-externen-systemen)
   - 7.1 [Externe Archivlösung](#71-externe-archivlösung)
   - 7.2 [Externe Personenverwaltung](#72-externe-personenverwaltung)
8. [Rollen- und Rechtekonzept (RBAC)](#8-rollen--und-rechtekonzept-rbac)
9. [Statusmodell](#9-statusmodell)
10. [Compliance und Aufbewahrung](#10-compliance-und-aufbewahrung)
11. [Audit-Trail](#11-audit-trail)
12. [Architekturentscheide](#12-architekturentscheide)
13. [Offene Punkte](#13-offene-punkte)

---

## 1. Einleitung und Zweck

Dieses Dokument beschreibt die **fachliche Spezifikation** einer Contract-Lifecycle-Management-Applikation (CLM). Es definiert den funktionalen Umfang, die Systemgrenzen, die Akteure sowie die Schnittstellen zu externen Systemen.

### Ziel

Das CLM-System unterstützt den gesamten Lebenszyklus von Verträgen: von der Erstellung über die rechtliche Prüfung und digitale Unterzeichnung bis zur revisionssicheren Archivierung. Das System ist eigenständig lauffähig, kann aber optionale Integrationen zu einem **externen Archiv** und einer **externen Personenverwaltung** nutzen.

### Abgrenzung zum technischen Design

Diese Spezifikation ist rein fachlich. Sie enthält keine Entscheidungen zu Technologien, Frameworks oder Deployment-Infrastruktur.

---

## 2. Systemabgrenzung

```
┌─────────────────────────────────────────────────────────┐
│                      CLM-System                         │
│                                                         │
│  ┌──────────────┐  ┌────────────┐  ┌────────────────┐  │
│  │ Vertrags-    │  │ Workflow-  │  │ Signatur-      │  │
│  │ management   │  │ Engine     │  │ Integration    │  │
│  └──────────────┘  └────────────┘  └────────────────┘  │
│  ┌──────────────┐  ┌────────────────────────────────┐   │
│  │ Benach-      │  │ Audit-Trail / Protokollierung  │   │
│  │ richtigungen │  └────────────────────────────────┘   │
│  └──────────────┘                                       │
└────────┬────────────────────────┬────────────────────────┘
         │  REST-API (optional)   │  REST-API (optional)
         ▼                        ▼
┌─────────────────┐   ┌───────────────────────────────┐
│ Externes Archiv │   │ Externe Personenverwaltung    │
│ (z. B. DMS/ECM) │   │ (z. B. ERP, HR, AD/LDAP)     │
└─────────────────┘   └───────────────────────────────┘
```

### Eigenständiger Betrieb

Das CLM-System **muss ohne externe Systeme vollständig funktionieren**. Externe Integrationen sind optionale Erweiterungen:

| Modus | Archiv | Personenverwaltung | Beschreibung |
|---|---|---|---|
| Standalone | Intern (DB) | Intern (Benutzerverwaltung) | Vollständig selbstständig |
| Hybrid Archiv | Extern | Intern | Dokumente werden extern abgelegt |
| Hybrid Personen | Intern | Extern | Benutzerdaten kommen aus externem System |
| Vollintegriert | Extern | Extern | Maximale Integration |

---

## 3. Akteure

| Akteur | Beschreibung | Primäre Aufgaben im CLM |
|---|---|---|
| **Sachbearbeiter** | Erstellt und verwaltet Verträge im Tagesgeschäft | Vertrag erfassen, Metadaten setzen, Prüfung einreichen |
| **Rechtsabteilung** | Prüft Verträge auf rechtliche Konformität | Prüfung, Kommentieren, Genehmigen oder Ablehnen |
| **Geschäftsführung** | Autorisiert und unterzeichnet Verträge | Digitale Signatur, finale Freigabe |
| **Administrator** | Verwaltet Konfiguration und Benutzerzugänge | RBAC-Pflege, Systemkonfiguration, Berichte |
| **Externes Archiv** | Optionales Fremdsystem für Langzeitarchivierung | Dokumentenablage, Retrieval |
| **Externe Personenverwaltung** | Optionales Fremdsystem für Benutzer-/Partnerdaten | Personenstammdaten liefern |

---

## 4. Funktionale Sicht – Use Cases

### Use-Case-Übersicht

```
@startuml
left to right direction
skinparam packageStyle rectangle

actor "Sachbearbeiter" as SB
actor "Rechtsabteilung" as RA
actor "Geschäftsführung" as GF
actor "Administrator" as AD
actor "Ext. Archiv" as ARC <<extern>>
actor "Ext. Personenverwaltung" as PV <<extern>>

rectangle "CLM System" {
    usecase "Vertrag erstellen" as UC01
    usecase "Metadaten erfassen" as UC02
    usecase "Person suchen / zuordnen" as UC03
    usecase "Vertragshierarchie verwalten" as UC04
    usecase "Vertrag zur Prüfung einreichen" as UC05
    usecase "Rechtliche Prüfung durchführen" as UC06
    usecase "Kommentar / Korrektur erfassen" as UC07
    usecase "Digitale Signatur anfordern" as UC08
    usecase "Vertrag unterzeichnen" as UC09
    usecase "Vertrag archivieren" as UC10
    usecase "Vertrag suchen / abrufen" as UC11
    usecase "Benachrichtigung senden" as UC12
    usecase "Audit-Protokoll einsehen" as UC13
    usecase "Benutzer und Rollen verwalten" as UC14
}

SB --> UC01
SB --> UC02
SB --> UC03
SB --> UC04
SB --> UC05
SB --> UC11
RA --> UC06
RA --> UC07
GF --> UC09
GF --> UC11
AD --> UC13
AD --> UC14

UC05 ..> UC06 : <<include>>
UC06 ..> UC07 : <<extend>>
UC06 ..> UC08 : <<include>>
UC08 ..> UC09 : <<include>>
UC09 ..> UC10 : <<include>>
UC10 ..> ARC : <<extend>>
UC03 ..> PV : <<extend>>
UC12 ..> SB
UC12 ..> RA
UC12 ..> GF
@enduml
```

### Use-Case-Steckbriefe (Auswahl)

#### UC01 – Vertrag erstellen

| Feld | Inhalt |
|---|---|
| **ID** | UC01 |
| **Name** | Vertrag erstellen |
| **Auslöser** | Sachbearbeiter initiiert neuen Vertrag |
| **Vorbedingung** | Benutzer ist angemeldet und hat Rolle «Sachbearbeiter» |
| **Nachbedingung** | Vertrag liegt im Status «Entwurf» vor |
| **Normalablauf** | 1. Benutzer wählt Vertragstyp. 2. System erstellt Entwurf mit eindeutiger ID. 3. Benutzer erfasst Metadaten (UC02). 4. System speichert Entwurf. |
| **Alternativen** | Vertrag als Kopie eines bestehenden Vertrags anlegen. |
| **Fehler** | Ungültiger Vertragstyp → Fehlermeldung, kein Entwurf angelegt. |

#### UC03 – Person suchen / zuordnen

| Feld | Inhalt |
|---|---|
| **ID** | UC03 |
| **Name** | Person suchen / zuordnen |
| **Auslöser** | Benutzer möchte Vertragspartei, Unterzeichner oder Zuständigen benennen |
| **Vorbedingung** | Vertrag liegt im Status «Entwurf» oder «Korrekturbedarf» |
| **Normalablauf (Standalone)** | Suche in interner Benutzerverwaltung. Auswahl und Zuordnung zur Vertragsrolle. |
| **Erweiterung (extern)** | Falls externe Personenverwaltung konfiguriert: Suche via API. Ergebnis wird lokal gecacht. |
| **Geschäftsregel** | Jede Vertragspartei muss mit Vor- und Nachname, Funktion und E-Mail-Adresse erfasst sein. |

#### UC10 – Vertrag archivieren

| Feld | Inhalt |
|---|---|
| **ID** | UC10 |
| **Name** | Vertrag archivieren |
| **Auslöser** | Vertrag erreicht Status «Unterzeichnet» |
| **Vorbedingung** | Alle Signaturen vorhanden. Audit-Trail vollständig. |
| **Normalablauf (Standalone)** | Dokument wird in interner Datenbank revisionssicher abgelegt. Metadaten indexiert. |
| **Erweiterung (extern)** | Falls externes Archiv konfiguriert: Dokument wird via API übergeben. Externe Referenz-ID wird lokal gespeichert. CLM behält Metadaten und Verknüpfungen. |
| **Nachbedingung** | Vertrag ist nicht mehr editierbar. Löschung nur gemäss Löschkonzept. |

---

## 5. Strukturelle Sicht – Domänenmodell

```
@startuml
skinparam classAttributeIconSize 0

class Vertrag {
    + id: UUID
    + titel: String
    + typ: VertragsTyp
    + status: WorkflowStatus
    + erstelltAm: DateTime
    + gueltigVon: Date
    + gueltigBis: Date
    + erstelleVersion(): VertragsVersion
    + archivieren(): void
}

enum VertragsTyp {
    LIEFERANTENVERTRAG
    KUNDENVERTRAG
    ARBEITSVERTRAG
    KOOPERATIONSVERTRAG
    SONSTIGES
}

enum WorkflowStatus {
    ENTWURF
    IN_PRUEFUNG
    KORREKTURBEDARF
    FREIGEGEBEN
    ZUR_SIGNATUR
    UNTERZEICHNET
    ARCHIVIERT
    ABGELAUFEN
    GEKUENDIGT
}

class VertragsVersion {
    + versionNummer: Integer
    + erstellt: DateTime
    + erstelltVon: Person
    + inhaltHash: String
    + dokumentReferenz: DokumentReferenz
}

class DokumentReferenz {
    + speicherTyp: SpeicherTyp
    + pfadLokal: String
    + archivExternId: String
    + mimeType: String
    + groesseByte: Long
}

enum SpeicherTyp {
    INTERN
    ARCHIV_EXTERN
}

class Vertragshierarchie {
    + parentId: UUID
    + childId: UUID
    + beziehungsTyp: String
}

class VertragsPartei {
    + rolle: ParteiRolle
    + person: Person
}

enum ParteiRolle {
    AUFTRAGGEBER
    AUFTRAGNEHMER
    UNTERZEICHNER
    INFORMIERT
    VERANTWORTLICH
}

class Person {
    + id: UUID
    + vorname: String
    + nachname: String
    + email: String
    + organisation: String
    + quelleTyp: PersonenQuelle
    + externeId: String
}

enum PersonenQuelle {
    INTERN
    EXTERN_API
}

class WorkflowTask {
    + id: UUID
    + aufgabenTyp: AufgabenTyp
    + status: TaskStatus
    + zuweisung: Person
    + faelligAm: DateTime
    + kommentar: String
}

enum AufgabenTyp {
    RECHTLICHE_PRUEFUNG
    FREIGABE
    SIGNATUR
    ARCHIVIERUNG
}

enum TaskStatus {
    OFFEN
    IN_BEARBEITUNG
    ABGESCHLOSSEN
    ABGELEHNT
}

class SignaturProzess {
    + id: UUID
    + signaturServiceId: String
    + status: SignaturStatus
    + initiiert: DateTime
    + abgeschlossen: DateTime
    + ablaufDatum: Date
}

enum SignaturStatus {
    AUSSTEHEND
    TEILWEISE_SIGNIERT
    VOLLSTAENDIG_SIGNIERT
    ABGELAUFEN
    ABGEBROCHEN
}

class AuditEreignis {
    + id: UUID
    + zeitstempel: DateTime
    + ereignisTyp: String
    + ausgeloestVon: Person
    + details: String
    + vorhergehenderStatus: WorkflowStatus
    + neuerStatus: WorkflowStatus
}

Vertrag "1" -- "0..*" Vertragshierarchie : besitzt
Vertrag "1" -- "1..*" VertragsVersion : hat
Vertrag "1" -- "0..*" VertragsPartei : umfasst
Vertrag "1" -- "0..*" WorkflowTask : durchlaeuft
Vertrag "1" -- "0..1" SignaturProzess : erfordert
Vertrag "1" -- "0..*" AuditEreignis : protokolliert
VertragsVersion "1" -- "1" DokumentReferenz : referenziert
VertragsPartei "1" -- "1" Person : benennt
@enduml
```

### Zentrale Geschäftsregeln

- Ein Vertrag hat immer **mindestens eine Version**. Jede Änderung am Inhalt erzeugt eine neue Version.
- Ein Vertrag kann **Eltern- und Kindverträge** haben (z. B. Rahmenvertrag → Einzelauftrag).
- Metadaten und Verknüpfungen bleiben stets im CLM, auch wenn das Dokument extern archiviert wird.
- Eine `Person` kann aus dem internen Verzeichnis oder aus der **externen Personenverwaltung** stammen; `quelleTyp` kennzeichnet dies explizit.

---

## 6. Prozessuale Sicht – Genehmigungsworkflow

### Happy Path – Sequenzdiagramm

```
@startuml
autonumber
actor Sachbearbeiter as SB
participant "CLM System" as Sys
participant "Ext. Personenverwaltung" as PV <<extern, optional>>
actor "Rechtsabteilung" as RA
actor "Geschäftsführung" as GF
participant "Ext. Archiv" as ARC <<extern, optional>>

SB -> Sys: Neuen Vertrag anlegen (Typ, Titel)
Sys -> Sys: Vertrag-ID generieren, Status = ENTWURF
SB -> Sys: Metadaten erfassen
SB -> Sys: Person suchen (Name / ID)
opt Externe Personenverwaltung konfiguriert
    Sys -> PV: GET /persons?query=...
    PV --> Sys: Personenstammdaten
end
Sys --> SB: Personenvorschläge anzeigen
SB -> Sys: Person zuordnen und speichern
SB -> Sys: Dokument hochladen (neue Version)
SB -> Sys: Zur Prüfung einreichen
Sys -> Sys: Status = IN_PRUEFUNG
Sys -> RA: Benachrichtigung senden (E-Mail / System)

RA -> Sys: Vertrag prüfen

alt Genehmigt
    RA -> Sys: Freigabe bestätigen
    Sys -> Sys: Status = FREIGEGEBEN
    Sys -> Sys: Status = ZUR_SIGNATUR
    Sys -> GF: Benachrichtigung Signaturanforderung
    GF -> Sys: Digitale Signatur leisten
    Sys -> Sys: SignaturProzess abschliessen
    Sys -> Sys: Status = UNTERZEICHNET
    Sys -> Sys: Audit-Eintrag erstellen
    opt Externes Archiv konfiguriert
        Sys -> ARC: POST /documents (Dokument + Metadaten)
        ARC --> Sys: archivExternId
        Sys -> Sys: DokumentReferenz.speicherTyp = ARCHIV_EXTERN
    end
    Sys -> Sys: Status = ARCHIVIERT
    Sys -> SB: Benachrichtigung «Vertrag archiviert»

else Abgelehnt / Korrekturbedarf
    RA -> Sys: Ablehnen mit Kommentar
    Sys -> Sys: Status = KORREKTURBEDARF
    Sys -> SB: Benachrichtigung mit Kommentar
    SB -> Sys: Korrekturen vornehmen, neue Version erstellen
    SB -> Sys: Erneut zur Prüfung einreichen
    note right: Prozess wiederholt ab Schritt 9

end
@enduml
```

### Statusübergänge (Übersicht)

```
ENTWURF
  │ Einreichen (SB)
  ▼
IN_PRUEFUNG
  │ Genehmigen (RA)          │ Ablehnen (RA)
  ▼                          ▼
FREIGEGEBEN            KORREKTURBEDARF
  │                          │ Erneut einreichen (SB)
  ▼                          └──────────┐
ZUR_SIGNATUR                            ▼
  │ Signiert (GF)                  IN_PRUEFUNG
  ▼
UNTERZEICHNET
  │ Archivieren (System)
  ▼
ARCHIVIERT
  │ Ablauf Gültigkeitsdatum
  ▼
ABGELAUFEN
```

---

## 7. Schnittstellen zu externen Systemen

Beide externen Systeme sind **optional**. Das CLM muss ohne sie vollständig funktionieren. Die Konfiguration erfolgt je Mandant durch den Administrator.

### 7.1 Externe Archivlösung

#### Zweck

Revisionssichere Langzeitarchivierung von unterzeichneten Vertragsdokumenten gemäss gesetzlicher Aufbewahrungspflicht.

#### Wann wird das externe Archiv verwendet?

Nur wenn `ARCHIV_EXTERN_ENABLED = true` konfiguriert ist. Im Standalone-Modus speichert das CLM Dokumente intern und übernimmt selbst die Aufbewahrungsverantwortung.

#### Datenfluss

| Richtung | Auslöser | Inhalt |
|---|---|---|
| CLM → Archiv | Vertrag erreicht Status UNTERZEICHNET | Dokument (PDF/A), Metadaten (JSON), Signaturnachweise |
| Archiv → CLM | Auf Anfrage (Retrieval) | Dokumentbytes, Archiv-Metadaten |

#### Schnittstellenspezifikation (REST)

```
POST /api/v1/archive/documents
Content-Type: multipart/form-data

Felder:
  document       (binary, PDF/A)
  metadata       (JSON):
    vertragId    (UUID)
    titel        (String)
    typ          (VertragsTyp)
    unterzeichnetAm (DateTime ISO-8601)
    gueltigBis   (Date)
    parteien     (Array<{name, email, rolle}>)

Response 201:
  { "archivId": "string", "zeitstempel": "ISO-8601" }

---

GET /api/v1/archive/documents/{archivId}
Response 200: { "url": "...", "mimeType": "application/pdf", "gueltigBis": "..." }
```

#### Verhalten bei Nichterreichbarkeit

- Das CLM speichert das Dokument intern (Fallback-Speicher).
- Regelmässige Retry-Mechanismus (konfigurierbar, z. B. alle 15 Minuten).
- Administratoren werden per Benachrichtigung informiert.
- Der Vertragsstatus bleibt UNTERZEICHNET, bis die Archivierung bestätigt ist.

#### Verantwortlichkeitsteilung

| Verantwortung | CLM | Externes Archiv |
|---|---|---|
| Metadaten und Verknüpfungen | ✓ | – |
| Versionierung | ✓ | – |
| Workflow-Status | ✓ | – |
| Langzeitspeicherung Dokument | Fallback | ✓ (primär) |
| Unveränderlichkeit / Integrität | ✓ (Hash) | ✓ |
| Aufbewahrungsfristen | ✓ (Steuerung) | ✓ (Durchsetzung) |

---

### 7.2 Externe Personenverwaltung

#### Zweck

Stammdaten zu Personen (Mitarbeitende, Vertragspartner) aus einem bestehenden Quellsystem beziehen, ohne Datendopplung.

#### Wann wird die externe Personenverwaltung verwendet?

Nur wenn `PERSONENVERWALTUNG_EXTERN_ENABLED = true` konfiguriert ist. Im Standalone-Modus verwaltet das CLM Benutzer und Kontakte intern.

#### Datenfluss

| Richtung | Auslöser | Inhalt |
|---|---|---|
| CLM → PV | Personensuche durch Benutzer | Suchbegriff (Name, E-Mail, ID) |
| PV → CLM | Suchergebnis | Personenstammdaten (lesend) |

#### Schnittstellenspezifikation (REST)

```
GET /api/v1/persons?query={suchbegriff}&limit=20
Response 200:
[
  {
    "externeId": "string",
    "vorname":   "string",
    "nachname":  "string",
    "email":     "string",
    "organisation": "string",
    "funktion":  "string"
  }
]

---

GET /api/v1/persons/{externeId}
Response 200: { ... gleiche Felder ... }
```

#### Datenhaltung im CLM

- Das CLM **schreibt nie** in die externe Personenverwaltung.
- Zugeordnete Personen werden lokal gecacht (Snapshot), damit Verträge auch bei Nichterreichbarkeit des externen Systems lesbar bleiben.
- Der Cache wird bei erneuter Zuordnung aktualisiert.
- `Person.quelleTyp = EXTERN_API` und `Person.externeId` markieren den Ursprung.

#### Verhalten bei Nichterreichbarkeit

- Suche fällt auf internen Cache zurück.
- Benutzer wird informiert («Personensuche temporär eingeschränkt»).
- Bereits zugeordnete Personen bleiben vollständig verfügbar.

---

## 8. Rollen- und Rechtekonzept (RBAC)

### Rollen

| Rolle | Beschreibung |
|---|---|
| `SACHBEARBEITER` | Erstellt und verwaltet eigene Verträge |
| `RECHTSABTEILUNG` | Prüft und kommentiert Verträge; kann genehmigen oder ablehnen |
| `GESCHAEFTSFUEHRUNG` | Unterzeichnet freigegebene Verträge |
| `LESERECHT` | Kann Verträge einsehen (kein Schreiben, keine Aktionen) |
| `ADMINISTRATOR` | Konfiguriert das System, verwaltet Benutzer und Rollen |

### Berechtigungsmatrix

| Aktion | SACHBEARBEITER | RECHTSABTEILUNG | GESCHAEFTSFUEHRUNG | LESERECHT | ADMINISTRATOR |
|---|:---:|:---:|:---:|:---:|:---:|
| Vertrag erstellen | ✓ | – | – | – | ✓ |
| Eigene Entwürfe bearbeiten | ✓ | – | – | – | ✓ |
| Fremde Entwürfe bearbeiten | – | – | – | – | ✓ |
| Zur Prüfung einreichen | ✓ | – | – | – | – |
| Rechtliche Prüfung | – | ✓ | – | – | – |
| Genehmigen / Ablehnen | – | ✓ | – | – | – |
| Unterzeichnen | – | – | ✓ | – | – |
| Alle Verträge lesen | – | ✓ | ✓ | ✓ | ✓ |
| Eigene Verträge lesen | ✓ | – | – | – | – |
| Audit-Trail einsehen | – | – | – | – | ✓ |
| Benutzer verwalten | – | – | – | – | ✓ |
| Systemkonfiguration | – | – | – | – | ✓ |

### Geschäftsregeln Berechtigung

- Ein Sachbearbeiter sieht nur Verträge, an denen er beteiligt ist.
- Die Rechtsabteilung sieht alle Verträge im Status `IN_PRUEFUNG`.
- Die Geschäftsführung sieht alle Verträge im Status `ZUR_SIGNATUR` und alle unterzeichneten Verträge.
- Administratoren haben Lesezugriff auf alle Verträge, aber dürfen keine Prüf- oder Signaturaktionen auslösen.

---

## 9. Statusmodell

| Status | Beschreibung | Erlaubte Folgestatus |
|---|---|---|
| `ENTWURF` | Vertrag in Bearbeitung, noch nicht eingereicht | IN_PRUEFUNG |
| `IN_PRUEFUNG` | Rechtliche Prüfung läuft | FREIGEGEBEN, KORREKTURBEDARF |
| `KORREKTURBEDARF` | Zurückgewiesen, Nachbesserung erforderlich | IN_PRUEFUNG |
| `FREIGEGEBEN` | Rechtlich geprüft und genehmigt | ZUR_SIGNATUR |
| `ZUR_SIGNATUR` | Signaturprozess initiiert | UNTERZEICHNET |
| `UNTERZEICHNET` | Alle Signaturen vorhanden | ARCHIVIERT |
| `ARCHIVIERT` | Revisionssicher abgelegt, nicht mehr editierbar | ABGELAUFEN, GEKUENDIGT |
| `ABGELAUFEN` | Gültigkeitsdatum überschritten | – |
| `GEKUENDIGT` | Vorzeitig beendet | – |

---

## 10. Compliance und Aufbewahrung

### Aufbewahrungsfristen (Schweizer Recht / OR)

| Vertragstyp | Mindestfrist | Grundlage |
|---|---|---|
| Handelskorrespondenz / Verträge allgemein | 10 Jahre | Art. 958f OR |
| Arbeitsverträge | 10 Jahre nach Beendigung | OR, ArG |
| Versicherungsverträge (KVG/VVG) | 10 Jahre | Branchenspezifisch |
| Sonstige Dokumente | 5 Jahre | OR |

### Löschkonzept

- Verträge im Status ARCHIVIERT dürfen **nicht manuell gelöscht** werden.
- Das System berechnet automatisch das **Löschdatum** = Archivierungsdatum + Aufbewahrungsfrist.
- Löschungen erfolgen ausschliesslich durch automatisierte Prozesse nach Ablauf der Frist.
- Vor der Löschung wird ein Audit-Eintrag mit vollständigen Metadaten erzeugt.
- Bei Aktivierung des externen Archivs: Löschanfrage wird auch an das externe Archiv gesendet.

### nDSG / DSGVO

- Personenbezogene Daten in Vertragsparteien unterliegen dem nDSG.
- Das Recht auf Auskunft und das Recht auf Löschung werden unterstützt, soweit nicht gesetzliche Aufbewahrungsfristen entgegenstehen.
- Gespeicherte externe Personen-IDs (`externeId`) sind als datenschutzrelevant zu klassifizieren.

---

## 11. Audit-Trail

### Zu protokollierende Ereignisse (zwingend)

| Ereignistyp | Zeitstempel | Benutzer | Details |
|---|---|---|---|
| Vertrag erstellt | ✓ | ✓ | Typ, Titel |
| Version erstellt | ✓ | ✓ | Versionsnummer, Dokumenthash |
| Status geändert | ✓ | ✓ | Von-Status → Nach-Status |
| Person zugeordnet | ✓ | ✓ | Person-ID, Quelletyp, Rolle |
| Kommentar erfasst | ✓ | ✓ | Kommentartext (gekürzt) |
| Signatur geleistet | ✓ | ✓ | Signatur-Service-ID |
| Dokument archiviert | ✓ | System | Archiv-ID, Speicherort |
| Berechtigungsänderung | ✓ | ✓ | Alte / neue Rolle |
| Zugriff verweigert | ✓ | ✓ | Ressource, Grund |
| Externer API-Aufruf | ✓ | System | Ziel-URL, HTTP-Status |

### Anforderungen an den Audit-Trail

- Unveränderlich: Audit-Einträge können nicht modifiziert oder gelöscht werden.
- Vollständig: Jede zustandsverändernde Aktion erzeugt einen Eintrag.
- Nachvollziehbar: Jeder Eintrag referenziert den auslösenden Benutzer und den betroffenen Vertrag.
- Exportierbar: Audit-Trail muss als CSV oder PDF exportierbar sein (für Revisionen).

---

## 12. Architekturentscheide

Diese Entscheide wurden im Rahmen der Spezifikationsreview festgelegt und ersetzen die ursprünglichen offenen Punkte OP-01 bis OP-07.

### AE-01 – Authentifizierung

**Entscheid:** Hybrid – SSO/OIDC als primärer Anmeldemechanismus, lokale Fallback-Accounts für Notbetrieb und administrative Zwecke.

**Konsequenzen:**
- Die Applikation implementiert einen OIDC-konformen Identity-Provider-Adapter (z. B. Keycloak, Azure AD).
- Lokale Accounts werden separat verwaltet und sind auf ein Minimum zu beschränken (Administratoren, Systemaccounts).
- Benutzerrollen können aus OIDC-Claims übernommen oder im CLM lokal übersteuert werden.
- Passwort-Reset und MFA werden vollständig an den externen Identity Provider delegiert.

---

### AE-02 – Externes Archiv

**Entscheid:** Anbieter noch offen – Evaluation ausstehend. Die CLM-Schnittstelle wird als **generischer Archiv-Adapter** spezifiziert.

**Konsequenzen:**
- Die API-Spezifikation in Kapitel 7.1 definiert den Minimalkontrakt; konkrete Anbieter-Implementierungen werden als Adapter realisiert.
- Die Adapter-Schnittstelle muss austauschbar sein, ohne den CLM-Kern zu verändern.
- Evaluationskriterien für den Anbieter: ZertES-Konformität (CH), CMIS-Unterstützung, SLA ≥ 99.9%, Datenhaltung in der Schweiz.
- **Ausstehend:** Sobald Anbieter gewählt, muss Kapitel 7.1 mit konkreter API-Spezifikation ergänzt werden.

---

### AE-03 – Signatur-Dienstleister

**Entscheid:** Anbieter noch offen – Evaluation ausstehend. Die Integration wird als **austauschbarer Signatur-Adapter** realisiert.

**Konsequenzen:**
- Der Adapter kapselt alle Anbieter-spezifischen API-Details (Webhook-Format, Signaturstatus-Mapping, Authentifizierung).
- Fachliche Mindestanforderungen an den Anbieter: qualifizierte elektronische Signatur (QES) gemäss ZertES/eIDAS, Schweizer Datenhaltung bevorzugt.
- Kandidaten zur Evaluation: Skribble (ZertES-nativ, CH), DocuSign (eIDAS), Adobe Sign.
- **Ausstehend:** Sobald Anbieter gewählt, Adapter-Implementierung und konkrete Webhook-Spezifikation ergänzen.

---

### AE-04 – Mehrsprachigkeit

**Entscheid:** i18n-Architektur von Beginn an vorsehen; initialer Release ausschliesslich auf Deutsch (DE).

**Konsequenzen:**
- Alle UI-Texte, Fehlermeldungen und E-Mail-Templates werden in Ressourcendateien externalisiert (kein Hardcoding).
- Weitere Sprachen (FR, IT) werden in späteren Releases ergänzt, ohne Architekturänderung.
- Datumsformate, Zahlenformate und Sortierung werden locale-fähig implementiert.

---

### AE-05 – Mandantenfähigkeit

**Entscheid:** Mehrere Gesellschaften mit **getrennter Datenhaltung** (Schema- oder Datenbankisolation je Mandant).

**Konsequenzen:**
- Jeder Mandant erhält ein isoliertes Datenschema; mandantenübergreifende Abfragen sind architektonisch ausgeschlossen.
- RBAC, Benutzer, Konfigurationen und externe Systemanbindungen werden je Mandant separat verwaltet.
- Mandantenauswahl erfolgt bei Anmeldung oder via subdomain-basiertes Routing.
- Shared-Services (Benachrichtigungen, Audit-Infrastruktur) bleiben mandantenfähig durch Mandanten-ID in allen Ereignissen.

---

### AE-06 – SLA und Verfügbarkeit

**Entscheid:** CLM-System 99.9% Verfügbarkeit; externe Systeme (Archiv, Personenverwaltung, Signatur) Best-Effort.

**Konsequenzen:**
- Das CLM muss bei Ausfall jedes einzelnen externen Systems vollständig funktionsfähig bleiben (vgl. Fallback-Strategien in Kapitel 7).
- Für jede externe Abhängigkeit ist ein Timeout (max. 5 Sekunden) und ein Retry-Mechanismus zu implementieren.
- Monitoring und Alerting für externe Systemverfügbarkeit ist Betriebspflicht.
- Wartungsfenster für das CLM: maximal 4 Stunden/Monat, ausserhalb Geschäftszeiten.

---

### AE-07 – Datenschutz-Folgeabschätzung (DSFA)

**Entscheid:** DSFA als nicht erforderlich eingestuft – **Begründung muss formal dokumentiert werden.**

**Konsequenzen:**
- Die Begründung muss durch den zuständigen Datenschutzverantwortlichen schriftlich festgehalten und abgezeichnet werden.
- Zu prüfende Kriterien gemäss nDSG Art. 22: systematische Verarbeitung von Personendaten, besonders schützenswerte Daten, Verknüpfung mit externen Personenstammdaten.
- **Hinweis:** Die Verarbeitung von Personendaten aus einer externen Personenverwaltung (OP-02 / Kapitel 7.2) kann eine DSFA-Pflicht begründen. Die Einschätzung «nicht erforderlich» ist vor Go-Live durch den Datenschutzbeauftragten zu bestätigen.
- **Ausstehend:** Formale Begründung und Unterschrift Datenschutzverantwortlicher.

---

## 13. Offene Punkte

| ID | Thema | Zuständig | Abhängigkeit | Fällig |
|---|---|---|---|---|
| OP-A | Archiv-Anbieter evaluieren und Adapter-API konkretisieren | Einkauf / Architektur | AE-02 | Nach Evaluation |
| OP-B | Signatur-Anbieter evaluieren und Adapter konkretisieren | Einkauf / IT | AE-03 | Nach Evaluation |
| OP-C | DSFA-Begründung formal dokumentieren und abzeichnen | Datenschutzbeauftragter | AE-07 | Vor Go-Live |

---

*Ende der fachlichen Spezifikation – CLM-SPEC-001 v1.1*