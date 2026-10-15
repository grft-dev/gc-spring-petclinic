#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"

docker compose --profile graft up --build -d

guid=""
for _ in $(seq 1 60); do
  if body="$(curl -fsS --max-time 5 http://localhost:8090/maven 2>/dev/null)"; then
    if guid="$(printf '%s' "$body" | sed -n 's|.*https://grft.dev/maven2/\([0-9a-f-]*\)__free.*|\1|p' | head -1)"; then
      if [ -n "$guid" ]; then
        break
      fi
    fi
  fi
  sleep 2
done

if [ -z "$guid" ]; then
  echo "Graftcode Gateway did not publish a Maven repository at http://localhost:8090/maven" >&2
  exit 1
fi

echo "graft.guid=$guid"

artifact_ready=0
for _ in $(seq 1 60); do
  if ./mvnw -q -U dependency:get \
    "-Dartifact=graft.maven.org.springframework.samples:petclinic-clinic:1.0.0" \
    "-DremoteRepositories=graftcode::::https://grft.dev/maven2/${guid}__free" 2>/dev/null; then
    artifact_ready=1
    break
  fi
  sleep 2
done

if [ "$artifact_ready" -ne 1 ]; then
  echo "Graft Maven client petclinic-clinic:1.0.0 was not published for guid $guid" >&2
  exit 1
fi

./mvnw test "-Dgraft.guid=$guid"
