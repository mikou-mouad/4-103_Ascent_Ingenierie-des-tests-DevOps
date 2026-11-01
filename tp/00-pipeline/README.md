# TP 00 – Le pipeline d'Embarq

**Durée :** 1 h (9h30 – 10h30) · **Branche :** `tp-00-pipeline` · **Correction :** `solutions/00-pipeline/`

## L'incident

> L'été dernier, Tom a corrigé le surbooking un vendredi à 18h. Inès a recliqué le site pendant deux jours, puis Tom a copié le jar sur le serveur, à la main. Le samedi matin, plus personne ne pouvait réserver. Personne ne savait quelle version tournait, ni ce qui avait été testé.
>
> **Maëlle :** « Cette année, je veux savoir à chaque instant ce qui part en production, et ce qui a été vérifié. »

## Objectifs

- Démarrer un lab décrit comme du code : Jenkins et une Selenium Grid avec Docker Compose, Jenkins configuré « as code ».
- Relier GitLab CI/CD à Jenkins : un push sur gitlab.com lance le pipeline Jenkins, et GitLab affiche son résultat.
- Analyser le pipeline d'Embarq (étude de cas) : ce qu'il vérifie, ce qu'il ne vérifie pas, combien de temps il prend.

## Point de départ

La machine a été préparée avec le [guide d'installation](../../guide-installation-lab.md). Le dépôt est déjà dans `~/iroise` :

```bash
cd ~/iroise && git fetch
git checkout tp-00-pipeline
```

Si votre terminal affiche des logs à chaque commande, appliquez d'abord la section « Préparer le terminal » du guide.

## Étape 1 – Faire connaissance avec Embarq (10 min)

```bash
cd ~/iroise/embarq
ls
ls src/main/java/fr/iroise/embarq/*
ls src/test/java/fr/iroise/embarq/*
```

Lancez les tests unitaires :

```bash
mvn -B test
```

Construisez l'application et lancez-la dans l'environnement « dev » :

```bash
mvn -B -q package -DskipTests
docker build -t embarq:local .
bash deploy/deployer.sh dev
```

Ouvrez http://localhost:8090 dans Firefox, et réservez une traversée pour 2 adultes et 1 enfant.

**Question 1.** Combien de tests unitaires compte Embarq ? Quelles classes testent-ils ?

**Question 2.** Combien de temps faut-il à Tom pour « tester à la main » avant une livraison, d'après l'incident ? Combien de temps a pris `mvn -B test` ?

## Étape 2 – Votre projet sur gitlab.com (10 min)

1. Connectez-vous à https://gitlab.com.
2. Créez un jeton d'accès, qui servira de mot de passe pour `git push` :
   - avatar en haut à droite > **Edit profile** ;
   - dans le menu de gauche, **Access** > **Personal access tokens** ;
   - **Generate token** > **Legacy token** (un « Fine-grained token » demande de choisir un projet, qui n'existe pas encore) ;
   - **Token name** : `lab` ; **Expiration date** : après la fin de la formation ;
   - cochez seulement la portée **write_repository** ;
   - **Generate token**, puis copiez le jeton tout de suite et gardez-le dans un fichier : il ne s'affiche qu'une fois.
3. Créez un projet : **New project** > **Create blank project**. Nom `embarq`, visibilité **Public**, **décochez** « Initialize repository with a README ».

Le projet doit être public : Jenkins le clone sans mot de passe.

Sur la page du projet, cliquez sur **Code** et copiez l'adresse **Clone with HTTPS**. Elle a la forme `https://gitlab.com/<votre-espace>/embarq.git` : une barre oblique après `gitlab.com`, pas deux-points. Vous en aurez besoin deux fois : ici, et dans `lab/.env` à l'étape 3.

Ajoutez gitlab.com comme second dépôt distant, avec cette adresse, puis poussez la branche du TP :

```bash
cd ~/iroise
git config --global user.name "Prénom Nom"
git config --global user.email "vous@exemple.fr"
git config --global pull.rebase false
git config --global credential.helper 'cache --timeout=36000'
git remote add gitlab https://gitlab.com/<votre-espace>/embarq.git
git push gitlab tp-00-pipeline
```

Les deux premières lignes donnent à Git votre nom et votre e-mail, inscrits dans chaque commit (mettez l'e-mail de votre compte gitlab.com) : sans elles, Git et VS Code refusent de commiter. La troisième dit à `git pull` de fusionner les corrections du formateur avec votre travail. La quatrième garde le jeton en mémoire pour la journée.

Vous avez maintenant deux dépôts distants : `origin`, le dépôt GitHub du cours (en lecture seule pour vous), et `gitlab`, votre projet. Publiez toujours votre travail dans le terminal, avec `git push gitlab HEAD`. N'utilisez pas le bouton **Sync** de VS Code : il pousse vers `origin`, et échoue.

Git demande un nom d'utilisateur (votre compte gitlab.com) et un mot de passe : collez le **jeton**.

Dans GitLab, ouvrez **Build** > **Pipelines** : un pipeline est apparu, bloqué en `pending`. Il attend un runner : on l'installe à l'étape 4.

## Étape 3 – Le lab « as code » (15 min)

Ouvrez les trois fichiers du lab et lisez-les :

```bash
cd ~/iroise/lab
cat docker-compose.yml
cat jenkins/Dockerfile jenkins/plugins.txt
cat jenkins/casc.yaml
```

Complétez le fichier `.env`, créé pendant la préparation de la machine :

```bash
getent group docker | cut -d: -f3        # le numéro du groupe docker
nano .env
```

| Variable | Valeur |
|----------|--------|
| `JENKINS_ADMIN_PASSWORD` | un mot de passe de votre choix |
| `GITLAB_PROJET` | l'adresse **Clone with HTTPS** du projet, `https://gitlab.com/<votre-espace>/embarq.git` |
| `DOCKER_GID` | le numéro affiché par la commande `getent` |

Démarrez le lab et attendez Jenkins :

```bash
docker compose up -d --build
until curl -s -o /dev/null -w '%{http_code}' http://localhost:8080/login | grep -q 200; do sleep 3; done; echo "Jenkins est prêt"
docker compose ps
```

Ouvrez http://localhost:8080 et connectez-vous avec `admin` et votre mot de passe. Le job **embarq** existe déjà.

**Question 3.** Personne n'a cliqué dans Jenkins pour créer ce job, ni pour installer les plugins. Où sont-ils décrits ? Que se passe-t-il si vous supprimez le conteneur Jenkins et le recréez ?

**Question 4.** Comment Jenkins sait-il quel dépôt cloner et quel fichier lire pour connaître les étapes du pipeline ?

### Le jeton d'API Jenkins

GitLab va appeler Jenkins : il lui faut un jeton **Jenkins**. Ne le confondez pas avec le jeton GitLab de l'étape 2.

| Jeton | Créé dans | Sert à | Où il va |
|-------|-----------|--------|----------|
| Jeton d'accès GitLab (étape 2) | gitlab.com | `git push` vers gitlab.com | Le mot de passe demandé par `git push` |
| **Jeton d'API Jenkins** (ici) | Jenkins, http://localhost:8080 | Laisser GitLab lancer le job Jenkins | La variable GitLab `JENKINS_TOKEN` (étape 4) |

1. Dans Jenkins, cliquez sur **admin**, en haut à droite.
2. Dans le menu de gauche, cliquez sur **Security**.
3. Section **API Token** : **Add new Token**, nom `gitlab`, puis **Generate**.
4. Copiez le jeton affiché et gardez-le dans un fichier : il ne s'affiche qu'une fois.

Vérifiez-le tout de suite, avant de le donner à GitLab :

```bash
curl -s -o /dev/null -w "%{http_code}" -u admin:<jeton-api-jenkins> http://localhost:8080/api/json; echo
```

`200` : le jeton est bon. `401` : il est faux (ou c'est le mot de passe qui a été copié) ; créez-en un autre.

## Étape 4 – GitLab déclenche Jenkins (15 min)

**Les variables.** Dans votre projet GitLab : **Settings** > **CI/CD** > **Variables** > **Add variable**, deux fois :

| Clé | Valeur | Options |
|-----|--------|---------|
| `JENKINS_USER` | `admin` | décochez **Protect variable** |
| `JENKINS_TOKEN` | le **jeton d'API Jenkins** de l'étape 3 (ni le jeton GitLab, ni le mot de passe de `.env`) | visibilité **Masked**, décochez **Protect variable** |

Une variable protégée n'est transmise qu'aux branches protégées. Les branches des TP ne le sont pas : il faut décocher **Protect variable**.

**Le runner.** **Settings** > **CI/CD** > **Runners** > **Create project runner** :

- **Tags** : `embarq` ;
- **Create runner**, puis copiez la commande de l'étape 1 proposée par GitLab (`gitlab-runner register --url https://gitlab.com --token glrt-…`).

Lancez-la **avec `sudo`** : la configuration va alors dans `/etc/gitlab-runner/config.toml`, celle que lit le service du runner. Sans `sudo`, elle irait dans votre dossier personnel, et le service ne verrait pas votre runner. Répondez :

```bash
sudo gitlab-runner register --url https://gitlab.com --token glrt-…
```

| Question du runner | Réponse |
|--------------------|---------|
| GitLab instance URL | Entrée (garde https://gitlab.com) |
| name for the runner | `lab-<votre-prénom>` |
| executor | `shell` |

```bash
sudo gitlab-runner verify
sudo gitlab-runner status
```

`status` doit afficher `Service is running` : le runner tourne en service, en arrière-plan. Ne lancez pas `gitlab-runner run` dans le terminal.

Dans GitLab, le runner apparaît en vert. Retournez dans **Build** > **Pipelines** : le pipeline en attente démarre. Ouvrez le job **jenkins** : sa console recopie celle de Jenkins. Ouvrez aussi le job dans Jenkins (http://localhost:8080/job/embarq/) et regardez les étapes s'enchaîner.

**Question 5.** Lisez `.gitlab-ci.yml` et `embarq/ci/declencher-jenkins.sh`. Qui exécute le job GitLab ? Qui exécute la compilation et les tests ? Comment GitLab connaît-il le résultat de Jenkins ?

Si le pipeline échoue sur `GenerateurReferenceTest`, relancez-le (**Retry** dans GitLab) et notez-le : on en reparle au TP 03.

## Étape 5 – Étude de cas : analyse du pipeline d'Embarq (10 min)

Ouvrez `embarq/Jenkinsfile` et la vue des étapes du job dans Jenkins (**Stages**).

**Question 6.** Complétez le tableau :

| Étape | Commande | Ce qu'elle vérifie | Durée |
|-------|----------|--------------------|-------|
| Compilation | | | |
| Tests unitaires | | | |
| Paquet | | | |

**Question 7.** Les bugs de l'été dernier (le surbooking du 14 juillet, la réservation cassée tout le week-end) auraient-ils été arrêtés par ce pipeline ? Qu'est-ce qui manque ?

Provoquez un échec. Dans `embarq/src/test/java/fr/iroise/embarq/tarif/TarifServiceTest.java`, remplacez `"36.00"` par `"35.00"`, puis :

```bash
cd ~/iroise
git commit -am "Test : tarif adulte à 35 €"
git push gitlab tp-00-pipeline
```

**Question 8.** Où voyez-vous l'échec, et combien de temps après le push ? Le jar a-t-il été construit et archivé ?

Remettez la bonne valeur, poussez, et vérifiez que le pipeline repasse au vert :

```bash
git revert --no-edit HEAD
git push gitlab tp-00-pipeline
```

## Questions de fin

1. Citez trois fichiers de ce dépôt qui relèvent de l'« infrastructure as code ».
2. Quelle différence faites-vous entre intégration continue, livraison continue et déploiement continu ? Où en est Embarq ?
3. Le pipeline teste-t-il l'application telle qu'elle tournera en production ?

## Bonus

- Lancez le job directement depuis Jenkins (**Build with Parameters**), sur la branche `tp-00-pipeline`. Quelle différence avec un lancement par GitLab ?
- Relancez le pipeline 5 fois depuis GitLab (**Build** > **Pipelines** > **Run pipeline**). Est-il toujours vert ?
- Ajoutez au `Jenkinsfile` une option qui coupe le build au bout de 10 minutes au lieu de 30.

## À garder pour la suite

Laissez tourner le lab (`docker compose ps` dans `~/iroise/lab`) et l'environnement « dev » : les TP suivants s'en servent. Au début de chaque TP, après le `git checkout`, poussez la nouvelle branche sur gitlab.com (`git push gitlab HEAD`) : le pipeline construit le point de départ du TP.
