**Spezifikation**

**Model-Driven Application Platform**

*Technologieneutrale Referenzspezifikation für den Nachbau einer modellgetriebenen Anwendungsplattform*

Version 1.0

Status: Draft

# **Inhaltsverzeichnis**

# **1\. Einführung**

## **1.1 Zweck des Dokuments**

Dieses Dokument spezifiziert eine modellgetriebene Anwendungsplattform in technologieneutraler Form. Ziel ist es, die wesentlichen Konzepte, Artefakte, Laufzeitdienste und Schnittstellen so präzise zu beschreiben, dass auf dieser Grundlage eine Eigenentwicklung in einer beliebigen Ziel-Technologie (z. B. Java/Quarkus, Kotlin/Spring, .NET, Node.js, Rust) erfolgen kann. Die Spezifikation beschreibt das Was, nicht das Wie – konkrete Frameworks, Produkte und Bibliotheken sind bewusst nicht festgelegt.

## **1.2 Zielgruppe**

Softwarearchitektinnen und \-architekten, Plattform- und Produktteams, technische Projektleitungen sowie Compliance- und Security-Verantwortliche, die eine interne Low-Code-Plattform für fachliche Anwendungen mit starkem Prozess- und CRUD-Anteil konzipieren oder aufbauen wollen.

## **1.3 Abgrenzung**

Die Spezifikation deckt Laufzeitverhalten, Modellstruktur, Engine-Dienste und Schnittstellen ab. Nicht Teil dieses Dokuments sind: konkrete UI-Design-Richtlinien, ein verbindlicher Look-and-Feel, spezifische Reporting-/Analytics-Engines sowie produktbezogene Entscheidungen zur Zielplattform. Stark individualisierte, pixelgenaue Public-Facing-Anwendungen sind ausdrücklich nicht das Ziel dieses Plattformtyps; sie gehören in ein eigenständiges UI-Framework.

## **1.4 Begriffe und Definitionen**

| Begriff | Definition |
| :---- | :---- |
| Modell | Deklarative Beschreibung einer Anwendung (Entitäten, Regeln, UI-Struktur, Navigation, Sicherheit, Prozesse) in einem plattformeigenen Schema. |
| Metamodell | Schema, gegen das Modelle validiert werden; beschreibt die zulässigen Elemente, Attribute und Beziehungen eines Modells. |
| Engine / Runtime | Der serverseitige Laufzeitdienst, der ein Modell interpretiert, Daten verwaltet, Regeln ausführt und APIs bereitstellt. |
| Renderer | Der clientseitige Dienst, der aus Modell und Daten dynamisch eine UI erzeugt. |
| Entität | Typ eines fachlichen Datenobjekts (entspricht einer Tabelle/Aggregate-Root). |
| Attribut | Einzelnes Feld einer Entität mit Typ, Constraints und Metadaten. |
| Beziehung | Typisierte Verknüpfung zwischen Entitäten (1:1, 1:n, n:m). |
| Datensatz | Konkrete Instanz einer Entität zur Laufzeit. |
| Ansicht (View) | Deklarative, gefilterte und sortierte Liste über Entitätsdaten. |
| Formular (Form) | Deklarative Eingabe-/Darstellungssicht auf einen einzelnen Datensatz. |
| Dashboard | Zusammengesetzte Ansicht aus Kennzahlen, Diagrammen und Listen. |
| Prozess (BPF) | Geführter, mehrstufiger Fachprozess mit Stadien, Kriterien und optionalen Entitätswechseln. |
| Regel | Deklarative Logik (Validierung, Ableitung, Sichtbarkeit, Pflicht) mit definiertem Geltungsbereich. |
| Rolle | Benannte Sammlung von Privilegien, die einem Principal zugewiesen wird. |
| Solution | Paketierbare Menge versionierter Modellartefakte, inkl. Abhängigkeiten. |

# **2\. Grundprinzipien**

## **2.1 Modell vor Code**

Die Anwendung entsteht primär durch Konfiguration eines deklarativen Modells, nicht durch handgeschriebenen UI- oder CRUD-Code. Code ist eine gezielte Erweiterung für Fälle, die deklarativ nicht hinreichend abbildbar sind (komplexe Integrationen, domänenspezifische Berechnungen, regulatorisch begründete Sonderlogik).

## **2.2 Datenzentrierung**

Das Domänenmodell ist die Single Source of Truth. Formulare, Ansichten, Navigation, Prozesse, Berechtigungen und Reports leiten sich konsistent daraus ab. UI ist eine Projektion des Modells, keine eigenständige Quelle von Struktur.

## **2.3 Konsistenz durch Interpretation**

Das Modell wird zur Laufzeit interpretiert. UI-Konsistenz, Barrierefreiheit, Responsive-Verhalten, Mehrsprachigkeit, Audit, Berechtigungen und Validierung sind zentrale Plattformleistungen und keine Aufgabe der einzelnen Anwendung.

## **2.4 Deklarativer Vorrang**

Logik wird in dieser Reihenfolge realisiert: (1) Attribut-Constraints, (2) deklarative Business Rules am Formular, (3) serverseitige deklarative Prozesse/Workflows, (4) prozeduraler Code als letzte Eskalation. Höher gelistete Mittel haben Vorrang, weil sie besser prüfbar, versionierbar und portabel sind.

## **2.5 Versionierbarkeit und Portabilität**

Jedes Modellartefakt ist eigenständig versionierbar, paketierbar, exportierbar und importierbar. Umgebungsübergreifende Transporte (Dev → Test → Prod) erfolgen über schemavalidierte Pakete, nicht über manuelle Konfiguration pro Umgebung.

## **2.6 Multi-Tenancy als Standardfall**

Die Plattform ist so auszulegen, dass mehrere fachlich getrennte Anwendungen (Tenants/Organisationseinheiten) auf derselben Instanz sicher, daten- und berechtigungsisoliert betrieben werden können. Cross-Tenant-Zugriffe sind grundsätzlich unterbunden.

## **2.7 Sicherheit und Governance by Design**

Berechtigungen, Audit, Datenklassifizierung, Datenschutz-Kennzeichnungen und Retention-Policies sind Modellelemente erster Klasse, nicht nachgelagerte Infrastrukturthemen. Jede Datenzelle, jedes Feld und jede Aktion ist einer Berechtigungs- und ggf. Klassifizierungssemantik zugeordnet.

# **3\. Architekturüberblick**

## **3.1 Schichten**

Die Plattform besteht aus fünf logischen Schichten:

1. Persistenzschicht: relationale Primärablage der Entitätsdaten, Änderungs-/Audit-Log, Dokumentablage, Volltextindex.

2. Metamodell- und Modellspeicher: Verwaltung des Metamodells und aller Modellartefakte inkl. Versionen, Solutions, Abhängigkeiten.

3. Engine: Interpretation des Modells, CRUD-Dienste, Regel-Engine, Workflow-Engine, Ereignisverteilung, Berechtigungsprüfung, Audit.

4. Integrations- und API-Schicht: REST/OData-kompatible Kern-API, GraphQL (optional), Event-Bus-Anbindung, Webhooks, Dateischnittstellen, Auth/SSO.

5. Präsentationsschicht (Renderer): clientseitige Generierung von Listen, Formularen, Dashboards, Navigation, Prozessen aus Modell und API-Daten.

## **3.2 Logische Komponenten**

