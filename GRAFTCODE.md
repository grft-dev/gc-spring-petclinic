# Spring Petclinic — Graftcode

This repo keeps the Petclinic **database and business core** (owners, pets, visits, vets, Spring Data, H2 / MySQL / Postgres) and hosts it with Graftcode Gateway (`gg`). REST, MVC, and Thymeleaf are removed.

## Prerequisites

- [Docker](https://docs.docker.com/get-docker/) with Compose
- JDK 17+ and Maven (or `./mvnw`) for tests

## Run the host

From the repo root:

```bash
docker compose up --build -d
```

Calls: `ws://localhost:8090/ws`. Graftcode Vision is typically at [http://localhost:8090/GV](http://localhost:8090/GV) (check `docker compose logs backend` if the URL differs).

Wait for the Maven feed:

```bash
curl http://localhost:8090/maven
```

The graft host JAR is `org.springframework.samples:petclinic-clinic:1.0.0`. Vision should expose `Clinic`, `OwnerDto`, `PetDto`, `VisitDto`, and `VetDto`. Compose starts Postgres and points the host at it (`SPRING_PROFILES_ACTIVE=postgres`). MySQL is also available on port 3306. In-process tests stay on H2.

## Smoke (seed data)

- Owner `1`: George Franklin
- Pet `1` (George): Leo the cat, born `2010-09-07`
- Visit on pet `7` (owner `6`): `2013-01-01`, `rabies shot`
- Vet `3`: Linda Douglas (dentistry, surgery)

## Tests

In-process (Spring + H2, no gateway):

```bash
./mvnw test -Dtest=ClinicGraftTest
```

Cross-boundary (Docker + JVM graft client):

```powershell
./run-e2e.ps1
```

On Linux/macOS: `./run-e2e.sh`

E2E uses **`GraftConfig.stateless = false`** (required for DTO getters through the graft; see product notes on `stateless: true`).

The JVM graft client may fail on **array** return types (`getPets`, `getVisits`) with `NoSuchElementException` in generated `GraftUtils.getUnderlyingArrayType` (defect #4 in [`petclinic-graftcode-defects.md`](../petclinic-graftcode-defects.md)). The reference monolith uses npm for full e2e; this repo keeps JVM tests as the cross-boundary check for scalar calls and error propagation.

## Graft API

```text
Clinic.listOwners()
Clinic.listVets()
Clinic.getOwner(int id)
Clinic.getPets(int ownerId)
Clinic.getVisits(int ownerId, int petId)
Clinic.addVisit(int ownerId, int petId, String date, String description)
```

Visit dates on `VisitDto` are ISO-8601 strings.

## Build host JAR locally

```bash
./mvnw package -DskipTests
```

Produces `target/petclinic-clinic-1.0.0.jar` and `target/dependency/` for `gg --modules`.

## Async / messaging (out of scope)

This codebase has no queues, gRPC, or WebSockets. The original communication was synchronous HTTP (MVC and `/vets` JSON). That transport is removed; `gg` hosts the remaining core.
