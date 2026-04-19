# ADR 0003 — Authentifizierung als Hybrid OIDC + lokale Fallback-Accounts

**Status:** akzeptiert (AE-01)
**Datum:** 2026-04-19

## Kontext
Kunden betreiben Keycloak/Azure AD und wollen SSO. Gleichzeitig braucht der Betrieb lokale Admin-Accounts fuer Notfaelle und Serviceaccounts.

## Entscheidung
- Primaerer Anmeldepfad: OIDC (Auth Code + PKCE fuer UI, Client Credentials fuer Services).
- Lokale Accounts nur fuer Administratoren / Systemaccounts.
- Rollen koennen aus OIDC-Claims uebernommen werden; lokale Ueberschreibung ist moeglich.

## Konsequenzen
- Im Erstentwurf ist OIDC noch **nicht** verdrahtet (nur Vorbereitung). Ein spaeteres Feature integriert `quarkus-oidc`.
- Lokale Accounts brauchen eine minimale interne Benutzerverwaltung (Audit, MFA-Delegation an IdP bei OIDC-Nutzung).