| Komponente | Verantwortung |
| :---- | :---- |
| Metadata Service | Lesen/Schreiben des Metamodells, Validierung, Publizieren, Versionsverwaltung, Solution-Handling. |
| Data Service (CRUD) | Generische Lese-/Schreibdienste über alle Entitäten, Filter, Sortierung, Paginierung, Projektionen. |
| Query/Search Service | Strukturierte Queries, gespeicherte Ansichten, Volltextsuche, aggregierte Abfragen (Chart/KPI). |
| Rule Engine | Ausführung deklarativer Regeln (Validierung, Ableitung, Pflicht, Sichtbarkeit) client- und serverseitig. |
| Workflow Engine | Ausführung ereignisgesteuerter und zeitbasierter Automationen, inkl. Kompensation und Retries. |
| Process Engine (BPF) | Zustandsautomat für geführte, mehrstufige, ggf. entitätsübergreifende Fachprozesse. |
| Security Service | Authentifizierung, Autorisierung, Rollen, Teams, Owner-Modell, feldbezogene Sicherheit, Row-Level-Filter. |
| Audit & Compliance | Lückenlose Änderungsprotokollierung, Zugriffsprotokolle, Retention, Legal Hold, Export für Aufsicht. |
| Event Bus Adapter | Outbox-basierte Produktion fachlicher Ereignisse, Konsum externer Ereignisse. |
| Notification Service | Benachrichtigungen (In-App, E-Mail, Push) auf Basis deklarativer Regeln. |
| Job Scheduler | Zeit- und Kalender-getriggerte Ausführungen, langlaufende Aufträge, Wiederaufsetzen. |
| Renderer (Client) | Dynamische UI-Generierung, lokale Regel-Evaluation, Offline-/Latenz-Optimierung. |
| Designer (Authoring) | Werkzeug zur Modellerstellung und \-bearbeitung mit Live-Validierung gegen das Metamodell. |
| Solution Manager | Paketierung, Transport, Import/Export, Upgrade, Deinstallation. |

## **3.3 Datenflüsse (High-Level)**

* Authoring: Designer schreibt Modell → Metadata Service validiert und versioniert → Publikation stellt Modell für die Engine bereit.

* Laufzeit-Anfrage: Client lädt Modell-Fragment (z. B. Formular) und Daten über Data Service; Renderer evaluiert Business Rules lokal; Server-Regeln werden beim Speichern erneut und autoritativ ausgeführt.

* Ereignisverteilung: Jeder fachliche Schreibvorgang erzeugt deterministisch ein Ereignis (Outbox-Muster), das an den Event Bus publiziert wird.

* Integration eingehend: Externe Systeme rufen die Kern-API oder publizieren Ereignisse, die deklarativ auf Workflows/Automationen gemappt werden.

# **4\. Metamodell**

## **4.1 Übersicht**

Das Metamodell definiert die zulässige Struktur aller Modellartefakte. Es ist selbst wieder strukturiert und versioniert. Anwendungsmodelle müssen gegen eine konkrete Metamodell-Version gültig sein. Die Plattform liefert zu jeder unterstützten Metamodell-Version ein maschinenlesbares Schema (z. B. JSON-Schema) und eine Referenzdokumentation.

## **4.2 Artefakttypen**

| Artefakttyp | Beschreibung |
| :---- | :---- |
| Entity | Entitätsdefinition mit Attributen, Schlüsseln, Beziehungen, Icons, Plural/Singular-Bezeichnungen, Lokalisierung. |
| Attribute | Feld einer Entität mit Datentyp, Constraints, Default, Formatierung, Klassifizierung, Lokalisierung. |
| Relationship | Typisierte Beziehung zwischen zwei Entitäten inkl. Kardinalität, Kaskaden- und Lösch-Verhalten, Navigationsnamen. |
| View | Definition einer Listenansicht (Entität, Spalten, Filter, Sortierung, Gruppierung, Aggregationen). |
| Form | Formulardefinition (Typ, Header, Tabs, Sections, Felder, Sub-Grids, QuickView, Regeln). |
| BusinessRule | Deklarative Formular-/Entitätsregel mit Bedingungen und Aktionen. |
| Workflow | Ereignis- oder zeitgesteuerte, serverseitige Automation mit Schritten, Bedingungen, Fehler-/Kompensationslogik. |
| ProcessFlow | Geführter Prozess mit Stadien, Schritten, Kriterien, optionalen Entitätsübergängen. |
| SiteMap | Navigationsstruktur (Bereiche, Gruppen, Einträge, Rollenbindung). |
| Dashboard | Zusammensetzung aus Charts, KPIs, Listen, Filtern. |
| Chart | Diagrammdefinition auf Basis einer Entität/Ansicht mit Aggregationen. |
| Role | Rolle mit Privilegien und optionalen Gültigkeitsregeln. |
| FieldSecurityProfile | Feldbezogene Sicherheits-Policies (Lesen/Schreiben/Erstellen). |
| Team | Gruppierung von Principals mit eigenen Rollen und Ownership-Rechten. |
| Translation | Lokalisierte Bezeichnungen und Texte pro Sprache. |
| Solution | Paket aus Artefakten mit Version, Abhängigkeiten, Signaturen. |
| Webhook / Integration Endpoint | Deklarative Definition eingehender und ausgehender Integrationspunkte. |
| NotificationTemplate | Template für In-App-, E-Mail- und Push-Benachrichtigungen inkl. Platzhaltern. |

## **4.3 Identifikation und Namensgebung**

* Jedes Artefakt besitzt eine stabile, global eindeutige ID (URN-artig, z. B. 'urn:mdapp:entity:core.contact').

* Zusätzlich einen menschenlesbaren logischen Namen (snake\_case oder camelCase, ASCII, maximal 64 Zeichen) und einen lokalisierten Anzeigenamen pro Sprache.

* Namenskonflikte werden beim Publizieren erkannt und verhindern den Import.

## **4.4 Versionierung**

Jedes Artefakt trägt eine semantische Version (MAJOR.MINOR.PATCH). MAJOR-Änderungen enthalten Breaking Changes (z. B. Entfernen eines Attributs), MINOR-Änderungen sind abwärtskompatibel (z. B. neues optionales Attribut), PATCH-Änderungen sind rein redaktioneller Natur. Die Engine garantiert für MINOR/PATCH die Kompatibilität laufender Clients und gespeicherter Daten.

## **4.5 Referenzen und Abhängigkeiten**

* Artefakte referenzieren einander ausschließlich über stabile IDs, niemals über Anzeigenamen.

* Zyklische Abhängigkeiten zwischen Solutions sind unzulässig.

* Beim Import wird die Abhängigkeitstopologie berechnet; fehlende oder inkompatible Abhängigkeiten blockieren den Import mit eindeutiger Fehlermeldung.

## **4.6 Metamodell-Evolution**

Erweiterungen des Metamodells (neue Elementtypen, neue Attribute) sind additiv und abwärtskompatibel einzuführen. Entfernungen oder Bedeutungsänderungen erfordern eine neue Metamodell-MAJOR-Version und einen dokumentierten Migrationspfad. Die Plattform hält für eine definierte Zeit mindestens zwei Metamodell-Versionen parallel lauffähig.

# **5\. Domänenmodell**

## **5.1 Entitäten**

Eine Entität repräsentiert einen fachlichen Typ. Sie besitzt:

* Stabile ID, logischen Namen, Plural-/Singularformen, Icon, Farbe.

* Primärschlüssel (verpflichtend, systemgeneriert, global eindeutig, unveränderlich).

* Optionale alternative Schlüssel (ein oder mehrere, für externe Integration oder Geschäftsschlüssel).

