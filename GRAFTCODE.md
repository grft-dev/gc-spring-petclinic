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

The graft host JAR is `org.springframework.samples:petclinic-clinic:1.0.0`. Vision should expose `Clinic` and the domain types the facade returns (`Owner`, `Pet`, `Visit`, `Vet`). It can also see the public superclasses (`BaseEntity`, `Person`, `NamedEntity`), the public Spring Data repository interfaces, and `ClinicHost`. `ClinicHost` is public only so `owner.Clinic` can start the package-private boot class. `PetType` and `Specialty` are package-private. Compose starts Postgres and points the host at it (`SPRING_PROFILES_ACTIVE=postgres`). MySQL is also available on port 3306. In-process tests stay on H2.

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

`./mvnw test` calls the public static facade (`Clinic.getOwner`, `getPets`, `getVisits`, `addVisit`, `listVets`) and does not start the gateway. `src/graft-test` still imports the previous generated DTO client, so the `graft-e2e` profile does not compile against this host. Gateway transfer of `Owner`, `Pet`, `Visit`, `Vet`, `List`, and `LocalDate` was not re-run.

## Graft API

```text
Clinic.listOwners()                         -> List<Owner>
Clinic.listVets()                           -> Collection<Vet>
Clinic.getOwner(int id)                     -> Owner
Clinic.getPets(int ownerId)                 -> List<Pet>
Clinic.getVisits(int ownerId, int petId)    -> Collection<Visit>
Clinic.addVisit(int ownerId, int petId, LocalDate date, String description) -> Visit
```

Birth dates and visit dates stay `LocalDate` on the entities. `addVisit` returns the same `Visit` instance passed to `Owner.addVisit` after `saveAndFlush`.

## src/main/java against origin/main

`git diff --numstat origin/main...HEAD -- src/main/java` at `d0cced3` is **194** insertions and **968** deletions. The previous commit on this branch (`82922e6`) had 408 insertions in the same path, including the DTO copies.

| miejsce | linie na main | linie po zmianie | co zniknęło z kodu aplikacji |
| --- | ---: | ---: | --- |
| `PetClinicApplication.java` | 36 | 0 | klasa `main` i start aplikacji web |
| `PetClinicRuntimeHints.java` | 38 | 0 | podpowiedzi AOT dla zasobów web |
| `package-info.java` → `ClinicGraftBoot.java` | 16 | 23 | opis pakietu; została klasa startu bez publicznych metod |
| `system/package-info.java` → `owner/ClinicException.java` | 16 | 24 | opis pakietu; został komunikat błędu kliniki |
| `owner/Clinic.java` | 0 | 114 | nie było na main; fasada woła repozytoria i `Owner.addVisit`, bez kopii pól i bez drugiego odczytu wizyty |
| `ClinicHost.java` | 0 | 62 | nie było na main; start Springa przy pierwszym wywołaniu i `preferLogbackOverGatewayNop` |
| `owner/OwnerController.java` | 188 | 0 | HTTP właścicieli |
| `owner/PetController.java` | 207 | 0 | HTTP zwierząt |
| `owner/VisitController.java` | 114 | 0 | HTTP wizyt |
| `vet/VetController.java` | 81 | 0 | HTTP weterynarzy |
| `system/WelcomeController.java` | 30 | 0 | strona powitalna |
| `system/CrashController.java` | 37 | 0 | endpoint błędu |
| `system/WebConfiguration.java` | 60 | 0 | konfiguracja MVC |
| `owner/PetTypeFormatter.java` | 62 | 0 | formatter formularza |
| `owner/PetValidator.java` | 69 | 0 | walidacja formularza zwierzęcia |
| `vet/Vets.java` | 43 | 0 | opakowanie JSON listy weterynarzy |
| `owner/PetType.java` | 30 | 30 | modyfikator `public`; encja nie jest zwrotem fasady |
| `vet/Specialty.java` | 32 | 32 | modyfikator `public`; encja nie jest zwrotem fasady |

## Build host JAR locally

```bash
./mvnw package -DskipTests
```

Produces `target/petclinic-clinic-1.0.0.jar` and `target/dependency/` for `gg --modules`.

## Async / messaging (out of scope)

This codebase has no queues, gRPC, or WebSockets. The original communication was synchronous HTTP (MVC and `/vets` JSON). That transport is removed; `gg` hosts the remaining core.
