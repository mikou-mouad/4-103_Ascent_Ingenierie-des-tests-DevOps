# TP 07 – Déploiement continu avec des tests à chaque étape

**Durée :** 1 h (14h45 – 15h45) · **Branche :** `tp-07-deploiement` · **Correction :** `solutions/07-deploiement/`

## L'incident

> J-7 avant l'ouverture de la saison. Maëlle a encore en tête le vendredi noir de l'été dernier : un jar copié à la main sur le serveur, une version que personne ne savait nommer, un week-end sans réservation.
>
> **Maëlle :** « Je veux une préproduction, et un bouton. Quand j'appuie, ça part en production. Et si ça casse, on revient en arrière tout seul. »
> **Tom :** « Et si ça casse le samedi matin ? »
> **Inès :** « Alors les tests l'auront vu avant les passagers. »

## Objectifs

- Écrire des smoke tests : quelques vérifications rapides de l'application déployée, dont sa version.
- Prolonger le pipeline : préproduction et smoke tests, approbation humaine, production et smoke tests.
- Revenir automatiquement à la version précédente si la production échoue (rollback).
- Rejouer le « vendredi noir » : une erreur de configuration propre à la production.

## Point de départ

```bash
cd ~/iroise && git fetch
git stash -u
git checkout tp-07-deploiement
git push gitlab HEAD
```

Les environnements de la machine :

| Environnement | Adresse depuis la machine | Adresse depuis Jenkins | Données |
|---------------|---------------------------|------------------------|---------|
| e2e | http://localhost:8089 | `http://embarq-e2e:8080` | neuves à chaque build |
| preprod | http://localhost:8081 | `http://embarq-preprod:8080` | conservées |
| prod | http://localhost:8082 | `http://embarq-prod:8080` | conservées |

Chaque environnement a son profil Spring (`SPRING_PROFILES_ACTIVE`, dans `embarq/docker-compose.yml`) : `application-prod.properties`, s'il existe, ne s'applique qu'en production.

## Étape 1 – Les smoke tests (10 min)

Un smoke test vérifie en quelques secondes que l'application déployée « respire » : elle répond, c'est la bonne version, les fonctions essentielles marchent. Il ne remplace pas les autres tests, il vérifie le **déploiement**.

```bash
cp ~/iroise/tp/07-deploiement/smoke-test.sh ~/iroise/embarq/deploy/
```

Complétez les deux `TODO`, puis essayez le script sur l'environnement « dev » (sa version est `local`) :

```bash
cd ~/iroise/embarq
bash deploy/smoke-test.sh http://localhost:8090 local
bash deploy/smoke-test.sh http://localhost:8090 1.0.99
```

La seconde commande doit échouer sur la version.

**Question 1.** Pourquoi vérifier la version déployée ? Quel incident de l'été dernier ce contrôle aurait-il évité ?

## Étape 2 – Préproduction, approbation, production (15 min)

Dans `embarq/Jenkinsfile`, ajoutez trois étapes après « Tests end-to-end » :

```groovy
stage('Préproduction') {
    steps {
        dir('embarq') {
            sh 'bash deploy/deployer.sh preprod $VERSION'
            sh 'bash deploy/smoke-test.sh http://embarq-preprod:8080 $VERSION'
        }
    }
}

stage('Approbation') {
    steps {
        // Livraison continue : la mise en production est une décision humaine, prise dans Jenkins
        timeout(time: 15, unit: 'MINUTES') {
            input message: "Mettre Embarq ${env.VERSION} en production ?", ok: 'Déployer en production'
        }
    }
}

stage('Production') {
    steps {
        dir('embarq') {
            script {
                // La version en production avant ce déploiement : celle qu'on remet si ça tourne mal
                def precedente = sh(returnStdout: true, script:
                        "docker inspect --format '{{.Config.Image}}' embarq-prod-app-1 2>/dev/null | cut -d: -f2 || true").trim()
                try {
                    sh 'bash deploy/deployer.sh prod $VERSION'
                    sh 'bash deploy/smoke-test.sh http://embarq-prod:8080 $VERSION'
                } catch (erreur) {
                    if (precedente) {
                        echo "Échec en production : retour à la version ${precedente}"
                        sh "bash deploy/deployer.sh prod ${precedente}"
                        sh "bash deploy/smoke-test.sh http://embarq-prod:8080 ${precedente}"
                    }
                    error "Mise en production de ${env.VERSION} annulée. Production : ${precedente ?: 'aucune version'}"
                }
            }
        }
    }
}
```

