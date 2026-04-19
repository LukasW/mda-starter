package ch.grudligstrasse.mda.starter.obligation.application.port.in;

import java.time.LocalDate;

public interface FristErinnernUseCase {
    int erinnere(LocalDate stichtag);
}
