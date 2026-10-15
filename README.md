# Spring Petclinic — Graftcode

Original project:
https://github.com/spring-projects/spring-petclinic

This repository keeps the database and business core (owners, pets, visits, vets, Spring Data, H2 / MySQL / Postgres) and hosts it with Graftcode Gateway. MVC controllers and Thymeleaf are not in this port.

The graft surface is the `@Service` class `Clinic`: static methods for the gateway, instance methods and repositories for Spring.

| Application location | Before | With Graftcode | Developer responsibility removed |
| --- | --- | --- | --- |
| Owner, pet, visit, and vet API | Spring MVC / `GET /vets` JSON | `Clinic.getOwner`, `getPets`, `getVisits`, `addVisit`, `listVets` | HTTP routes, form binding, and a handwritten REST client |

On the host, `gg` is the process. The first `Clinic` call starts the Spring context and uses the existing `OwnerRepository`. There is no `spring-boot-starter-web`.

## Run

Prerequisites:

- [Docker](https://docs.docker.com/get-docker/) with Compose
- JDK 17+ and Maven (or `./mvnw`) for tests

```bash
docker compose up --build -d
```

Calls: `ws://localhost:8090/ws`. Graftcode Vision is typically at [http://localhost:8090/GV](http://localhost:8090/GV).

Wait for the Maven feed:

```bash
curl http://localhost:8090/maven
```

You should see `Clinic`, `OwnerDto`, `PetDto`, `VisitDto`, and `VetDto` in Vision. Compose starts Postgres and uses it as the host database. MySQL is also on port 3306. In-process tests stay on H2.

## Smoke (seed data)

- Owner `1`: George Franklin
- Pet `1` (George): Leo the cat, born `2010-09-07`
- Visit on pet `7` (owner `6`): `2013-01-01`, `rabies shot`

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

E2E uses **`GraftConfig.stateless = false`**. The JVM graft client may fail on array return types (`getPets`, `getVisits`); see [`GRAFTCODE.md`](GRAFTCODE.md).

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

More Graftcode notes: [`GRAFTCODE.md`](GRAFTCODE.md).
