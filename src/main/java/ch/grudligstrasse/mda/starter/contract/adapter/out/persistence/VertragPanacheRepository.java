package ch.grudligstrasse.mda.starter.contract.adapter.out.persistence;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class VertragPanacheRepository implements PanacheRepositoryBase<VertragJpaEntity, UUID> { }
