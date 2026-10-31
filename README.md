# Ingénierie des tests DevOps : supports de formation

Formation Ascent 4-103 de 2 jours (12 h, 75 % de pratique), racontée à travers l'histoire d'**Iroise Traversées**, la compagnie maritime qui relie Brest et Le Conquet aux îles de Molène et d'Ouessant. Avant l'ouverture de la saison, le 1er avril, son application de réservation **Embarq** passe des tests à la main à un pipeline GitLab CI/CD et Jenkins qui teste, documente et déploie.

- [programme-2-jours.md](programme-2-jours.md) : le programme détaillé et le fil rouge
- [guide-installation-lab.md](guide-installation-lab.md) : la préparation de la machine d'un participant
- `embarq/` : l'application Embarq (Java 21, Spring Boot, PostgreSQL), son `Jenkinsfile` et ses scripts de déploiement
- `lab/` : le lab décrit comme du code (Jenkins configuré « as code », Selenium Grid)
- `.gitlab-ci.yml` : le pipeline GitLab, qui déclenche Jenkins
- `presentation/` : les slides en HTML, sur la branche `main` (ouvrir `presentation/index.html`)
- `tp/` : les énoncés des TP (un dossier par TP, ajouté dans chaque branche)

## Les branches des TP

Chaque branche part de la précédente. La branche `tp-NN` contient :

- Embarq et son pipeline dans l'état attendu au début du TP NN ;
- l'énoncé du TP (`tp/NN-*/README.md`) et les fichiers à compléter ;
- sa correction (`solutions/NN-*/`), ainsi que celles des TP précédents.

Elle ne contient rien du TP suivant : on peut consulter la correction sans découvrir la suite.

| Branche | TP |
|---------|----|
| `tp-00-pipeline` | Le lab « as code », GitLab CI/CD qui déclenche Jenkins, analyse du pipeline |
| `tp-01-strategie` | Stratégie de test, tests unitaires JUnit 5 |
| `tp-02-integration` | Tests d'intégration avec Testcontainers, test de charge k6 |
| `tp-03-indicateurs` | Couverture JaCoCo, quality gate, test instable, indicateurs qualité |
| `tp-04-bdd` | Shift-left et BDD avec Cucumber |
| `tp-05-selenium` | Tests end-to-end avec Selenium WebDriver et Selenium Grid |
| `tp-06-documentation` | Documentation et maintenance des tests |
| `tp-07-deploiement` | Déploiement continu : préprod, smoke tests, approbation, production, rollback |
| `solution-finale` | État final d'Embarq et de son pipeline |

Sur la machine du lab :

```bash
git clone https://github.com/mikou-mouad/4-103_Ascent_Ingenierie-des-tests-DevOps.git ~/iroise
cd ~/iroise
git checkout tp-00-pipeline
```

Au début de chaque TP suivant :

```bash
cd ~/iroise && git fetch
git stash -u                  # met de côté votre travail du TP précédent
git checkout tp-03-indicateurs
git push gitlab HEAD          # le pipeline GitLab → Jenkins construit le point de départ du TP
```

## Essayer Embarq sans le lab

Avec Java 21, Maven et Docker :

```bash
cd embarq
mvn -B package -DskipTests
docker network create lab
docker build -t embarq:local .
bash deploy/deployer.sh dev
```

Puis http://localhost:8090.
