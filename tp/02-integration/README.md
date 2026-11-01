# TP 02 – Tests d'intégration et de performance

**Durée :** 35 min (13h30 – 14h05) · **Branche :** `tp-02-integration` · **Correction :** `solutions/02-integration/`

## L'incident

> Le 14 juillet, le Fromveur II a embarqué 312 passagers pour 280 places. Le lendemain, Tom a relancé tous les tests : verts.
>
> **Tom :** « Le service refuse les réservations quand le navire est plein, il y a un test pour ça. »
> **Inès :** « Ton test dit au service combien de places sont vendues. Qui vérifie que la base le dit aussi ? »

## Objectifs

- Écrire un test d'intégration qui exécute les vraies requêtes sur une vraie base PostgreSQL, démarrée par Testcontainers.
- Trouver et corriger le bug du surbooking.
- Ajouter l'étape « Tests d'intégration » au pipeline.
- Mesurer la tenue en charge d'Embarq avec k6.

## Point de départ

```bash
cd ~/iroise && git fetch
git stash -u
git checkout tp-02-integration
git push -u gitlab HEAD     # -u : la branche suit votre projet GitLab, pas le dépôt du cours
```

> **N'oubliez pas le `-u`** sur ce premier push. Sans lui, la nouvelle branche suit le dépôt GitHub du cours, et les boutons **Sync** et **Push** de VS Code partent au mauvais endroit. Vérifiez avec `git branch -vv` : la branche courante doit afficher `[gitlab/tp-02-integration]`.

## Étape 1 – Le test d'intégration (15 min)

Copiez le squelette dans les tests du projet :

```bash
cp ~/iroise/tp/02-integration/ReservationRepositoryIT.java \
   ~/iroise/embarq/src/test/java/fr/iroise/embarq/reservation/
```

Ouvrez-le et lisez les annotations :

- `@DataJpaTest` : démarre seulement la couche base de données de Spring (entités, dépôts, Flyway) ;
- `@Testcontainers`, `@Container` : démarrent un PostgreSQL 17 dans Docker pour la durée des tests ;
- `@ServiceConnection` : branche Spring sur ce PostgreSQL, sans configuration.

Complétez les deux `TODO`, puis lancez les tests d'intégration :

```bash
cd ~/iroise/embarq
mvn -B verify
```

Les tests dont le nom finit par `IT` sont lancés par le plugin failsafe, après les tests unitaires. Pendant qu'ils tournent, `docker ps` montre le conteneur PostgreSQL.

**Question 1.** Quels tests échouent, avec quels messages ?

**Question 2.** Lisez `placesReservees` dans `ReservationRepository.java`. Expliquez les 312 passagers du 14 juillet.

## Étape 2 – Corriger le surbooking (5 min)

Corrigez la requête de `placesReservees`. Relancez `mvn -B verify` jusqu'à ce que tout soit vert.

**Question 3.** Les tests unitaires de `ReservationServiceTest` ont-ils changé de résultat avant et après la correction ? Pourquoi ?

## Étape 3 – L'étape « Tests d'intégration » du pipeline (5 min)

Dans `embarq/Jenkinsfile`, ajoutez une étape entre « Tests unitaires » et « Paquet » :

```groovy
stage('Tests d\'intégration') {
    steps {
        dir('embarq') {
            // Les tests unitaires sont déjà passés : on ne lance que les *IT (failsafe)
            sh 'mvn -B verify -Dtests.unitaires.ignores=true'
        }
    }
    post {
        always {
            junit 'embarq/target/failsafe-reports/TEST-*IT.xml'
        }
    }
}
```

```bash
cd ~/iroise
git add embarq
git commit -m "Surbooking : les enfants et les annulations comptent, test d'intégration"
git push gitlab HEAD
```

Le motif `TEST-*IT.xml` n'enregistre que les rapports des tests d'intégration : plus tard, les tests end-to-end écriront aussi dans `failsafe-reports/`, et chaque étape doit n'enregistrer que ses propres tests.

Dans Jenkins, la vue des étapes montre la nouvelle étape, et le rapport de tests compte les tests d'intégration.

**Question 4.** Jenkins tourne dans un conteneur. Comment peut-il démarrer un conteneur PostgreSQL pour les tests ? (Indice : `lab/docker-compose.yml`.)

## Étape 4 – Le test de charge de l'ouverture (10 min)

Reconstruisez l'image avec la correction, et redéployez l'environnement « dev » :

```bash
cd ~/iroise/embarq
mvn -B -q package -DskipTests
docker build -t embarq:local .
bash deploy/deployer.sh dev
```

Lisez `perf/charge.js`, puis lancez le test (k6 tourne dans un conteneur, sur le réseau du lab) :

```bash
docker run --rm -i --network lab grafana/k6:1.8.2 run -e BASE_URL=http://embarq-dev:8080 - < perf/charge.js
```

**Question 5.** Combien de requêtes par seconde Embarq a-t-il servies ? Quel est le temps de réponse au 95e centile (`p(95)`) ? Les seuils sont-ils tenus ?

**Question 6.** Le 1er avril à 9h, Maëlle attend 300 visiteurs en même temps. Que faudrait-il changer dans le script pour le simuler ? Ce test doit-il tourner à chaque push ? Sur quel environnement ?

## Questions de fin

1. Pourquoi une base H2 en mémoire n'aurait-elle pas suffi pour ce test ?
2. Les tests d'intégration sont plus lents que les tests unitaires. Comment garder un pipeline rapide quand leur nombre augmente ?
3. Un test de performance qui passe sur votre machine prouve-t-il que la production tiendra ?

## Bonus

- Ajoutez un test d'intégration : les places d'une **autre** traversée ne doivent pas compter.
- Montez à 100 visiteurs dans `charge.js` (`target: 100`). Où sont les limites de votre machine ?
- Ajoutez au test de charge une réservation (`http.post` sur `/api/traversees/<id>/reservations`, en JSON). Que se passe-t-il quand le navire est plein ?
