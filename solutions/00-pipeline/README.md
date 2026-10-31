# Correction du TP 00 – Le pipeline d'Embarq

Ce TP ne modifie pas le code d'Embarq : la branche `tp-01-strategie` part du même état.

## Étape 1

**Question 1.** 6 tests unitaires, dans 3 classes :

- `TarifServiceTest` (2 tests) : le calcul du prix ;
- `ReservationServiceTest` (2 tests) : la réservation, avec des dépôts simulés par Mockito ;
- `GenerateurReferenceTest` (2 tests) : le format et l'unicité des références.

Rien ne teste les pages web, l'API, ni les requêtes SQL.

**Question 2.** Inès passe deux jours à recliquer le site. `mvn -B test` prend une dizaine de secondes, dont l'essentiel pour démarrer Maven. C'est l'argument principal de l'automatisation : un retour en secondes, à chaque modification, au lieu de deux jours avant une livraison.

## Étape 3

**Question 3.** Tout est dans `lab/` :

- `jenkins/Dockerfile` : l'image Jenkins, avec Maven, le client Docker et les plugins ;
- `jenkins/plugins.txt` : la liste des plugins, installés à la construction de l'image ;
- `jenkins/casc.yaml` : la configuration (compte administrateur, sécurité, le job `embarq`), appliquée par le plugin *configuration-as-code* à chaque démarrage.

Si on supprime le conteneur et qu'on le recrée, Jenkins revient dans le même état : la configuration est relue depuis `casc.yaml`. L'historique des builds survit, car il est dans le volume `jenkins_home`. Avec `docker compose down -v`, on perd l'historique, mais pas la configuration.

**Question 4.** Le job est un `pipelineJob` dont la définition vient du dépôt (`cpsScm`) : l'adresse est `GITLAB_PROJET` (dans `.env`, transmise au conteneur), la branche est le paramètre `BRANCHE`, et le fichier lu est `embarq/Jenkinsfile`. Le pipeline est donc versionné avec le code : une branche peut changer son propre pipeline.

## Étape 4

**Question 5.**

- Le job GitLab est exécuté par **votre GitLab Runner**, sur votre machine (tag `embarq`, exécuteur `shell`). Il ne fait qu'une chose : lancer `declencher-jenkins.sh`.
- La compilation et les tests sont exécutés par **Jenkins**, qui clone le projet depuis gitlab.com.
- Le script appelle l'API de Jenkins (`buildWithParameters`), attend le numéro du build, recopie sa console, puis lit le résultat (`/api/json`). Il sort en erreur si le résultat n'est pas `SUCCESS` : le job GitLab devient rouge.

Le runner va chercher ses jobs chez gitlab.com (connexion sortante) et appelle Jenkins en local : aucun port de la machine n'est ouvert sur Internet.

## Étape 5

**Question 6.** Les durées dépendent de la machine ; ordre de grandeur :

| Étape | Commande | Ce qu'elle vérifie | Durée |
|-------|----------|--------------------|-------|
| Compilation | `mvn -B compile` | Le code compile | 10 à 20 s |
| Tests unitaires | `mvn -B test` | Les 6 tests unitaires passent | 15 à 30 s |
| Paquet | `mvn -B package -DskipTests` | Le jar se construit ; il est archivé dans Jenkins | 10 à 20 s |

**Question 7.** Non :

- **le surbooking** : le bug est dans une requête SQL. Les tests du service remplacent la base par un mock, ils ne l'exécutent jamais (TP 02) ;
- **la réservation cassée** : aucun test ne passe par les pages web (TP 05) ;
- rien ne vérifie l'application **déployée**, ni sa tenue en charge, et le déploiement reste manuel (TP 02 et TP 07).

Le pipeline vérifie seulement que le code compile et que la logique de calcul et de réservation, isolée, fonctionne.

**Question 8.** L'échec apparaît dans GitLab (pipeline rouge, avec la console Jenkins dans le job) et dans Jenkins (étape « Tests unitaires » rouge, rapport de tests avec l'assertion attendue et obtenue), une à deux minutes après le push. L'étape « Paquet » n'est pas exécutée : aucun jar n'est archivé. Un pipeline s'arrête à la première étape en échec.

## Questions de fin

1. `lab/docker-compose.yml`, `lab/jenkins/casc.yaml`, `lab/jenkins/Dockerfile`, `embarq/Jenkinsfile`, `.gitlab-ci.yml`, `embarq/Dockerfile`, `embarq/docker-compose.yml`. Ils décrivent l'environnement et la chaîne de livraison : on les relit, on les versionne, on les rejoue à l'identique.
2. **Intégration continue** : chaque modification est intégrée et vérifiée automatiquement (compilation, tests). **Livraison continue** : chaque version validée est prête à partir en production, le départ restant une décision humaine. **Déploiement continu** : chaque version validée part en production sans intervention. Embarq fait de l'intégration continue, sans les tests d'intégration ni de bout en bout, et rien pour la livraison.
3. Non. Les tests unitaires tournent dans Jenkins, sans base de données, sans l'image Docker, sans navigateur. On le corrige aux TP 02 (vraie base), 05 (vrai navigateur sur l'application déployée) et 07 (préproduction et production).

## Bonus

- Lancé depuis Jenkins, le build fonctionne de la même façon, mais GitLab n'en sait rien : pas de pipeline dans GitLab, pas de lien avec le commit. GitLab reste la porte d'entrée.
- Il peut arriver qu'un pipeline échoue sans aucun changement de code, sur `GenerateurReferenceTest`. C'est un **test instable**, le sujet du TP 03.
- Dans `options` : `timeout(time: 10, unit: 'MINUTES')`.
