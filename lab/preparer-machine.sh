#!/usr/bin/env bash
# Prépare la machine d'un participant pour la formation (Ubuntu 24.04) : c'est le guide d'installation
# (guide-installation-lab.md), en une commande. À lancer la veille, avec le compte du participant
# (pas root), qui doit pouvoir utiliser sudo :
#
#   git clone https://github.com/mikou-mouad/4-103_Ingenierie-des-tests-DevOps.git ~/iroise
#   bash ~/iroise/lab/preparer-machine.sh
#
# Options : --vscode (installe VS Code), --intellij (installe IntelliJ IDEA Community),
#           --sans-construction (ne lance pas le « mvn verify » de vérification, plus rapide).
# Le script peut être relancé : ce qui est déjà fait est sauté.
set -euo pipefail

DEPOT=https://github.com/mikou-mouad/4-103_Ingenierie-des-tests-DevOps.git
DOSSIER="$HOME/iroise"
IMAGES=(
  postgres:17-alpine
  eclipse-temurin:21-jre
  grafana/k6:1.8.2
  selenium/standalone-chrome:155.0-chromedriver-155.0-grid-4.51.0-20261010
  jenkins/jenkins:2.580.2-lts-jdk21
)
VSCODE=non
INTELLIJ=non
CONSTRUCTION=oui
for option in "$@"; do
  case "$option" in
    --vscode) VSCODE=oui ;;
    --intellij) INTELLIJ=oui ;;
    --sans-construction) CONSTRUCTION=non ;;
    *) echo "Option inconnue : $option" >&2; exit 1 ;;
  esac
done

etape() { echo; echo "=== $* ==="; }

if [ "$(id -u)" -eq 0 ]; then
  echo "Lancez ce script avec le compte du participant, pas en root (il utilise sudo quand il le faut)." >&2
  exit 1
fi
sudo -v

etape "1/8 Outils de base : git, curl, jq, Java 21, Maven"
sudo apt-get update -q
sudo apt-get install -y -q git curl jq openjdk-21-jdk maven ca-certificates

etape "2/8 Docker (dépôt officiel)"
if ! command -v docker > /dev/null; then
  sudo install -m 0755 -d /etc/apt/keyrings
  sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
  echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo "$VERSION_CODENAME") stable" \
    | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
  sudo apt-get update -q
  sudo apt-get install -y -q docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
else
  echo "Docker est déjà installé."
fi
sudo usermod -aG docker "$USER"

etape "3/8 GitLab Runner"
if ! command -v gitlab-runner > /dev/null; then
  curl -fsSL https://packages.gitlab.com/install/repositories/runner/gitlab-runner/script.deb.sh | sudo bash
  sudo apt-get install -y -q gitlab-runner
else
  echo "GitLab Runner est déjà installé."
fi
# Le .bash_logout d'Ubuntu fait échouer les jobs du runner (prepare environment: exit status 1)
sudo rm -f /home/gitlab-runner/.bash_logout

etape "4/8 Éditeur"
if [ "$VSCODE" = oui ]; then sudo snap install code --classic; fi
if [ "$INTELLIJ" = oui ]; then sudo snap install intellij-idea-community --classic; fi
if [ "$VSCODE" = non ] && [ "$INTELLIJ" = non ]; then echo "Aucun (options --vscode ou --intellij)."; fi

etape "5/8 Le dépôt de la formation dans $DOSSIER"
if [ -d "$DOSSIER/.git" ]; then
  git -C "$DOSSIER" fetch -q origin
  echo "Dépôt déjà présent, mis à jour."
else
  git clone -q "$DEPOT" "$DOSSIER"
fi

# Le compte n'a le groupe docker qu'après reconnexion : jusque-là, les commandes Docker passent par « sg docker ».
avec_docker() { sg docker -c "$*"; }

etape "6/8 Images Docker et réseau lab"
for image in "${IMAGES[@]}"; do
  avec_docker "docker pull -q $image"
done
avec_docker "docker network inspect lab > /dev/null 2>&1 || docker network create lab"

etape "7/8 Image Jenkins du lab"
cd "$DOSSIER/lab"
[ -f .env ] || cp .env.exemple .env
avec_docker "docker compose build -q"

etape "8/8 Dépendances Maven et vérification complète (solution finale)"
mkdir -p "$HOME/.m2"
cd "$DOSSIER"
BRANCHE_INITIALE=$(git branch --show-current)
if [ "$CONSTRUCTION" = oui ]; then
  git checkout -q solution-finale
  avec_docker "cd '$DOSSIER/embarq' && mvn -B -q verify"
  echo "BUILD SUCCESS : tests unitaires, Cucumber et tests d'intégration passent."
else
  echo "Sautée (--sans-construction)."
fi
if [ -z "$BRANCHE_INITIALE" ] || [ "$BRANCHE_INITIALE" = solution-finale ] || [ "$BRANCHE_INITIALE" = main ]; then
  BRANCHE_INITIALE=tp-00-pipeline
fi
git checkout -q "$BRANCHE_INITIALE"

etape "Vérifications"
echo "Java    : $(java -version 2>&1 | head -1)"
echo "Maven   : $(mvn -version 2>/dev/null | head -1)"
echo "Docker  : $(avec_docker 'docker version --format {{.Server.Version}}')"
echo "Compose : $(avec_docker 'docker compose version --short')"
echo "Runner  : $(gitlab-runner --version 2>/dev/null | head -1)"
echo "Branche : $(git -C "$DOSSIER" branch --show-current)"
OCCUPES=$(ss -ltn | grep -E ':(8080|8081|8082|8089|8090|4444|7900) ' || true)
if [ -n "$OCCUPES" ]; then
  echo "ATTENTION, ports déjà utilisés :"
  echo "$OCCUPES"
fi

echo
echo "Machine prête. Dernière étape : FERMEZ LA SESSION UBUNTU ET ROUVREZ-LA (ou redémarrez),"
echo "pour que le compte « $USER » utilise Docker sans sudo. Puis vérifiez : docker ps"