* Primäres Anzeigefeld (Pflichtangabe, dient als Label in Listen, Lookups und Referenzen).

* Owner-Modell: User-owned, Team-owned, Organization-owned oder None.

* Flags: Auditierung aktiv, Änderungsverfolgung aktiv, Duplikaterkennung aktiv, Offline-fähig.

* Lebenszyklus-Metadaten: systemgenerierte Felder createdAt, createdBy, modifiedAt, modifiedBy, versionNumber (monoton).

## **5.2 Datentypen für Attribute**

Die Plattform unterstützt mindestens folgende primitive und zusammengesetzte Datentypen:

| Typ | Bereich / Speicher | Anmerkungen |
| :---- | :---- | :---- |
| String | 1 – 1'048'576 Zeichen | Mit Maximal-/Minimallänge, Regex, Casing, Mehrzeilig, Rich-Text-Flag. |
| Integer | 64 Bit signed | Min/Max-Grenzen, Schrittweite, Format. |
| Decimal | 28 signifikante Stellen | Skalierung (0–10), Rundungsmodus, Währungs-Flag. |
| Currency | Decimal \+ Währungsreferenz | Optional mit Wechselkurs-Historie. |
| Boolean | true/false | Anzeige als Checkbox, Toggle oder zwei-Optionen-Feld. |
| Date | ISO 8601 Datum | Nur Datum, ohne Zeit. |
| DateTime | ISO 8601 UTC | Serverseitig UTC, clientseitig lokale Darstellung, Zeitzonensemantik definierbar. |
| Duration | Minuten oder ISO 8601 | Für Laufzeiten, SLA-Fristen. |
| Enum / OptionSet | Kodierte Optionsliste | Global oder lokal, lokalisiert, optional Multi-Select. |
| Lookup | Referenz auf Entität | Typisiert auf eine oder mehrere Zielentitäten (Polymorphic). |
| File / Attachment | Binärdaten \+ Metadaten | Maximalgrösse, MIME-Whitelist, Virus-Scan-Pflicht. |
| Image | Binärdaten \+ Abmessungen | Vorschau, Skalierung. |
| UniqueIdentifier | UUID v4/v7 | Für technische Schlüssel. |
| JSON / Structured | Strukturiertes Dokument | Mit hinterlegtem Schema (optional); für seltene, nicht-relationale Teile. |
| Calculated | Abgeleitet | Serverseitig berechnet, read-only, deterministisch. |
| Rollup | Aggregation | Über eine Beziehung, mit Aktualisierungsintervall. |
| Formula | Clientseitig berechnet | Nur Anzeige, nicht persistiert. |

## **5.3 Attribut-Metadaten**

Jedes Attribut besitzt eine Menge von Metadaten, die vom Renderer und von der Engine ausgewertet werden:

* Pflicht/Optional, Unique-Constraint, Default-Wert (statisch oder formelbasiert).

* Anzeigename, Kurzbeschreibung, Tooltip, Hilfetext (alle lokalisiert).

* Darstellungsformat (z. B. Telefonformat, IBAN, Prozent, Datum), Eingabemaske.

* Datenklassifizierung (öffentlich, intern, vertraulich, streng vertraulich, personenbezogen).

* Datenschutz-Flags (z. B. besondere Kategorien personenbezogener Daten), Retention-Kategorie.

* Sichtbarkeits-Defaults (z. B. 'nicht in Listen', 'nicht in Exporten').

* Searchable-Flag, Filterable-Flag, Sortable-Flag.

* Deprecation-Flag mit Nachfolger-Referenz.

## **5.4 Beziehungen**

Beziehungen sind eigenständige Modellartefakte und beschreiben die Verknüpfung zwischen zwei Entitäten.

| Kardinalität | Umsetzung | Semantik |
| :---- | :---- | :---- |
| 1:1 | Optionale Lookup \+ Unique | Selten; z. B. Erweiterungsobjekt. |
| 1:n | Lookup-Attribut auf Kind | Standardfall; Kind-Löschverhalten konfigurierbar. |
| n:m | Explizite Zwischen-Entität | Mit eigenen Attributen; kein implizites Mapping. |
| Hierarchie | Selbstreferenz mit Parent-Lookup | Für Organisationsstrukturen; mit Zyklusprüfung. |

## **5.5 Lösch- und Kaskadenverhalten**

Pro Beziehung ist festzulegen, wie sich das Löschen des Eltern-Datensatzes auf Kind-Datensätze und umgekehrt auswirkt. Unterstützt werden mindestens: Restrict (Verhindern), Cascade (Mitlöschen), SetNull (Referenz nullen), Reassign (Umhängen auf anderen Elternteil). Das Default-Verhalten für Lookups ist Restrict.

## **5.6 Berechnete Felder und Rollups**

* Berechnete Felder werden serverseitig deterministisch aus anderen Attributen derselben oder verbundener Entitäten abgeleitet.

* Rollups aggregieren Werte entlang einer 1:n-Beziehung (SUM, COUNT, AVG, MIN, MAX) mit definierter Aktualisierungsfrequenz (synchron oder asynchron).

* Zirkelbezüge werden beim Publizieren erkannt und verhindert.

## **5.7 Referenzielle Integrität und Transaktionen**

Sämtliche fachlichen Schreiboperationen über die Kern-API erfolgen in Transaktionen mit mindestens Read-Committed-Isolation. Bulk-Operationen werden transaktional geklammert oder explizit als Streaming-Operation mit definierter Fehlerbehandlung gekennzeichnet. Die Plattform garantiert die Konsistenz der Beziehungs- und Owner-Modelle auch bei parallelen Schreibvorgängen (optimistisches Locking über versionNumber).

# **6\. Sicherheits- und Berechtigungsmodell**

## **6.1 Principals**

* User: menschlicher Benutzer mit eindeutiger ID, verknüpft mit einem Identity Provider über OIDC/SAML.

* Service Principal / Application User: technischer Nutzer für Integrationen, mit eigenen Rollen.

* Team: Gruppierung von Usern oder anderen Teams; kann selbst Datensätze besitzen (Team-Owned).

* BusinessUnit / Organisationseinheit: hierarchische Einheit zur Daten- und Rollen-Abgrenzung.

## **6.2 Privilegien und Aktionen**

Privilegien sind die feingranulare Währung des Sicherheitsmodells. Pro Entität existieren mindestens folgende Aktionen:

* Create, Read, Update, Delete, Append, AppendTo, Assign, Share, ReadArchived.

* Plus entitätsunabhängige Privilegien: Export, Import, ExecuteWorkflow, ExecuteProcess, SystemAdministration, ImpersonateUser.

## **6.3 Geltungsbereiche (Scope)**

Jedes Privileg wird mit einem Scope kombiniert, der die Reichweite bestimmt:

| Scope | Semantik |
| :---- | :---- |
| None | Kein Zugriff. |
| User | Nur eigene Datensätze (Owner \= aktueller User oder Team, in dem der User Mitglied ist). |
| BusinessUnit | Datensätze derselben Organisationseinheit. |
| ParentChild | Eigene und alle untergeordneten Organisationseinheiten. |
| Organization | Alle Datensätze der Organisation/des Tenants. |

## **6.4 Rollen**

Rollen sind benannte Sammlungen aus (Privileg, Scope)-Paaren. Rollen werden Principals zugewiesen. Ein User erhält die Vereinigung aller Rechte über seine direkten Rollen, Team-Rollen und BusinessUnit-Rollen. Least Privilege ist das Default: Ohne explizite Rolle hat ein User keinen Zugriff auf Entitätsdaten.

