$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

docker compose up --build -d

$guid = $null
for ($i = 0; $i -lt 60; $i++) {
    try {
        $body = (Invoke-WebRequest -Uri "http://localhost:8090/maven" -UseBasicParsing -TimeoutSec 5).Content
        $match = [regex]::Match($body, "https://grft\.dev/maven2/([0-9a-f-]+)__free")
        if ($match.Success) {
            $guid = $match.Groups[1].Value
            break
        }
    }
    catch {
        Start-Sleep -Seconds 2
    }
}

if (-not $guid) {
    throw "Graftcode Gateway did not publish a Maven repository at http://localhost:8090/maven"
}

Write-Output "graft.guid=$guid"

$artifactReady = $false
for ($i = 0; $i -lt 120; $i++) {
    ./mvnw -q -U "-Dgraft.guid=$guid" dependency:resolve 2>$null
    if ($LASTEXITCODE -eq 0) {
        $artifactReady = $true
        break
    }
    Start-Sleep -Seconds 2
}

if (-not $artifactReady) {
    throw "Graft Maven client petclinic-clinic:1.0.0 was not published for guid $guid"
}

./mvnw test "-Dgraft.guid=$guid"
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}
