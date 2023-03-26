# Architecture

## Context

- [C4 system view](https://www.structurizr.com/share/38199/diagrams#c4-system)
- [C4 containers view](https://www.structurizr.com/share/38199/diagrams#c4-sync)

## Micro design

### Clean architecture

The logical layers are organized like an onion.

- **appl** (application logic, orchestration of the use cases, transactional): `- uses ->` _domain_
- **domain** (Domain model composed of core entities and core logic)
- **external** (External systems models and adapters) `- imports ->` _appl_, _domain_

## Domain model

## Implementation

### Stacks, libraries, frameworks

- JDK 11
- Kotlin 1.5.x
- [Gradle 6.9](https://docs.gradle.org/current/userguide/userguide.html)
- [Allure](https://docs.qameta.io/allure/)
- [Structurizr](https://github.com/structurizr/)

## Deployment

[DockerHub](https://hub.docker.com/r/noia/arch-c4-sync)

## Monitoring
## Alerting
## Security
## CICD

## Decision records
