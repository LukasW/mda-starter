package ch.grudligstrasse.mda.starter.shared.problem;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class DomainExceptionMapper implements ExceptionMapper<DomainException> {

    public static final String PROBLEM_JSON = "application/problem+json";

    @Override
    public Response toResponse(DomainException ex) {
        int status = statusFor(ex.code());
        ProblemDetail body = ProblemDetail.of(
                status,
                reasonFor(status),
                ex.getMessage(),
                ex.code()
        );
        return Response.status(status)
                .type(MediaType.valueOf(PROBLEM_JSON))
                .entity(body)
                .build();
    }

    private static int statusFor(String code) {
        if (code == null) {
            return 400;
        }
        if (code.endsWith("NOT_FOUND")) {
            return 404;
        }
        if (code.startsWith("MDA-BPF")) {
            return 409;
        }
        return 400;
    }

    private static String reasonFor(int status) {
        return switch (status) {
            case 404 -> "Not Found";
            case 409 -> "Conflict";
            default -> "Bad Request";
        };
    }
}
