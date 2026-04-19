package ch.grudligstrasse.mda.clm.shared.problem;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.List;

@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        List<ProblemDetail.FieldError> fieldErrors = exception.getConstraintViolations().stream()
                .map(ValidationExceptionMapper::toFieldError)
                .toList();
        ProblemDetail body = ProblemDetail.withFieldErrors(
                "Validation failed",
                400,
                "MDA-VAL-001",
                "One or more fields failed validation.",
                fieldErrors);
        return Response.status(400)
                .type(MediaType.APPLICATION_JSON + "; profile=\"urn:ietf:rfc:7807\"")
                .entity(body)
                .build();
    }

    private static ProblemDetail.FieldError toFieldError(ConstraintViolation<?> violation) {
        String path = violation.getPropertyPath() != null ? violation.getPropertyPath().toString() : "<unknown>";
        return new ProblemDetail.FieldError(path, "MDA-VAL-001", violation.getMessage());
    }
}
