#!/usr/bin/env bash
# Indicateurs du pipeline d'Embarq, calculés à partir de l'API de Jenkins (et du rapport JaCoCo local s'il existe).
#
# Usage : JENKINS_USER=admin JENKINS_TOKEN=<jeton> bash ci/indicateurs.sh [nombre de builds, 20 par défaut]
set -euo pipefail

JENKINS_URL=${JENKINS_URL:-http://localhost:8080}
JENKINS_JOB=${JENKINS_JOB:-embarq}
N=${1:-20}
AUTH="${JENKINS_USER:?JENKINS_USER absente}:${JENKINS_TOKEN:?JENKINS_TOKEN absente}"

# Les N derniers builds terminés : réussite, durée, temps de réparation
curl -fsS -u "$AUTH" "$JENKINS_URL/job/$JENKINS_JOB/api/json?tree=builds[number,result,duration,timestamp]{0,$N}" \
| jq -r '
  [.builds[] | select(.result != null)] as $b
  | ($b | length) as $total
  | ($b | map(select(.result == "SUCCESS")) | length) as $ok
  | ($b | sort_by(.number)
       | reduce .[] as $x ({debut: null, durees: []};
           if $x.result != "SUCCESS" then (if .debut == null then .debut = $x.timestamp else . end)
           elif .debut != null then .durees += [$x.timestamp + $x.duration - .debut] | .debut = null
           else . end)) as $r
  | "Builds analysés             : \($total)",
    "Taux de réussite            : \(if $total > 0 then ($ok * 100 / $total | floor) else 0 end) % (\($ok) sur \($total))",
    "Durée moyenne d'\''un build    : \(if $total > 0 then ($b | map(.duration) | add / $total / 1000 | floor) else 0 end) s",
    "Build le plus long          : \(if $total > 0 then ($b | map(.duration) | max / 1000 | floor) else 0 end) s",
    "Temps moyen de réparation   : \(if ($r.durees | length) > 0 then ($r.durees | add / length / 60000 | floor | tostring) + " min" else "aucun échec réparé sur la période" end)"'

# Les tests du dernier build terminé
curl -fsS -u "$AUTH" "$JENKINS_URL/job/$JENKINS_JOB/lastCompletedBuild/testReport/api/json?tree=passCount,failCount,skipCount" 2>/dev/null \
| jq -r '"Tests du dernier build      : \(.passCount) réussis, \(.failCount) en échec, \(.skipCount) ignorés"' \
|| echo "Tests du dernier build      : pas de rapport"

# La couverture, d'après le dernier « mvn verify » lancé sur cette machine
CSV="$(dirname "$0")/../target/site/jacoco/jacoco.csv"
if [ -f "$CSV" ]; then
  awk -F, 'NR > 1 { m += $8; c += $9 } END { if (m + c > 0) printf "Couverture des lignes       : %.1f %% (rapport local)\n", 100 * c / (m + c) }' "$CSV"
fi
