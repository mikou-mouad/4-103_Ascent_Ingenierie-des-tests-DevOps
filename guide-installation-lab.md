# Guide d'installation du lab

Ce guide prépare **la machine d'un participant**. Il se fait **la veille de la formation**, par le formateur ou par le participant : il installe les outils et télécharge les images et les dépendances, pour que les TP ne commencent pas par un quart d'heure de téléchargement.

Le lab lui-même (Jenkins et Selenium Grid) est décrit comme du code dans `lab/` : on le démarre pendant le TP 00.

## En une commande

Le script `lab/preparer-machine.sh` fait les sections 3 à 8 de ce guide. Lancez-le avec le compte du participant (pas root), qui doit pouvoir utiliser `sudo` :

```bash
sudo apt-get update && sudo apt-get install -y git
git clone https://github.com/mikou-mouad/4-103_Ingenierie-des-tests-DevOps.git ~/iroise
bash ~/iroise/lab/preparer-machine.sh --vscode
```

`--vscode` installe VS Code, `--intellij` installe IntelliJ IDEA Community. Comptez 20 à 40 minutes selon le réseau : la dernière étape construit la solution finale avec tous ses tests, ce qui télécharge les dépendances Maven et vérifie que Docker et Testcontainers fonctionnent. Le script peut être relancé sans risque.

À la fin, **fermez la session Ubuntu et rouvrez-la** (ou redémarrez), puis vérifiez que `docker ps` fonctionne sans `sudo`. Si le terminal affiche des logs à chaque commande, appliquez aussi la section 2.

Les sections suivantes détaillent ce que fait le script, pour une installation à la main ou un dépannage.

## 1. La machine

Une machine par participant, avec les droits administrateur (`sudo`).

| Élément | Valeur |
|---------|--------|
| Système | Ubuntu 24.04 LTS avec bureau (navigateur Firefox) |
| Processeur | 4 vCPU minimum, 6 conseillés |
| Mémoire | 16 Go |
| Disque | 60 Go |
| Réseau | Accès Internet sortant : gitlab.com, Docker Hub, Maven Central, dépôts Ubuntu et Docker |

Tout tourne sur cette machine, dans Docker :

| Service | Adresse depuis la machine | Nom dans le réseau Docker `lab` | Démarré au |
|---------|---------------------------|----------------------------------|------------|
| Jenkins | http://localhost:8080 | `jenkins` | TP 00 |
| Selenium Grid | http://localhost:4444 | `selenium` | TP 00 |
| Bureau du navigateur de test (noVNC) | http://localhost:7900 | | TP 00 |
| Embarq « dev » | http://localhost:8090 | `embarq-dev` | TP 00 |
| Embarq « e2e » (tests end-to-end) | http://localhost:8089 | `embarq-e2e` | TP 05 |
| Embarq « preprod » | http://localhost:8081 | `embarq-preprod` | TP 07 |
| Embarq « prod » | http://localhost:8082 | `embarq-prod` | TP 07 |
| GitLab Runner | (service de la machine) | | TP 00 |

GitLab est **gitlab.com** : chaque participant y crée un compte gratuit avant la formation. Aucun port de la machine n'est ouvert sur Internet : le GitLab Runner va chercher ses jobs chez gitlab.com.

## 2. Préparer le terminal

Sur certains labs, le terminal affiche des logs de « shell integration » à chaque commande. Si c'est le cas :

```bash
unset PROMPT_COMMAND
PS1='\u@\h:\w\$ '
PS0=''
```

Pour que ce soit permanent :

```bash
echo 'unset PROMPT_COMMAND; PS1="\u@\h:\w\$ "' >> ~/.bashrc
echo 'PS0=""' >> ~/.bashrc
source ~/.bashrc
```

Si les logs reviennent, cherchez le fichier qui redéfinit `PROMPT_COMMAND`, et ajoutez la même ligne à la fin de ce fichier :

```bash
grep -n "machineid\|PROMPT_COMMAND" ~/.bashrc ~/.profile /etc/profile.d/* 2>/dev/null
echo 'unset PROMPT_COMMAND; PS1="\u@\h:\w\$ "; PS0=""' | sudo tee -a <fichier>
```

## 3. Les outils de base

```bash
sudo apt-get update
sudo apt-get install -y git curl jq openjdk-21-jdk maven
java -version
mvn -version
```

`java -version` doit afficher 21, et `mvn -version` doit utiliser ce Java 21.

## 4. Docker

Docker vient du dépôt officiel de Docker (le paquet `docker.io` d'Ubuntu n'a pas toujours le plugin Compose récent).

```bash
sudo install -m 0755 -d /etc/apt/keyrings
sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | sudo tee /etc/apt/sources.list.d/docker.list
sudo apt-get update
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
sudo usermod -aG docker "$USER"
```

**Fermez la session Ubuntu et rouvrez-la** (ou redémarrez) pour que le groupe `docker` soit pris en compte, puis :

```bash
docker run --rm hello-world
docker compose version
```

