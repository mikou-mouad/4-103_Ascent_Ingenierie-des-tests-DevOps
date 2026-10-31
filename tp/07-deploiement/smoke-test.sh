#!/usr/bin/env bash
# Smoke tests : quelques vérifications rapides après un déploiement. On s'arrête au premier problème.
#
# Usage : bash deploy/smoke-test.sh <url de l'application> <version attendue>
#   ex.   bash deploy/smoke-test.sh http://embarq-preprod:8080 1.0.42
# Chaque réponse est lue entière avant d'être vérifiée : « curl | grep -q » échoue au hasard avec pipefail
# (grep s'arrête dès qu'il a trouvé, curl reçoit alors un SIGPIPE).
set -euo pipefail

URL=${1:?usage : smoke-test.sh <url> <version attendue>}
VERSION=${2:?usage : smoke-test.sh <url> <version attendue>}

echec() { echo "SMOKE TEST ÉCHOUÉ : $*" >&2; exit 1; }
ok() { echo "  ok   $*"; }

echo "Smoke tests sur $URL (version attendue : $VERSION)"

# 1. L'application est en bonne santé (base de données comprise)
SANTE=$(curl -fs -m 5 "$URL/actuator/health") || echec "santé : /actuator/health ne répond pas"
grep -q '"status":"UP"' <<< "$SANTE" || echec "santé : l'application n'est pas UP"
ok "santé : UP"

# 2. C'est bien la version qu'on vient de déployer
# TODO : lire .version dans $URL/actuator/info (avec jq -r), et échouer si elle est différente de $VERSION

# 3. La page d'accueil s'affiche
ACCUEIL=$(curl -fs -m 5 "$URL/") || echec "page d'accueil : pas de réponse"
grep -q "Prochaines traversées" <<< "$ACCUEIL" || echec "page d'accueil : contenu inattendu"
ok "page d'accueil"

# 4. L'API liste des traversées
# TODO : compter les traversées de $URL/api/traversees (jq 'length'), et échouer s'il n'y en a aucune

# 5. Le calcul d'un tarif connu
curl -fs -m 5 "$URL/api/tarifs?adultes=2&enfants=1" | jq -e '.montant == 90' > /dev/null || echec "tarif : une famille ne paie pas 90 €"
ok "tarif : 90 € pour 2 adultes et 1 enfant"

echo "Smoke tests réussis."