## **6.5 Datensatzbezogene Freigabe (Sharing)**

Zusätzlich zu Rollen kann ein Owner oder privilegierter Benutzer einen Einzel-Datensatz gezielt mit einem oder mehreren Principals teilen. Das Share-Objekt ist selbst auditiert und enthält ablaufende Gültigkeiten (optional). Shares erweitern, aber reduzieren niemals die Rechte.

## **6.6 Feldbezogene Sicherheit**

Für Attribute mit erhöhtem Schutzbedarf kann ein Field Security Profile definiert werden, das pro Rolle Lese-, Schreib- und Erstellungsrechte separat steuert. Unberechtigte Felder werden in API-Antworten ausgeblendet (nicht maskiert mit Dummy-Werten) und in der UI nicht angezeigt. Suchen und Filter auf gesperrten Feldern sind serverseitig zu verhindern.

## **6.7 Row-Level-Filter**

Über deklarative, rollenbezogene Filterausdrücke kann die sichtbare Datenmenge weiter eingeschränkt werden (z. B. 'Benutzerin sieht nur Verträge, deren Produkt in ihrer Produktfreigabeliste liegt'). Filter werden konsistent in API, Ansichten, Charts, Dashboards und Exporten angewendet.

## **6.8 Authentifizierung**

* Unterstützte Protokolle: OIDC (empfohlen), SAML 2.0, optional mTLS für Service Principals.

* Tokens sind kurzlebig (empfohlen ≤ 60 Minuten), mit Refresh-Flow; sensitive Operationen können Step-Up-Authentifizierung erfordern.

* Session-Fixation, Replay und Token-Diebstahl-Gegenmassnahmen sind Pflicht (Audience-Check, nonce, short-lived, key rotation).

## **6.9 Autorisierung zur Laufzeit**

Jede API-Anfrage durchläuft eine zentrale Policy-Auswertung: (1) Authentifizierung prüfen, (2) Tenant-/BusinessUnit-Kontext auflösen, (3) wirksame Rollen und Shares bestimmen, (4) Privileg \+ Scope gegen Ziel-Entität/Datensatz prüfen, (5) Feld-Sicherheit anwenden, (6) Row-Level-Filter injizieren, (7) Audit-Log schreiben. Diese Kette ist nicht umgehbar; direkte Datenbankzugriffe am Engine-Kontext vorbei sind verboten.

## **6.10 Besondere Privilegien**

* Impersonation: nur für explizit berechtigte Administratoren, mit vollständiger, unveränderlicher Audit-Spur (Original-User, Ziel-User, Zeitraum, Begründung).

* Break-Glass-Konten: nur für Notfälle, mit MFA-Pflicht, Nachweispflicht und automatischer Alarmierung.

# **7\. Navigation und Informationsarchitektur**

## **7.1 Struktur**

Die Navigation wird deklarativ als Site Map modelliert. Sie besteht aus:

* Areas: oberste Gruppierung (z. B. Vertrieb, Service, Verwaltung).

* Groups: Untergliederung innerhalb einer Area.

* SubAreas: einzelne Navigationsziele; binden an eine Entität, eine bestimmte Ansicht, ein Dashboard oder eine URL.

## **7.2 Sichtbarkeit**

* SubAreas können an Rollen, BusinessUnits oder Feature-Flags gebunden werden; nicht sichtbare Einträge erscheinen nicht in der UI.

* Default-Einstiegspunkt pro Rolle definierbar.

* Mehrere Site Maps pro Anwendung (App-Modul) möglich, z. B. für unterschiedliche Fachbereiche auf derselben Plattform-Instanz.

## **7.3 App-Module**

Ein App-Modul bündelt eine Site Map, eine Auswahl an Entitäten, Formularen, Ansichten und Dashboards zu einer fokussierten Anwendung. Benutzer sehen nur die App-Module, für die sie berechtigt sind. App-Module sind eigene Modellartefakte und können unabhängig versioniert und transportiert werden.

# **8\. Ansichten (Views)**

## **8.1 Arten von Ansichten**

* System-Ansichten: vom Entwickler ausgeliefert, für alle Benutzer sichtbar (Rechte vorausgesetzt).

* Persönliche Ansichten: von Benutzern selbst gespeichert, nur für den Ersteller sichtbar, teilbar.

* Lookup-Ansichten: reduzierte Darstellung für Lookup-Dialoge.

* Associated Views: zeigen verwandte Datensätze im Kontext eines Eltern-Datensatzes.

## **8.2 Struktur**

Eine Ansicht referenziert eine Entität und definiert:

* Spalten (inkl. Breite, Sortierbarkeit, Sichtbarkeit auf Mobile).

* Filter (strukturierter Ausdrucksbaum mit UND/ODER/NICHT, Operatoren, Platzhaltern wie '@currentUser', '@today').

* Sortierung (mehrspaltig).

* Gruppierung und Aggregation (optional).

* Voreingestellte Ansicht (default).

## **8.3 Filterausdruck**

Der Filterausdruck ist als rekursiver, typisierter AST zu spezifizieren. Unterstützte Operatoren umfassen mindestens: eq, neq, gt, gte, lt, lte, in, notIn, like, notLike, isNull, isNotNull, between, contains, startsWith, endsWith, today, thisWeek, thisMonth, thisYear, lastNDays, nextNDays, inHierarchy, aboveHierarchy, belowHierarchy. Der Filter wird serverseitig autoritativ ausgewertet; clientseitige Filter sind ausschließlich UX-Beschleunigung.

## **8.4 Suche**

* QuickFind: entitätsweise Suche in einer konfigurierten Menge durchsuchbarer Felder.

* Globale Suche: tenant-weit über mehrere Entitäten, ranked by relevance.

* Volltextindex: asynchron aktualisiert; Verzögerung muss dokumentiert und beobachtbar sein.

# **9\. Formulare**

## **9.1 Formulartypen**

| Typ | Zweck |
| :---- | :---- |
| Main | Hauptformular mit vollem Funktionsumfang (Tabs, Sections, Sub-Grids, BPF-Header). |
| QuickCreate | Schnellerfassungsformular mit reduziertem Feldumfang für Erstanlage aus anderen Kontexten. |
| QuickView | Read-only-Einbettung in andere Formulare, zeigt verknüpfte Datensätze kompakt. |
| Card | Kompakte Darstellung für Listen/Karten-Layouts und mobile Szenarien. |

## **9.2 Aufbau eines Hauptformulars**

* Header: prominente Felder, Owner, Status, BPF-Indikator.

* Tabs: logische Gruppierung; einzelne Tabs können an Rollen oder Bedingungen gebunden werden.

* Sections: Spaltenraster (typisch 1–4 Spalten, responsive); enthalten Felder, Sub-Grids, QuickViews, Iframe-Slots.

* Sub-Grids: eingebettete Listen verwandter Datensätze mit eigener Ansicht und Inline-Aktionen.

* Footer: zusammenfassende Felder (Erstellt am, Geändert von, …).

## **9.3 Feld-Verhalten**

Pro Feld im Formular sind zu definieren: Sichtbar/Unsichtbar, Pflicht/Optional/Empfohlen, Read-only-Status, bedingte Formatierung, Label-Override, Hilfe-Override. Diese Eigenschaften können statisch oder regelbasiert sein (siehe Business Rules).

## **9.4 Ereignisse**

