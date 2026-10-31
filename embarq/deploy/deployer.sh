#!/usr/bin/env bash
# Déploie une version d'Embarq dans un environnement, puis attend qu'elle réponde.
#
# Usage : bash deploy/deployer.sh <dev|e2e|preprod|prod> [version]
#   La version est le tag de l'image embarq (local par défaut) : docker build -t embarq:<version> .
set -euo pipefail

ENVIRONNEMENT=${1:?usage : deployer.sh <dev|e2e|preprod|prod> [version]}
export VERSION=${2:-local}
cd "$(dirname "$0")/.."

docker compose -p "embarq-$ENVIRONNEMENT" --env-file "deploy/$ENVIRONNEMENT.env" up -d
echo "Embarq $VERSION déployé en $ENVIRONNEMENT. Démarrage en cours…"

# Depuis Jenkins (réseau lab), l'application s'appelle embarq-<env> ; depuis la machine, localhost:<port>
if [ -f /.dockerenv ]; then
  URL="http://embarq-$ENVIRONNEMENT:8080"
else
  URL="http://localhost:$(grep '^EMBARQ_PORT=' "deploy/$ENVIRONNEMENT.env" | cut -d= -f2)"
fi
for _ in $(seq 60); do
  if curl -fs -m 2 "$URL/actuator/health" | grep -q '"UP"'; then
    echo "Embarq répond sur $URL"
    exit 0
  fi
  sleep 2
done
echo "Embarq ne répond pas sur $URL après 2 minutes. Logs :" >&2
docker compose -p "embarq-$ENVIRONNEMENT" --env-file "deploy/$ENVIRONNEMENT.env" logs --tail 40 app >&2
exit 1
