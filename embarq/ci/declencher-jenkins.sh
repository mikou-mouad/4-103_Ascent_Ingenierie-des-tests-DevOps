#!/usr/bin/env bash
# Lance le pipeline Jenkins d'Embarq pour la branche poussée, affiche sa console au fil de l'eau,
# et sort en erreur si Jenkins ne finit pas en SUCCESS : le job GitLab prend le résultat de Jenkins.
#
# Variables : JENKINS_USER et JENKINS_TOKEN (obligatoires, définies dans GitLab),
#             JENKINS_URL (http://localhost:8080 par défaut), JENKINS_JOB (embarq par défaut).
# Fournies par GitLab : CI_COMMIT_REF_NAME, CI_COMMIT_SHA. À la main, la branche courante est utilisée.
set -euo pipefail

JENKINS_URL=${JENKINS_URL:-http://localhost:8080}
JENKINS_JOB=${JENKINS_JOB:-embarq}
: "${JENKINS_USER:?JENKINS_USER absente : à définir dans GitLab, Settings > CI/CD > Variables}"
: "${JENKINS_TOKEN:?JENKINS_TOKEN absente : à définir dans GitLab, Settings > CI/CD > Variables}"
BRANCHE=${CI_COMMIT_REF_NAME:-$(git rev-parse --abbrev-ref HEAD)}
COMMIT=${CI_COMMIT_SHA:-$(git rev-parse HEAD)}
AUTH="$JENKINS_USER:$JENKINS_TOKEN"
ENTETES=$(mktemp)
trap 'rm -f "$ENTETES"' EXIT

# Vérification des identifiants et du job, avec un message clair plutôt qu'un code d'erreur de curl
STATUT=$(curl -s -o /dev/null -w '%{http_code}' -u "$AUTH" "$JENKINS_URL/job/$JENKINS_JOB/api/json" || true)
case "$STATUT" in
  200) ;;
  401) echo "Jenkins refuse les identifiants (401) : vérifiez JENKINS_USER (admin) et JENKINS_TOKEN (le jeton d'API, pas le mot de passe)." >&2; exit 1 ;;
  403) echo "Jenkins refuse l'accès (403) : l'utilisateur $JENKINS_USER n'a pas les droits sur le job." >&2; exit 1 ;;
  404) echo "Le job Jenkins « $JENKINS_JOB » n'existe pas (404) : vérifiez GITLAB_PROJET dans lab/.env, puis docker compose up -d." >&2; exit 1 ;;
  000) echo "Jenkins ne répond pas sur $JENKINS_URL : est-il démarré ? (cd ~/iroise/lab && docker compose ps)" >&2; exit 1 ;;
  *) echo "Réponse inattendue de Jenkins : $STATUT" >&2; exit 1 ;;
esac

echo "Lancement du job Jenkins « $JENKINS_JOB » pour la branche $BRANCHE ($COMMIT)"
FILE=$(curl -fsS -u "$AUTH" -X POST -D - -o /dev/null \
         --data-urlencode "BRANCHE=$BRANCHE" --data-urlencode "COMMIT=$COMMIT" \
         "$JENKINS_URL/job/$JENKINS_JOB/buildWithParameters" \
       | tr -d '\r' | awk 'tolower($1) == "location:" { print $2 }')
if [ -z "$FILE" ]; then
  echo "Jenkins n'a pas accepté la demande : vérifiez l'utilisateur et le jeton." >&2
  exit 1
fi

# 1. Attendre que le build quitte la file d'attente de Jenkins
NUMERO=""
until [ -n "$NUMERO" ]; do
  sleep 2
  NUMERO=$(curl -fsS -u "$AUTH" "${FILE}api/json" | jq -r '.executable.number // empty')
done
BUILD="$JENKINS_URL/job/$JENKINS_JOB/$NUMERO"
echo "Build Jenkins n° $NUMERO : $BUILD/"
echo "------------------------------------------------------------------------"

# 2. Recopier la console Jenkins jusqu'à la fin du build
DEBUT=0
while true; do
  curl -fsS -u "$AUTH" -D "$ENTETES" "$BUILD/logText/progressiveText?start=$DEBUT"
  DEBUT=$(tr -d '\r' < "$ENTETES" | awk 'tolower($1) == "x-text-size:" { print $2 }')
  ENCORE=$(tr -d '\r' < "$ENTETES" | awk 'tolower($1) == "x-more-data:" { print $2 }')
  [ "$ENCORE" = "true" ] || break
  sleep 3
done

# 3. Le résultat de Jenkins devient celui du job GitLab
RESULTAT=$(curl -fsS -u "$AUTH" "$BUILD/api/json" | jq -r '.result')
echo "------------------------------------------------------------------------"
echo "Résultat Jenkins : $RESULTAT ($BUILD/)"
[ "$RESULTAT" = "SUCCESS" ]