Formulare definieren deklarativ Ereignis-Bindings, die zur Laufzeit Regeln oder Erweiterungen auslösen: OnLoad, OnSave, OnChange(field), OnTabChange, OnBPFStageChange. Ereignis-Handler dürfen das Speichern verhindern (veto) und Benutzerrückmeldungen erzeugen.

## **9.5 Responsive Verhalten**

Der Renderer passt Layouts an Formfaktoren an (Desktop, Tablet, Mobile). Sections und Sub-Grids deklarieren Breakpoints und Prioritäten; der Renderer entscheidet über Stacking, Kollabieren und Ausblenden. Eine pixelgenaue Positionierung ist nicht vorgesehen – die Plattform zielt auf konsistente, nicht auf individualisierte Darstellung.

## **9.6 Barrierefreiheit**

Der Renderer erzeugt semantisch korrekte Markup-Strukturen (Labels, Rollen, Landmarks) und erfüllt mindestens WCAG 2.2 AA. Formulare können deklarativ zusätzliche ARIA-Hinweise und Hilfetexte hinterlegen. Tastaturnavigation über alle Felder, Tabs und Aktionen ist Pflicht.

# **10\. Geschäftslogik**

## **10.1 Schichten der Logik**

6. Attribut-Constraints (Typ, Länge, Regex, Pflicht, Unique, Default).

7. Business Rules am Formular oder an der Entität (deklarativ, clientseitig evaluiert, serverseitig autoritativ erneut geprüft).

8. Serverseitige Workflows und Automationen (ereignis- und zeitgetrieben).

9. Business Process Flows (mehrstufige, geführte Prozesse).

10. Prozedurale Erweiterungen (Plug-ins, Skripte, externe Services) – nur wenn 1–4 nicht ausreichen.

## **10.2 Business Rules**

Eine Business Rule besteht aus einer Bedingung (Prädikatsbaum) und einer Liste von Aktionen. Unterstützte Aktionen:

* Feld setzen (statisch oder formelbasiert).

* Feld leeren.

* Feld als Pflicht / Nicht-Pflicht markieren.

* Feld sichtbar / unsichtbar schalten.

* Feld Read-only / Editable schalten.

* Fehlermeldung an Feld oder Formular anzeigen (verhindert Speichern).

* Empfehlung anzeigen (verhindert Speichern nicht).

## **10.3 Ausführungsorte von Regeln**

| Regel-Art | Geltungsbereich | Ausführungszeitpunkt |
| :---- | :---- | :---- |
| Formular-BR | Nur im betreffenden Formular | Clientseitig bei OnLoad/OnChange; serverseitig erneut bei Save. |
| Entitäts-BR | Für alle Schreibvorgänge auf der Entität | Ausschliesslich serverseitig, auch über API. |
| Attribut-Constraint | Immer | Serverseitig; clientseitig als Hinweis. |
| Workflow | Auslöser-abhängig | Serverseitig, synchron oder asynchron. |
| Process Flow | Bindend für Prozess | Serverseitig beim Stadium-Übergang. |

## **10.4 Determinismus und Idempotenz**

Alle serverseitigen Regeln und Workflows sind so zu implementieren, dass sie bei gleichem Eingabezustand das gleiche Ergebnis liefern (deterministisch) und wiederholbare Ausführung ohne Seiteneffekte tolerieren (idempotent). Einmalige Seiteneffekte (z. B. Zahlung auslösen) werden über Idempotenz-Schlüssel abgesichert.

## **10.5 Konfliktbehandlung**

Bei konkurrierenden Änderungen am selben Datensatz gilt Optimistic Concurrency über die versionNumber. Ein Konflikt liefert einen definierten Fehlercode (HTTP 412/409-Äquivalent) mit strukturierter Fehlerantwort; Clients sind angehalten, den Konflikt dem Benutzer verständlich darzustellen.

# **11\. Workflows und Automationen**

## **11.1 Auslöser**

* Ereignisbasiert: onCreate, onUpdate(field-set), onDelete, onAssign, onStatusChange.

* Zeitbasiert: Cron-Ausdruck, Kalender (werktäglich, Geschäftstage).

* Externer Trigger: Webhook-Eingang, Nachrichten aus dem Event Bus.

* Manuell: Benutzer-initiiert über eine Button-Aktion am Formular/an der Ansicht.

## **11.2 Schritte**

Ein Workflow besteht aus einer Folge von Schritten. Unterstützte Schritttypen umfassen mindestens:

* Datensatz erstellen, aktualisieren, löschen.

* Feld berechnen, setzen, leeren.

* Benachrichtigung senden (In-App, E-Mail, Push).

* Webhook aufrufen (mit Retry-Policy und Circuit-Breaker-Semantik).

* Ereignis an Event Bus publizieren.

* Warten (fixe Dauer oder bis zu einem Ereignis).

* Bedingte Verzweigung, Schleife mit Abbruchgrenze.

* Aufruf eines anderen Workflows (mit begrenzter Tiefe).

* Genehmigungsschritt (mit Eskalation und Fristen).

## **11.3 Ausführungsmodell**

* Workflows laufen grundsätzlich asynchron; synchrone Ausführung ist auf kurze, nicht-blockierende Logik beschränkt und begrenzt.

* Jeder Schritt ist persistent protokolliert; Workflows sind nach Neustart wieder aufsetzbar.

* Retries mit Exponential Backoff; nach definierter Anzahl Fehlschläge Überführung in einen Fehlerzustand mit manueller Wiederaufnahme.

* Kompensationsschritte können pro Workflow definiert werden (Saga-Muster).

## **11.4 Governance**

* Workflows mit externen Effekten (E-Mail, Zahlung, API-Aufruf in andere Tenants) sind gekennzeichnet und unterliegen einer Genehmigungspflicht vor Publikation.

* Rate-Limits pro Workflow und pro Tenant verhindern unbeabsichtigte Massenwirkungen.

* Alle Workflow-Ausführungen sind auditierbar (Start, Schritte, Dauer, Ergebnis, Fehlermeldungen).

# **12\. Business Process Flows**

## **12.1 Zweck**

Business Process Flows (BPF) führen Benutzer schrittweise durch einen definierten Fachvorgang. Sie sind visuelle Zustandsmaschinen, die Stadien (stages), Schritte (steps) und Kriterien kombinieren und optional Entitätsübergänge erlauben (z. B. vom Lead zur Opportunity).

## **12.2 Struktur**

* Stage: fachlicher Abschnitt mit einer Zielentität, einer Menge von Schritten und Abschlusskriterien.

* Step: einzelnes Pflicht-/Optional-Feld oder eine zusammenfassende Abfrage im Stage.

* Transition: definierter Übergang zwischen Stages, optional mit Bedingung.

* Branching: bedingte Verzweigung anhand von Datensatzwerten.

## **12.3 Laufzeitverhalten**

* Ein Datensatz kann sich zu einem Zeitpunkt in höchstens einem aktiven BPF-Instanzzustand befinden (pro Prozesstyp).

* Stage-Wechsel werden protokolliert und auditiert.

* Berechtigte Rollen können Prozessinstanzen erneut starten, abbrechen oder überspringen, mit Begründungsplicht.

* BPF-Definitionen sind versioniert; laufende Instanzen bleiben an ihrer Ausgangsversion, neue Instanzen verwenden die aktuelle.

# **13\. Dashboards und Diagramme**

## **13.1 Diagramme**

