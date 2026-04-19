# Workflow (BPMN / Camunda 7 embedded)

Gemäss Stack 7.2. Nur für echte Workflows mit längeren Laufzeiten, Retries, Human Tasks.

## Prinzipien

- Workflows laufen **asynchron**. Synchron nur, wenn < 200 ms und ohne externe Calls.
- External-Task-Pattern für serviceTask — kein direkter Java-Delegate im Engine-Thread.
- Retries mit Exponential Backoff; Dead-Letter nach N Versuchen (Default 5).
- Kompensationsschritte erlauben Saga-artige Konsistenz.

## Ablageort

- BPMN: `src/main/resources/bpmn/{{workflowId}}.bpmn`
- Worker: `ch.grudligstrasse.mda.workflow.adapter.in.camunda.{{Topic}}Worker`
- Deployment: `ch.grudligstrasse.mda.workflow.config.BpmnAutoDeployment`

## Extension-Wahl

Die konkrete Quarkus-Extension für Camunda 7 ist via `context7` pro Lauf zu verifizieren. Der Agent entscheidet zwischen:

- `io.quarkiverse.camunda:quarkus-camunda` (Community)
- Self-managed `camunda-bpm-engine` mit Quarkus-Adapter (Fallback)

Die gewählte Variante + Version landet in ADR `0007-camunda7-embedded.md`.

## Kafka-Trigger (optional)

Pro `startEvent` mit Message-Typ wird ein Kafka-Consumer generiert:

```
helvetia.<domain>.<entity>.<event>.v1  →  correlateMessage(workflowId, payload)
```

Consumer-Klasse `…/workflow/adapter/in/kafka/{{Topic}}Consumer.java` mit SmallRye Reactive Messaging.

## SLOs (Spec Stack 11.6)

- Workflow Start Delay (async) ≤ 5 s.
- End-to-End Event Publish ≤ 2 s (verwandt, nicht Teil von Camunda).
