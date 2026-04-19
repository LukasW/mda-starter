package ch.grudligstrasse.mda.clm.shared.problem;

import java.util.List;

public record ProblemDetail(
        String type,
        String title,
        int status,
        String code,
        String detail,
        String correlationId,
        List<FieldError> errors) {

    public record FieldError(String field, String code, String message) {}

    public static ProblemDetail of(String title, int status, String code, String detail) {
        return new ProblemDetail(
                "https://problems.grudligstrasse.ch/" + code,
                title,
                status,
                code,
                detail,
                null,
                List.of());
    }

    public static ProblemDetail withFieldErrors(String title, int status, String code, String detail,
                                                List<FieldError> errors) {
        return new ProblemDetail(
                "https://problems.grudligstrasse.ch/" + code,
                title,
                status,
                code,
                detail,
                null,
                errors);
    }
}
