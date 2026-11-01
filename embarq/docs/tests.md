# Lancer et lire les tests d'Embarq

Pour un nouvel arrivant, ou pour l'équipe quand Inès est en congés.

## Les niveaux de tests

| Niveau | Où | Lancés par | Commande locale | Durée |
|--------|----|------------|-----------------|-------|
| Unitaires | `src/test/java/**/*Test.java` | surefire | `mvn -B test` | quelques secondes |
| Scénarios Cucumber | `src/test/resources/features/*.feature`, étapes dans `bdd/` | surefire, avec les unitaires | `mvn -B test -Dtest=CucumberTest` | quelques secondes |
| Intégration | `src/test/java/**/*IT.java` | failsafe, PostgreSQL dans Testcontainers | `mvn -B verify` | 30 s à 1 min |
| End-to-end | `src/test/java/fr/iroise/embarq/e2e/*E2E.java` | failsafe, profil `e2e`, Selenium Grid | `mvn -B -Pe2e verify -Dapp.url=<url>` | 10 à 30 s |
| Performance | `perf/charge.js` | k6, à la demande | voir ci-dessous | 1 min |

Les cas de test importants sont décrits dans [cas-de-test.md](cas-de-test.md) ; la stratégie dans [strategie-de-test.md](strategie-de-test.md).

## Prérequis

- Java 21, Maven, Docker (sans `sudo`).
- Pour les tests end-to-end : le lab démarré (`cd lab && docker compose up -d`), et Embarq déployé dans un environnement.

## Tests end-to-end : choisir l'environnement

Les tests prennent deux paramètres, l'adresse d'Embarq **vue depuis le navigateur de la Grid**, et l'adresse de la Grid :

| Environnement | Déploiement | `-Dapp.url` |
|---------------|-------------|-------------|
| dev (votre environnement, en local) | `bash deploy/deployer.sh dev` | `http://embarq-dev:8080` |
| e2e (le pipeline, base neuve à chaque build ; ne pas l'utiliser en local) | Jenkins | `http://embarq-e2e:8080` |
| preprod | pipeline (TP 07) | `http://embarq-preprod:8080` |

`-Dselenium.url` vaut `http://localhost:4444` par défaut (depuis la machine) ; Jenkins utilise `http://selenium:4444`.

Pour regarder le navigateur pendant les tests : http://localhost:7900.

## Performance

```bash
docker run --rm -i --network lab grafana/k6:1.8.2 run -e BASE_URL=http://embarq-dev:8080 - < perf/charge.js
```

Seuils : moins de 1 % d'erreurs, 95 % des réponses en moins de 500 ms.

## Où lire les résultats

| Résultat | Localement | Dans Jenkins (page du build) |
|----------|-----------|------------------------------|
| Tests unitaires, Cucumber, intégration, end-to-end | `target/surefire-reports/`, `target/failsafe-reports/` | **Tests** (noms lisibles, avec les identifiants CT-…) |
| Scénarios Cucumber | `target/cucumber/rapport.html` | **Scénarios Cucumber** |
| Couverture | `target/site/jacoco/index.html` | **Coverage** |
| Indicateurs | `bash ci/indicateurs.sh` | évolution sur la page du job |

## Quand un test échoue

1. Lire le message dans Jenkins (**Tests**), puis le cas de test correspondant (identifiant CT-…).
2. Le relancer **en local**, au même niveau. Un test qui passe en local et échoue dans le pipeline : penser aux données, à l'environnement, au temps (attentes).
3. Un test end-to-end en `NoSuchElementException` : une page a changé. Vérifier que l'élément a toujours son attribut `data-testid` ; ne corriger que le Page Object.
4. Ne jamais relancer un test jusqu'à ce qu'il passe sans comprendre : un test instable cache souvent un vrai bug (TP 03).

## Règles de maintenance

- Les tests end-to-end n'utilisent que des sélecteurs `data-testid`, définis dans `e2e/TestId.java`. Une page qui change garde ses `data-testid`.
- Les sélecteurs ne sont écrits que dans les Page Objects, jamais dans les tests.
- Chaque test end-to-end a un `@DisplayName` qui commence par l'identifiant de son cas de test.
- Un test supprimé ou modifié est mis à jour dans [cas-de-test.md](cas-de-test.md) dans le même commit.