Diagramme basieren auf einer Entität oder einer Ansicht und aggregieren Daten. Unterstützte Typen mindestens: Säule, Balken, Linie, Fläche, Torte, Ring, Funnel, Streuung, Kennzahl (Single Value). Pro Diagramm werden Dimensionen, Kennzahlen, Aggregationsfunktion (SUM, COUNT, AVG, MIN, MAX, DISTINCT\_COUNT), Sortierung und Top-N-Einschränkungen definiert.

## **13.2 Dashboards**

* Ein Dashboard komponiert mehrere Diagramme, Kennzahlen, Listen und Filter zu einer Gesamtsicht.

* Cross-Filtering: Klick auf ein Element filtert andere Elemente (deklarativ definiert).

* Rollenbasierte Sichtbarkeit; persönliche und systemweite Dashboards werden unterschieden.

* Datenaktualisierung mindestens on-demand, optional periodisch mit dokumentiertem Cache-TTL.

## **13.3 Abgrenzung zu Analytics**

Dashboards bedienen operative Auswertungen. Für explorative Analytics, komplexe Joins über viele Entitäten, historische Trendanalysen und ML-Szenarien ist eine Auskoppelung an ein dediziertes Analytics-Layer vorgesehen (z. B. Data Warehouse, Lakehouse). Die Plattform stellt dafür standardisierte Change-Feeds (CDC) bereit.

# **14\. Integration und APIs**

## **14.1 Kern-API**

Die Plattform stellt eine generische, modellgetriebene Kern-API bereit, die alle Entitäten einheitlich bedient. Für jede Entität existieren automatisch Endpunkte zum Auflisten, Lesen, Erstellen, Aktualisieren, Löschen, Upsert, Bulk-Operationen und zum Ausführen benannter Aktionen. Endpunkte folgen einer konsistenten URL- und Statuscode-Konvention (REST/OData-kompatibel). Optional wird eine GraphQL-Fassade bereitgestellt.

## **14.2 Abfragefähigkeiten**

* Projektion (nur benötigte Felder).

* Filter (identische Ausdruckssprache wie Ansichten).

* Sortierung, Paginierung (cursor-basiert für grosse Mengen).

* Expand verwandter Entitäten bis zu einer konfigurierten Tiefe.

* Aggregationen für Kennzahlen.

* Schwach typisierte freie Suche über Volltextindex.

## **14.3 Fehlermodell**

Alle Fehler werden strukturiert zurückgegeben (maschinenlesbarer Code, lokalisierte Meldung, Feldbezüge, Korrelations-ID). Fehlerkategorien umfassen mindestens: Validierung, Autorisierung, Konflikt, NichtGefunden, RateLimit, InterneStoerung, IntegrationsStoerung. Keine internen Exception-Details in Produktions-Responses.

## **14.4 Ereignisse (Outbox)**

Jede fachlich relevante Zustandsänderung erzeugt deterministisch ein Ereignis nach dem Transactional-Outbox-Muster. Ereignisse werden in eine Outbox-Tabelle geschrieben, die von einem Publisher in den Event Bus überführt wird. Ereignisse besitzen mindestens: Ereignistyp, Entitätsreferenz, Datensatz-ID, Version, Korrelations-ID, Trace-Kontext, Payload (mindestens die geänderten Felder), Zeitstempel, Tenant-ID.

## **14.5 Eingehende Integration**

* Webhook-Endpunkte pro Integrationspunkt mit Signaturprüfung (HMAC oder mTLS).

* Idempotenz über einen vom Aufrufer bereitgestellten Schlüssel im Header.

* Deklaratives Mapping eingehender Nachrichten auf Entitäten/Felder und optionalem Workflow-Trigger.

## **14.6 Ausgehende Integration**

* Webhooks pro Ereignis- oder Regeltyp, mit Retry-Policy, Backoff, Dead-Letter-Queue.

* Anbindung an externe APIs über deklarierte Konnektoren; Credentials im Secret-Store, nie im Modell.

## **14.7 Versionierung von APIs**

Die Kern-API trägt eine eigene Version, die unabhängig vom Anwendungsmodell ist. Breaking Changes werden mit Frist (mindestens 12 Monate) angekündigt und parallel zu Vorgängerversionen betrieben. Modelländerungen (neue Felder, Entitäten) sind additiv und erfordern keine API-Major-Änderung.

## **14.8 Ratenbegrenzung und Schutz**

* Pro Principal, Tenant und Endpunkt durchsetzbare Ratenlimits.

* Begrenzung von Payload-Grösse, Query-Tiefe und Expand-Tiefe.

* Mandatory TLS, HSTS, strenge CORS-Policies, Standard-Security-Header.

# **15\. Datenhaltung und Persistenz**

## **15.1 Primärablage**

Die Primärablage ist eine relationale Datenbank mit ACID-Eigenschaften. Jede Entität wird auf mindestens eine Tabelle abgebildet. Das physische Schema ist ein Implementierungsdetail der Engine und wird nicht Teil des Modells; Clients greifen ausschliesslich über die Kern-API zu.

## **15.2 Mandantentrennung**

* Pflicht-Discriminator-Spalte tenant\_id in jeder Entitätstabelle, erzwungen durch die Engine.

* Optional physisches Sharding / separate Datenbanken je Tenant in höheren Sicherheitsstufen.

* Querverweise zwischen Tenants sind unzulässig; die Engine verhindert sie technisch.

## **15.3 Änderungs-Log und Audit**

Jede schreibende Operation erzeugt Einträge in einem unveränderlichen Audit-Log (append-only). Protokolliert werden mindestens: wer, wann, was (Entität, Datensatz, Feld), alt/neu, über welchen Kanal (UI, API, Workflow, System), Korrelations-ID. Löschungen werden logisch protokolliert, auch wenn der Datensatz physisch entfernt wird. Der Audit-Log unterliegt separater Berechtigung.

## **15.4 Soft-Delete und Archivierung**

* Entitäten können deklarativ mit Soft-Delete versehen werden; gelöschte Datensätze sind standardmässig unsichtbar, lassen sich aber von berechtigten Rollen einsehen und wiederherstellen.

* Archivierung verschiebt selten genutzte Datensätze in eine kosten-/leistungsoptimierte Ablage; API-Transparenz bleibt gewahrt, mit höheren Latenzgarantien.

## **15.5 Retention und Löschung**

Pro Entität und pro Attribut können Retention-Regeln deklariert werden (z. B. 'personenbezogene Daten löschen 10 Jahre nach Vertragsende'). Die Retention-Engine prüft regelmässig und löscht oder anonymisiert gemäss Regel, unter Berücksichtigung von Legal Holds, die bestimmte Datensätze gegen Löschung schützen. Löschprotokolle sind auditierbar und aufsichtsseitig exportierbar.

## **15.6 Backup und Wiederherstellung**

* Regelmässige, verschlüsselte Backups mit definierter RPO (z. B. ≤ 15 Minuten) und RTO (z. B. ≤ 4 Stunden).

* Point-in-Time-Recovery mindestens für die letzten 30 Tage.

* Regelmässig geübte Wiederherstellung; Backup ohne Restore-Test ist nicht existent.

## **15.7 Datenexport und Import**

* Strukturierter Export ganzer Entitäten oder gefilterter Mengen als JSON/CSV/Parquet.

* Import mit Schemavalidierung, Dry-Run, Upsert-Semantik, Fehlerbericht pro Zeile.

* Volltenant-Export für Migration und aufsichtsrechtliche Portabilität.

# **16\. UI-Renderer**

## **16.1 Aufgabe**

