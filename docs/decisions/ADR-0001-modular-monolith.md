# ADR-0001: Use a modular monolith

## Status

Accepted

## Context

The application has several related domains but does not require independently scalable services.

## Decision

Use one Spring Boot deployment organized by business feature, with a separate React frontend.

## Alternatives considered

- Microservices
- Server-side rendered Spring MVC
- A single full-stack JavaScript application

## Consequences

The system remains simple to deploy while preserving internal boundaries. Modules can be extracted later if a demonstrated need emerges.

## Revisit criteria

Revisit when a module requires independent scaling, release cadence, security isolation, or ownership.
