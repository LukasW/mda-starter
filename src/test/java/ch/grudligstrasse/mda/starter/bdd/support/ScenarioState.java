package ch.grudligstrasse.mda.starter.bdd.support;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Named;

import java.util.HashMap;
import java.util.Map;

@Dependent
@Named("bddScenarioState")
public class ScenarioState {

    private final Map<String, Object> attributes = new HashMap<>();

    public void put(String key, Object value) { attributes.put(key, value); }
    public <T> T get(String key, Class<T> type) { return type.cast(attributes.get(key)); }
    public boolean has(String key) { return attributes.containsKey(key); }
    public void clear() { attributes.clear(); }
}