Der Renderer erzeugt aus Modell und Daten eine konsistente, barrierefreie und responsive Benutzeroberfläche. Er ist nicht dafür ausgelegt, pixelgenaue Individual-UIs umzusetzen; wer das braucht, baut eine separate Canvas-Anwendung.

## **16.2 Komponentenbibliothek**

Der Renderer liefert eine geschlossene, getestete Komponentenbibliothek für alle Modellprimitive (Felder pro Datentyp, Sub-Grid, QuickView, Navigation, Dashboard, Prozess-Header, Dialoge). Neue Komponenten werden zentral ergänzt, nicht pro Anwendung.

## **16.3 Lokalisierung**

* Vollständige Mehrsprachigkeit mit Fallbacks (Benutzersprache → Organisationssprache → Standardsprache).

* Lokalisierung von Formaten (Zahlen, Datum, Währung) pro Locale des Benutzers.

* Pluralregeln, Bidirektionalität und Non-Latin-Skripte werden unterstützt.

## **16.4 Zustand und Offline-Verhalten**

* Der Renderer hält einen klar definierten Client-State: geladenes Modellfragment, Originalzustand, Benutzeränderungen, Validierungsergebnisse, Server-Version.

* Optional: Offline-Unterstützung für ausgewählte Entitäten mit Synchronisations-Policy und Konfliktauflösung.

## **16.5 Performance-Zielwerte (Referenz)**

| Metrik | Zielwert | Bedingung |
| :---- | :---- | :---- |
| Time-to-interactive einer Formularseite | ≤ 2.0 s | p75, LAN, typische Entität (≤ 30 Felder). |
| Reaktion auf Feldänderung (Client-Regel) | ≤ 100 ms | p95, ohne Server-Roundtrip. |
| Speichern eines Standard-Datensatzes | ≤ 500 ms | p95, inkl. Server-Regeln. |
| Ansicht mit 10'000 Zeilen paginiert | ≤ 800 ms | p95 erste Seite, cursor-basiert. |

# **17\. Erweiterbarkeit und Custom Code**

## **17.1 Erweiterungspunkte**

* Server-Plug-ins: reagieren auf Entitätsereignisse (pre/post create/update/delete) und können Veto einlegen.

* Custom Actions: benannte, typisierte Aktionen, die in Formularen/Ansichten als Button auftauchen und serverseitig Logik ausführen.

* Custom Controls: zusätzliche UI-Komponenten für spezielle Feldtypen (z. B. Unterschrift, Kartenausschnitt), die in Formularen deklariert werden.

* Script Hooks (clientseitig): eng begrenzt, nur für UX-Verbesserungen, mit dokumentiertem API-Surface.

## **17.2 Sandboxing**

* Custom Code läuft in isolierten Ausführungsumgebungen mit CPU-, Memory- und Laufzeit-Limits.

* Netzwerkzugriff nur über genehmigte, deklarierte Konnektoren.

* Zugriff auf Daten nur über die Kern-API, nicht direkt auf die Datenbank.

* Secrets werden ausschliesslich über einen Secret-Store bereitgestellt, nie im Code.

## **17.3 Lifecycle von Erweiterungen**

* Erweiterungen sind versioniert und in Solutions paketiert.

* Signaturen zur Herkunftsprüfung; nur signierte Erweiterungen sind in Produktion zugelassen.

* Telemetrie zu Laufzeitverhalten, Fehlern und Ressourcenverbrauch ist Pflicht.

# **18\. Solution-Lifecycle**

## **18.1 Solutions**

Eine Solution ist ein versioniertes Paket von Modellartefakten und optionalen Erweiterungen, das als transportierbare Einheit dient. Solutions haben einen eindeutigen Namen, eine Version, ein Publisher-Kennzeichen und eine Liste von Abhängigkeiten mit Versionsbereichen.

## **18.2 Managed vs. Unmanaged**

* Unmanaged: bearbeitbar, für Entwicklung und iterative Anpassung.

* Managed: versiegelt, nur über neue Versionen aktualisierbar, für Test- und Produktionsumgebungen.

* In einer Produktionsumgebung sind ausschliesslich Managed Solutions zulässig.

## **18.3 Transport und Deployment**

11. Export einer Solution aus der Entwicklungsumgebung (schemavalidiert, signiert).

12. Automatisierter Import in Test-/Produktionsumgebungen über eine deklarative Pipeline (CI/CD).

13. Schema-Migration und Datenmigration als Teil des Imports, mit Pre-/Post-Hooks.

14. Rollback-Plan und Smoke-Tests je Umgebung.

## **18.4 Upgrade und Deinstallation**

* Upgrades sind nicht-destruktiv: keine stillen Datenverluste, Schema-Änderungen werden angekündigt und protokolliert.

* Deinstallation einer Solution entfernt nur eigene Artefakte; Daten werden nicht automatisch gelöscht.

* Pro Umgebung ist eine Liste installierter Solutions und Versionen jederzeit abfragbar.

## **18.5 Umgebungsstrategie**

Mindestumgebungen: Development, Integration/Test, Produktion. Pre-Produktions-Umgebungen müssen dem Produktivsystem funktional und konfigurativ gleichwertig sein; personenbezogene Daten dürfen nur pseudonymisiert oder synthetisch vorliegen.

# **19\. Observability**

## **19.1 Logging**

* Strukturierte, maschinenlesbare Logs (JSON) mit Korrelations-ID, Trace-ID, Tenant-ID, Principal-ID.

* Log-Levels sind konsistent und konfigurierbar; PII in Logs ist standardmässig zu unterdrücken.

## **19.2 Metriken**

* Standardisierte Metriken für Request-Raten, Latenzen, Fehlerraten, Ressourcenverbrauch.

* Fachliche Metriken pro Entität/Workflow (Anzahl Erstellungen, Workflow-Durchsatz, Fehlerrate).

## **19.3 Tracing**

* Verteiltes Tracing über alle Plattformkomponenten mit OpenTelemetry-kompatiblem Kontext.

* Trace-Kontext wird in ausgehenden Webhooks und Events propagiert.

## **19.4 Health und SLOs**

* Health-Endpunkte (liveness, readiness) pro Komponente.

* Publizierte SLOs für API-Verfügbarkeit und Latenz, mit Fehlerbudget-Steuerung.

# **20\. Nicht-funktionale Anforderungen**

## **20.1 Verfügbarkeit**

* Zielverfügbarkeit mindestens 99.9 % pro Monat (exklusive geplanter Wartung), abhängig von Tenant-Klasse.

* Keine Single Points of Failure in kritischen Pfaden (API, Auth, Engine, Persistenz).

* Multi-AZ-Deployment; Multi-Region optional für höchste Tenant-Klassen.

## **20.2 Skalierbarkeit**

* Horizontale Skalierung aller zustandslosen Komponenten.

* Datenbank: Leser repliziert, Schreiber abgesichert, mit dokumentierten Grenzen pro Tenant.

* Rate-Limits und Backpressure verhindern Kaskadenfehler.

## **20.3 Performance**

Referenz-Zielwerte sind in Abschnitt 16.5 definiert. Für Engine-seitige Endpunkte gelten entsprechende SLOs; Lasttests sind Teil jeder Release-Pipeline.

## **20.4 Sicherheit (Querschnitt)**

* Verschlüsselung in Transit (TLS 1.2+) und at Rest (AES-256 oder äquivalent).

* Secret-Management über zentralen Tresor; keine Secrets in Modell, Code oder Logs.

* Regelmässige Penetrationstests, abhängigkeitsbasierte Schwachstellen-Scans, SBOM pro Release.

