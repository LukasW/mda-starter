package ch.grudligstrasse.mda.clm.shared.problem;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class DomainExceptionMapper implements ExceptionMapper<DomainException> {

    @Override
    public Response toResponse(DomainException exception) {
        ProblemDetail body = ProblemDetail.of(
                "Domain rule violated",
                exception.status(),
                exception.code(),
                exception.getMessage());
        return Response.status(exception.status())
                .type(MediaType.APPLICATION_JSON + "; profile=\"urn:ietf:rfc:7807\"")
                .entity(body)
                .build();
    }
}