**Limites de Docker Hub.** Sans compte, Docker Hub limite les téléchargements par adresse IP. Si toute la salle sort par la même IP, la limite peut tomber pendant la formation (`toomanyrequests`). Pour l'éviter, téléchargez les images la veille (section 7), et connectez chaque machine avec un compte Docker Hub gratuit : `docker login`.

## 5. Le GitLab Runner

Le runner reçoit les jobs GitLab et les exécute sur la machine. On l'installe ici, on l'enregistre pendant le TP 00.

On utilise le binaire officiel : le dépôt apt de `packages.gitlab.com` peut rester bloqué sans rien afficher.

```bash
sudo curl -fL --connect-timeout 20 -o /usr/local/bin/gitlab-runner "https://s3.dualstack.us-east-1.amazonaws.com/gitlab-runner-downloads/latest/binaries/gitlab-runner-linux-amd64"
sudo chmod +x /usr/local/bin/gitlab-runner
id gitlab-runner || sudo useradd --create-home --shell /bin/bash gitlab-runner
sudo gitlab-runner install --user=gitlab-runner --working-directory=/home/gitlab-runner
sudo gitlab-runner start
sudo rm -f /home/gitlab-runner/.bash_logout
gitlab-runner --version
```

Le fichier `.bash_logout` d'Ubuntu fait échouer les jobs du runner (`prepare environment: exit status 1`) : c'est pour cela qu'on le supprime.

## 6. L'éditeur

Au choix :

```bash
sudo snap install code --classic
```

puis, dans VS Code, l'extension **Extension Pack for Java** (Microsoft) et l'extension **Cucumber** (Cucumber). Ou bien :

```bash
sudo snap install intellij-idea-community --classic
```

## 7. Le dépôt, les images et les dépendances

Récupérez le dépôt de la formation :

```bash
git clone https://github.com/mikou-mouad/4-103_Ingenierie-des-tests-DevOps.git ~/iroise
cd ~/iroise
```

Téléchargez les images Docker utilisées pendant les deux jours :

```bash
docker pull postgres:17-alpine
docker pull eclipse-temurin:21-jre
docker pull grafana/k6:1.8.2
docker pull selenium/standalone-chrome:155.0-chromedriver-155.0-grid-4.51.0-20261010
docker pull jenkins/jenkins:2.580.2-lts-jdk21
```

Créez le réseau Docker partagé par le lab et les environnements d'Embarq, puis construisez l'image Jenkins du lab (plugins compris) :

```bash
docker network create lab
cd ~/iroise/lab
cp .env.exemple .env
docker compose build
```

Enfin, construisez la version finale d'Embarq, avec tous ses tests. Cela télécharge toutes les dépendances Maven des deux jours dans `~/.m2`, que Jenkins réutilisera, et vérifie que Docker et Testcontainers fonctionnent :

```bash
mkdir -p ~/.m2
cd ~/iroise
git checkout solution-finale
cd embarq
mvn -B verify
```

La commande dure plusieurs minutes la première fois. Elle doit finir par `BUILD SUCCESS`, après les tests unitaires, les scénarios Cucumber et les tests d'intégration. Revenez ensuite au point de départ du TP 00 :

```bash
cd ~/iroise
git checkout tp-00-pipeline
```

## 8. Vérifications

| Vérification | Commande | Attendu |
|--------------|----------|---------|
| Java 21 | `java -version` | `21` |
| Maven | `mvn -version` | `Java version: 21` |
| Docker sans sudo | `docker ps` | pas d'erreur de permission |
| Compose | `docker compose version` | v2 ou plus |
| Images | `docker images` | les 5 images de la section 7, plus `lab-jenkins` |
| Runner | `gitlab-runner --version` | une version s'affiche |
| Dépôt | `cd ~/iroise && git branch --show-current` | `tp-00-pipeline` |
| Réseau | `docker network ls \| grep lab` | une ligne `lab` |
| Ports libres | `ss -ltn \| grep -E ':(8080\|8081\|8082\|8089\|8090\|4444\|7900) '` | rien |

## 9. Dépannage

| Symptôme | Cause | Solution |
|----------|-------|----------|
| `permission denied … docker.sock` | La session n'a pas encore le groupe `docker` | Fermer la session Ubuntu et la rouvrir |
| `toomanyrequests: You have reached your pull rate limit` | Limite de Docker Hub | `docker login` avec un compte gratuit, ou attendre |
| `mvn` utilise Java 17 ou 11 | Plusieurs Java installés | `sudo update-alternatives --config java`, choisir 21 |
| Testcontainers : `Could not find a valid Docker environment` | Docker ne répond pas ou pas de droits | `docker ps` doit fonctionner sans sudo |
| `~/.m2` appartient à root | Docker a créé le dossier au premier lancement de Jenkins | `sudo chown -R "$USER": ~/.m2` |
| Le job GitLab échoue avec `prepare environment: exit status 1` | Le `.bash_logout` du compte `gitlab-runner` | `sudo rm -f /home/gitlab-runner/.bash_logout` |
| Le port 8080 est déjà pris | Un autre service tourne | `sudo ss -ltnp \| grep :8080`, arrêter ce service |