* Zero-Trust-Prinzipien zwischen internen Diensten (mTLS, short-lived tokens, explicit allow-lists).

## **20.5 Datenschutz und Compliance**

* Rechtmässigkeits-Nachweis: Modell dokumentiert pro personenbezogenem Feld den Zweck, die Rechtsgrundlage und die Retention.

* Betroffenenrechte technisch abbildbar: Auskunft, Berichtigung, Löschung, Einschränkung, Datenübertragbarkeit.

* Regionale Datenresidenz konfigurierbar pro Tenant.

* Exportierbare Nachweise für interne und externe Audits (Zugriffsprotokolle, Löschprotokolle, Einwilligungshistorien).

## **20.6 Wartbarkeit**

* Jede Modelländerung ist nachvollziehbar (wer, wann, was, warum) und rückabwickelbar.

* Tests auf Modellebene (Regelausführung, Workflow-Pfade, Berechtigungen) sind Teil der Pipeline.

* Dokumentation wird aus dem Modell generiert (Entity-/Regel-/Prozess-Referenz, API-Referenz).

# **21\. Authoring-Umgebung (Designer)**

Die Designer-Umgebung ist das primäre Werkzeug zur Modellerstellung. Sie muss folgende Eigenschaften erfüllen:

* Strikte Live-Validierung gegen das Metamodell; keine persistierbaren ungültigen Zustände.

* Visuelle und textuelle (modell-als-code) Bearbeitung gleichwertig unterstützt; round-trip-fähig.

* Versionsvergleich (diff) auf Modellebene, nicht auf Binärebene.

* Rollenbasierte Authoring-Rechte (z. B. nur bestimmte Bereiche änderbar).

* Vorschau (Preview) auf Testdaten vor Publikation.

* Publikationsfreigabe mit Approvals und Changelog.

* Volltext-Suche und Referenzauflösung ('Wo wird dieses Attribut verwendet?') über das gesamte Modell.

# **22\. Beispielhafte Artefaktstrukturen (illustrativ)**

Die folgenden Skizzen sind beispielhaft und formatfrei – konkrete Serialisierung (JSON, YAML, XML) ist Implementierungsentscheidung.

## **22.1 Entity (Beispiel)**

Entity "Contact" {  id: urn:mdapp:entity:core.contact  version: 1.3.0  primaryAttribute: fullName  ownership: UserOrTeam  flags: { auditEnabled: true, changeTrackingEnabled: true, duplicateDetectionEnabled: true }  attributes: \[    { name: id, type: UniqueIdentifier, systemGenerated: true, primaryKey: true }    { name: fullName, type: String, length: 160, required: true, searchable: true }    { name: email, type: String, format: Email, unique: true, classification: Personal }    { name: birthDate, type: Date, classification: Personal }    { name: preferredLanguage, type: Enum(Language), default: "de-CH" }    { name: owner, type: Lookup(User|Team), required: true }    { name: createdAt, type: DateTime, systemGenerated: true }    { name: modifiedAt, type: DateTime, systemGenerated: true }    { name: versionNumber, type: Integer, systemGenerated: true }  \]  relationships: \[    { name: contact\_to\_cases, kind: OneToMany, target: Case, cascade: Restrict }  \]}

## **22.2 View (Beispiel)**

View "ActiveContacts" for Contact {  columns: \[fullName, email, preferredLanguage, modifiedAt\]  filter: and( eq(status, "Active"), eq(owner, @currentUser) )  sort: \[ desc(modifiedAt) \]  pageSize: 50}

## **22.3 Business Rule (Beispiel)**

BusinessRule "RequireEmailForActive" on Contact.MainForm {  when: eq(status, "Active")  actions: \[    setRequired(field: email, level: Required),    setVisible(field: email, visible: true),    showError(field: email, when: isNull(email),              message: @i18n("contact.email.requiredForActive"))  \]}

## **22.4 Workflow (Beispiel)**

Workflow "NotifyOwnerOnNewCase" {  trigger: onCreate(Case)  steps: \[    sendNotification(      to: @record.owner,      template: "case.new",      channel: \[InApp, Email\]    ),    publishEvent(      type: "case.created.v1",      payload: projection(@record, \[id, title, owner, createdAt\])    )  \]  retry: { attempts: 5, backoff: Exponential }}

## **22.5 Role (Beispiel)**

Role "CaseAgent" {  privileges: \[    (Case, Read, BusinessUnit),    (Case, Update, User),    (Case, Create, BusinessUnit),    (Contact, Read, BusinessUnit),    (Export, Execute, None)  \]}

# **23\. Akzeptanzkriterien für eine Implementierung**

Eine Implementierung gilt als spezifikationskonform, wenn sie mindestens die folgenden Kriterien erfüllt:

15. Vollständiges, öffentlich dokumentiertes Metamodell in maschinenlesbarer Form.

16. Deklarative Definition aller in Abschnitt 4.2 genannten Artefakttypen.

17. Generische Kern-API über alle Entitäten mit den in Abschnitt 14 beschriebenen Fähigkeiten.

18. Durchgesetztes Berechtigungsmodell gemäss Abschnitt 6 inkl. Feldsicherheit und Row-Level-Filter.

19. Regel-, Workflow- und Prozess-Engine mit den in Abschnitten 10–12 geforderten Schritttypen und Garantien.

20. Unveränderlicher Audit-Trail und durchsetzbare Retention.

21. Transactional-Outbox-basierte Ereignisveröffentlichung.

22. Solution-Lifecycle mit Managed/Unmanaged-Trennung, signierten Paketen und automatisiertem Transport.

23. Observability gemäss Abschnitt 19, inkl. verteiltem Tracing und SLO-Messung.

24. Erfüllung der nicht-funktionalen Zielwerte aus Abschnitt 20 in einer definierten Referenz-Lastklasse.

# **24\. Offene Punkte und bewusste Varianten**

Die folgenden Themen sind in dieser Spezifikation bewusst offen gehalten, weil sie stark vom Zielkontext abhängen und in einer Implementierung explizit zu entscheiden sind:

* Primär-Persistenztechnologie (PostgreSQL, SQL Server, o. a.) – relevant für Indexstrategien, Row-Level-Security, Partitionierung.

* Granularität der Multi-Tenancy (Pool-Modell, Silo-Modell, Hybrid).

* Event-Bus-Wahl (Kafka, Pulsar, NATS, Managed Cloud Bus).

* Regel- und Workflow-Engine (eigene Entwicklung vs. Einbettung eines BPMN/DMN-Motors).

* Umfang der Offline-Fähigkeit des Renderers.

* Grad der Unterstützung für Custom UI-Komponenten und zugehöriges Sandboxing (WebComponents, iFrame, o. a.).

* Integration mit bestehenden IdPs und Verzeichnissen.

* Reporting-/Analytics-Strategie (eingebettet vs. ausgekoppelt in Lakehouse/DWH).

# **25\. Referenzierte Konzepte**

Die Spezifikation orientiert sich – ohne an eine spezifische Produktfamilie gebunden zu sein – an etablierten Konzepten aus:

* Domain-Driven Design (Bounded Contexts, Aggregate, Anti-Corruption Layer).

* Event-Driven Architecture (Outbox, CDC, Event Carrier).

* Role-Based und Attribute-Based Access Control (RBAC/ABAC).

* Transactional Outbox und Saga-Muster.

* OData-/REST-Konventionen für generische, metadatengetriebene APIs.

* BPMN/Case Management für geführte Prozesse.

* OpenTelemetry für Observability.