```bash
cd ~/iroise
git add embarq
git commit -m "Pipeline de déploiement : préprod, approbation, production, rollback"
git push gitlab HEAD
```

Suivez le build. Quand il s'arrête sur **Approbation**, ouvrez la préproduction (http://localhost:8081) : la version est en bas de la page. Puis, dans Jenkins, cliquez sur l'étape en pause et sur **Déployer en production**. Ouvrez la production (http://localhost:8082).

**Question 2.** Pendant l'attente de l'approbation, que montre le job GitLab ? Qui peut approuver ?

**Question 3.** Combien de fois l'image `embarq:1.0.<build>` a-t-elle été construite ? Dans combien d'environnements a-t-elle tourné ? Pourquoi est-ce important ?

## Étape 3 – Le vendredi noir, rejoué (20 min)

Tom prépare l'ouverture de la saison : plus de connexions à la base en production. Ajoutez son fichier :

```bash
cp ~/iroise/tp/07-deploiement/application-prod.properties ~/iroise/embarq/src/main/resources/
cd ~/iroise
git add embarq
git commit -m "Production : plus de connexions à la base pour l'ouverture"
git push gitlab HEAD
```

Suivez le build jusqu'au bout, et approuvez la mise en production.

**Question 4.** Toutes les étapes de test passent. Pourquoi ? À quelle étape le build échoue-t-il, et que dit la console ? (Indice : `docker logs embarq-prod-app-1`.)

**Question 5.** Quelle version tourne en production à la fin du build ? Vérifiez sur http://localhost:8082/actuator/info. Les passagers ont-ils été privés de réservation ?

Corrigez `application-prod.properties`, poussez, approuvez : la nouvelle version arrive en production.

**Question 6.** L'erreur n'a été vue qu'en production. Que faudrait-il changer pour la voir avant ?

## Étape 4 – Les indicateurs de livraison (5 min)

```bash
cd ~/iroise/embarq
JENKINS_USER=admin JENKINS_TOKEN=<votre-jeton> bash ci/indicateurs.sh
```

**Question 7.** Mettez à jour dans `docs/indicateurs.md` la fréquence de déploiement, le taux d'échec des changements et le temps de rétablissement, avec les chiffres de ce TP.

## Questions de fin

1. Livraison continue ou déploiement continu : où en est Embarq ? Que faudrait-il pour passer à l'autre ?
2. Citez deux autres stratégies de déploiement qui limitent le risque d'une mise en production.
3. Quels tests tournent dans le pipeline avant la production, lesquels après ? Lesquels ajouteriez-vous en production, en continu ?

## Bonus

- Ajoutez en préproduction le test de charge du TP 02, avec ses seuils : `docker run --rm -i --network lab grafana/k6:1.8.2 run -e BASE_URL=http://embarq-preprod:8080 - < perf/charge.js`.
- Écrivez un test qui démarre Embarq avec le profil `prod` (`@SpringBootTest` et `@ActiveProfiles("prod")`, avec PostgreSQL dans Testcontainers comme `ApiIT`) : la coquille de Tom aurait été vue dès les tests d'intégration.
- Ne proposez l'approbation que sur la branche `main` (`when { branch 'main' }` ne marche pas ici, car le job n'est pas multibranche : utilisez `when { expression { params.BRANCHE == 'main' } }`).
