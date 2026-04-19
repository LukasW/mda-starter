# ADR 0002 — Quarkus 3.34 LTS + Java 25

- Status: accepted
- Datum: 2026-04-19

## Kontext

Der Starter soll auf aktueller, unterstuetzter Quarkus-LTS laufen und Java-Records / Sealed Interfaces produktiv nutzen (DDD-Freundlichkeit).

## Entscheid

`quarkus.platform.version = 3.34.5` (aus bestehendem `pom.xml`, per context7 verifiziert), `maven.compiler.release = 25`. Quarkus BOM steuert die Versionen aller `io.quarkus:*`-Extensions.

## Konsequenzen

- Nur Nicht-Quarkus-Dependencies (Cucumber-BOM 7.34.0, ArchUnit 1.4.1) brauchen explizite Versionen.
- Native-Build optional ueber Profil `-P native`.
- Java 25 bedingt aktuelle Maven + JDK 25 in CI.
