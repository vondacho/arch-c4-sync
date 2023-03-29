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

- Kotlin 1.8.x
- [Gradle 7.5](https://docs.gradle.org/current/userguide/userguide.html)
- [Allure](https://docs.qameta.io/allure/)
- [Structurizr](https://github.com/structurizr/)

## Deployment

[Github packages](https://github.com/vondacho/arch-c4-sync/pkgs/container/arch-c4-sync)

## CICD

[Github actions](https://github.com/vondacho/arch-c4-sync/actions)