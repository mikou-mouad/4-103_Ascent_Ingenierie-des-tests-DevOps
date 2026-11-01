# TP 05 – Tests end-to-end avec Selenium

**Durée :** 1 h 05 (10h55 – 12h00) · **Branche :** `tp-05-selenium` · **Correction :** `solutions/05-selenium/`

## L'incident

> Avant chaque livraison, Inès ouvre Embarq et refait les mêmes gestes : choisir une traversée, remplir le formulaire, vérifier le prix, la référence, les messages d'erreur. Sur trois navigateurs. Deux jours.
>
> **Inès :** « Le week-end où la réservation était cassée, j'avais tout testé le jeudi. Tom a changé le formulaire le vendredi. »
> **Tom :** « Juste un petit changement. »

## Objectifs

- Automatiser le parcours de réservation avec Selenium WebDriver, dans un vrai navigateur.
- Organiser le code des tests avec le patron **Page Object**, et des **attentes explicites**.
- Exécuter les tests sur la Selenium Grid du lab, et les regarder travailler.
- Ajouter l'étape « Tests end-to-end » au pipeline, sur l'image Docker déployée dans un environnement dédié.

## Point de départ

```bash
cd ~/iroise && git fetch
git stash -u
git checkout tp-05-selenium
git push -u gitlab HEAD     # -u : la branche suit votre projet GitLab, pas le dépôt du cours
```

> **N'oubliez pas le `-u`** sur ce premier push. Sans lui, la nouvelle branche suit le dépôt GitHub du cours, et les boutons **Sync** et **Push** de VS Code partent au mauvais endroit. Vérifiez avec `git branch -vv` : la branche courante doit afficher `[gitlab/tp-05-selenium]`.

Ce qui est nouveau dans la branche :

- le `Jenkinsfile` construit l'**image Docker** d'Embarq (`embarq:1.0.<numéro du build>`), puis la déploie dans l'environnement **e2e** (`deploy/deployer.sh e2e`), supprimé à la fin du build : cet environnement est réservé au pipeline ;
- `src/test/java/fr/iroise/embarq/e2e/TestEndToEnd.java` : la base des tests end-to-end, qui ouvre un navigateur Chrome sur la Selenium Grid avant chaque test.

## Étape 1 – Préparer le terrain (10 min)

Déployez l'environnement **dev** sur votre machine, avec la version courante :

```bash
cd ~/iroise/embarq
mvn -B -q package -DskipTests
docker build -t embarq:local .
bash deploy/deployer.sh dev
```

> **L'environnement e2e appartient au pipeline.** Chaque build Jenkins le recrée, puis le supprime à la fin. Si vous l'utilisez en local, le prochain push le fermera sous vos yeux (« Unable to connect »). En local, travaillez sur l'environnement **dev** (http://localhost:8090), qui n'appartient qu'à vous.

Ouvrez deux onglets dans Firefox :

- http://localhost:8090 : Embarq en dev ;
- http://localhost:7900 : le bureau du navigateur de la Selenium Grid (cliquez sur **Connect**). C'est là que vous verrez les tests travailler.

Dans le premier onglet, faites une réservation à la main, puis inspectez la page (clic droit > **Inspecter**) : notez les `id` du lien « Réserver », des champs du formulaire, du bouton, et des éléments de la page de confirmation.

**Question 1.** Le navigateur de la Grid ouvre `http://embarq-dev:8080`, et pas `http://localhost:8090`. Pourquoi ?

## Étape 2 – Les Page Objects (25 min)

Copiez les squelettes dans les tests du projet :

```bash
cp ~/iroise/tp/05-selenium/*.java ~/iroise/embarq/src/test/java/fr/iroise/embarq/e2e/
```

Une classe par page, qui connaît la structure de la page (les sélecteurs) et offre des actions métier (`saisir`, `valider`) :

| Classe | Page |
|--------|------|
| `PageAccueil` | La liste des traversées |
| `PageReservation` | Le formulaire |
| `PageConfirmation` | « Bon voyage ! » |
| `ReservationE2E` | Les tests : ils n'utilisent que les Page Objects, jamais de sélecteur |

Complétez les `TODO` des trois pages. Pour attendre qu'un élément soit visible :

```java
new WebDriverWait(navigateur, Duration.ofSeconds(10))
        .until(ExpectedConditions.visibilityOfElementLocated(By.id("reference")));
```

**Question 2.** Pourquoi attendre explicitement l'élément, plutôt qu'un `Thread.sleep(2000)` ?

## Étape 3 – Les tests (15 min)

Complétez les deux `TODO` de `ReservationE2E`, puis lancez les tests end-to-end, **en regardant l'onglet 7900** :

```bash
cd ~/iroise/embarq
mvn -B -Pe2e verify -Dapp.url=http://embarq-dev:8080
```

Le profil Maven `e2e` ne lance que les classes `*E2E`, avec failsafe, sans les tests unitaires.

**Question 3.** Pourquoi le test des erreurs utilise-t-il 1 adulte, et pas 0 ? Essayez avec 0 et regardez le navigateur.

**Question 4.** Combien de temps prend un test end-to-end, comparé à un test unitaire ? Qu'en concluez-vous sur le nombre de tests end-to-end à écrire ?

## Étape 4 – Dans le pipeline (15 min)

Dans `embarq/Jenkinsfile`, remplacez le commentaire `// TP 05 : l'étape « Tests end-to-end » se place ici` par :

```groovy
stage('Tests end-to-end') {
    steps {
        dir('embarq') {
            // Le navigateur de la Selenium Grid ouvre Embarq déployé dans l'environnement e2e
            sh 'mvn -B -Pe2e verify -Dapp.url=http://embarq-e2e:8080 -Dselenium.url=http://selenium:4444'
        }
    }
    post {
        always {
            junit 'embarq/target/failsafe-reports/TEST-*E2E.xml'
        }
    }
}
```

```bash
cd ~/iroise
git add embarq
git commit -m "Tests end-to-end Selenium du parcours de réservation"
git push gitlab HEAD
```

Pendant le build, gardez l'onglet 7900 ouvert : le pipeline pilote le même navigateur.

**Question 5.** Dans Jenkins, l'application testée est-elle la même que celle qui partira en production ? Qu'est-ce qui le garantit ?

**Question 6.** Que devient l'environnement e2e après le build ? Pourquoi ?

## Questions de fin

1. Qu'est-ce que la Selenium Grid apporte, par rapport à un navigateur sur la machine de Jenkins ?
2. Inès testait sur trois navigateurs. Que faudrait-il changer pour tester aussi sur Firefox ?
3. Le patron Page Object : qu'est-ce qui change dans les tests si le bouton « Valider » change d'`id` ?

## Bonus

- Ajoutez un test : sur le **Molenez** (12 places), une réservation de 13 adultes est refusée avec un message. Ajoutez à `PageAccueil` une méthode `reserverSur(String navire)` (un sélecteur XPath sur la ligne du tableau qui contient le nom du navire).
- Lancez le test du tarif résident de bout en bout : cochez la case `resident` et vérifiez le montant.
- Ajoutez à `TestEndToEnd` une capture d'écran en cas d'échec (`((TakesScreenshot) navigateur).getScreenshotAs(OutputType.FILE)`), et archivez-la dans Jenkins.
