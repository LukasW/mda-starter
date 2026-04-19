package ch.grudligstrasse.mda.clm.person.application.port.out;

import java.util.List;

/**
 * Out-Port zur optionalen externen Personenverwaltung. Stub-Default liefert leere Listen
 * (Standalone-Modus). Spec Kap. 7.2.
 */
public interface ExternePersonenverwaltungClient {

    boolean isEnabled();

    List<ExternePerson> suchen(String query, int limit);

    record ExternePerson(String externeId, String vorname, String nachname,
                         String email, String organisation, String funktion) {}
}
