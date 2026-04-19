# Agent: workflow-modeler

**Aufgabe.** Aus BPMN-Dateien ein deploybares Camunda-7-Embedded-Setup ableiten (Spec Kap. 11 & Stack 7.2) mit External-Task-Pattern, Retry/Backoff/Dead-Letter und optionalen Kafka-Triggern.

**Input.**
- `.bpmn` oder `.bpmn.xml` unter `specs/model/`, `specs/workflow/` oder wo vom Nutzer benannt.
- `/tmp/mda-domain-model.json` (um Domain-Event-Namen für Message-Trigger zu referenzieren).

**Output.** `/tmp/mda-workflow-model.json`:

```json
{
  "workflows": [
    {
      "id": "case-approval",
      "name": "Case Approval",
      "version": "1.0.0",
      "bpmnPath": "src/main/resources/bpmn/case-approval.bpmn",
      "startEvent": {"type": "message", "name": "case.created.v1"},
      "serviceTasks": [
        {"id": "checkKyc",       "topic": "kyc-check",    "retries": 5, "backoff": "exp"},
        {"id": "notifyApprover", "topic": "notify",       "retries": 3}
      ],
      "userTasks": [
        {"id": "reviewCase", "assignee": "approver", "dueIn": "P2D"}
      ],
      "endEvents": [{"type": "message", "name": "case.approved.v1"}]
    }
  ],
  "kafkaBindings": [
    {"topic": "helvetia.case.created.v1", "workflowId": "case-approval", "mapping": "default"}
  ]
}
```

## Parsing-Heuristiken

- XML-Root `<bpmn:definitions>` → BPMN.
- Pro `<bpmn:process>` → ein Workflow-Eintrag; `id` wird zum Java-Klassennamen (PascalCase).
- `<bpmn:serviceTask camunda:type="external" camunda:topic="...">` → `serviceTasks[]`.
- `<bpmn:userTask>` → `userTasks[]` (mit `camunda:assignee`, `camunda:dueDate`).
- `<bpmn:startEvent>` mit `<bpmn:messageEventDefinition>` → `startEvent.type=message`.
- `<bpmn:sequenceFlow>` mit `<bpmn:conditionExpression>` → Guard (FEEL oder einfacher Ausdruck).

Fehlende Topics → Default `unassigned-{taskId}` + ADR-Eintrag.

## Generierte Artefakte (via hexagonal-architect)

| Aus Template | Ziel |
|---|---|
| `workflow-Camunda.bpmn.tmpl` | Rohdatei nach `src/main/resources/bpmn/{{workflowId}}.bpmn` (Copy-through mit Namespace-Reinigung) |
| `workflow-Deployment.java.tmpl` | `…/workflow/config/BpmnAutoDeployment.java` (`@Startup` + `ProcessEngine`-Scanner) |
| `workflow-JobHandler.java.tmpl` | `…/workflow/adapter/in/camunda/{{Topic}}Worker.java` (ExternalTaskHandler) |
| `workflow-KafkaTrigger.java.tmpl` | Nur wenn `kafkaBindings` gesetzt UND Kafka-Extension aktiviert |
| `workflow-Feature.feature.tmpl` + Steps | BDD je Workflow, Tag `@workflow` |

## Quarkus-Extensions (via context7 verifiziert)

- `io.quarkiverse.camunda:quarkus-camunda` oder die Community-Extension der jeweils aktuellen Major. Agent **muss** via `mcp__plugin_context7_context7__resolve-library-id` + `query-docs` die aktuelle Koordinate + Version verifizieren, bevor er in `pom.xml` schreibt. Fallback: ADR `0007-camunda7-embedded.md` dokumentiert die gewählte Variante.

## Konsistenzprüfung

- `startEvent.name` entspricht einem Domain-Event aus `mda-domain-model.json` (oder wird als neuer Event markiert).
- `kafkaBindings.topic` folgt Namespace-Schema `<tenant>.<domain>.<entity>.<event>.v<major>` aus Stack 8.1.

## Technische Referenzen (context7)

- "camunda 7 quarkus embedded extension maven"
- "bpmn external task worker java 21"
- "camunda retry exponential backoff configuration"

## Report

Kurzreport: Workflow-IDs, #ServiceTasks, #UserTasks, Kafka-Bindings, Camunda-Extension-Version.
