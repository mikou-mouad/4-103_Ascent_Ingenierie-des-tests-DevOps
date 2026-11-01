# TP 06 – Documenter et maintenir les tests

**Durée :** 1 h 05 (13h15 – 14h20) · **Branche :** `tp-06-documentation` · **Correction :** `solutions/06-documentation/`

## L'incident

> Inès est partie en congés une semaine. L'agence Brume a livré la nouvelle charte graphique d'Embarq, et Tom l'a intégrée lundi. Depuis, le pipeline est rouge : tous les tests Selenium échouent.
>
> **Tom :** « Je n'ai pas touché au code, seulement aux pages. Et je ne sais pas ce que vérifient ces tests, ni comment les lancer. »
> **Maëlle :** « Si Inès part, les tests partent avec elle ? »

## Objectifs

- Diagnostiquer des tests end-to-end cassés par une évolution de l'application.
- Rendre les tests robustes aux changements de pages : des sélecteurs `data-testid`, utilisés uniquement dans les Page Objects.
- Documenter les cas de test (exigence, préconditions, étapes, résultat attendu, automatisation).
- Rendre les rapports lisibles : des noms de tests en français, reliés aux cas de test.
- Documenter l'exécution des tests : comment les lancer, sur quel environnement, où lire les résultats.

## Point de départ

```bash
cd ~/iroise && git fetch
git stash -u
git checkout tp-06-documentation
git push -u gitlab HEAD     # -u : la branche suit votre projet GitLab, pas le dépôt du cours
```

> **N'oubliez pas le `-u`** sur ce premier push. Sans lui, la nouvelle branche suit le dépôt GitHub du cours, et les boutons **Sync** et **Push** de VS Code partent au mauvais endroit. Vérifiez avec `git branch -vv` : la branche courante doit afficher `[gitlab/tp-06-documentation]`.

Le pipeline de ce push est rouge : c'est le point de départ.

## Étape 1 – Le diagnostic (15 min)

Dans Jenkins, ouvrez le build en échec, puis **Tests** : lisez les erreurs des tests end-to-end.

Retrouvez le changement qui les a cassés :

```bash
cd ~/iroise
git log --oneline -3 -- embarq/src/main/resources/templates
CHARTE=$(git log -1 --format=%h -- embarq/src/main/resources/templates)
git show --stat $CHARTE
git show $CHARTE -- embarq/src/main/resources/templates/reserver.html | head -60
```

Redéployez votre environnement **dev** avec la nouvelle charte, regardez la page (http://localhost:8090), puis relancez les tests en local :

```bash
cd ~/iroise/embarq
mvn -B -q package -DskipTests
docker build -t embarq:local .
bash deploy/deployer.sh dev
mvn -B -Pe2e verify -Dapp.url=http://embarq-dev:8080
```

**Question 1.** Quelle exception lèvent les tests ? Sur quels sélecteurs ? Quels changements de l'agence les ont cassés ?

**Question 2.** Combien de fichiers de tests faudrait-il modifier si on corrigeait les sélecteurs un par un ? Lesquels ? Pourquoi aucun fichier `ReservationE2E` n'est-il concerné ?

## Étape 2 – Des sélecteurs qui résistent aux changements (20 min)

Un attribut `data-testid` est un **contrat** entre les pages et les tests : il ne sert ni au style, ni au JavaScript, et une refonte graphique doit le conserver.

**1. Les pages.** Ajoutez un attribut `data-testid` aux éléments utilisés par les tests :

| Page | Élément | `data-testid` |
|------|---------|---------------|
| `index.html` | chaque carte de traversée (`<article>`) | `traversee` |
| `index.html` | le nom du navire dans la carte | `navire` |
| `index.html` | le lien « Réserver ma place » | `reserver` |
| `reserver.html` | les champs | `nom`, `email`, `adultes`, `enfants`, `velos`, `resident` |
| `reserver.html` | le bouton « Confirmer ma réservation » | `valider` |
| `reserver.html` | chaque message d'erreur (`.alerte` et `.aide-erreur`) | `erreur` |
| `confirmation.html` | la référence, le statut, le total | `reference`, `statut`, `montant` |

Par exemple :

```html
<input id="passager-nom" data-testid="nom" type="text" th:field="*{nom}" autocomplete="family-name">
```

**2. Les Page Objects.** Créez `embarq/src/test/java/fr/iroise/embarq/e2e/TestId.java` :

```java
package fr.iroise.embarq.e2e;

import org.openqa.selenium.By;

/** Les sélecteurs des tests end-to-end : l'attribut data-testid, posé dans les pages pour les tests. */
final class TestId {

    private TestId() {
    }

    static By testId(String valeur) {
        return By.cssSelector("[data-testid='" + valeur + "']");
    }
}
```

Dans `PageAccueil`, `PageReservation` et `PageConfirmation`, remplacez tous les `By.id(…)`, `By.cssSelector(…)` et `By.xpath(…)` par `testId(…)` (avec `import static fr.iroise.embarq.e2e.TestId.testId;`). Pour `reserverSur(navire)`, parcourez les cartes `traversee` et gardez celle dont le `navire` a le bon texte.

Vos pages ont changé : il faut reconstruire le jar, **puis** l'image, puis redéployer. Le `Dockerfile` ne compile rien, il copie `target/embarq.jar` tel qu'il est : une image construite avant `mvn package` contiendrait encore les anciennes pages.

```bash
cd ~/iroise/embarq
mvn -B -q package -DskipTests
docker build -t embarq:local .
bash deploy/deployer.sh dev
curl -s http://localhost:8090/ | grep -c 'data-testid="reserver"'
```

Le `grep` doit afficher un nombre supérieur à 0 (un lien « Réserver » par traversée). S'il affiche `0`, l'application déployée n'a pas vos `data-testid` : vérifiez les pages, puis recommencez dans cet ordre. Relancez ensuite les tests : tout doit être vert.

```bash
mvn -B -Pe2e verify -Dapp.url=http://embarq-dev:8080
```

**Question 3.** L'agence change à nouveau la couleur des boutons, remplace les cartes par un tableau et renomme les `id`. Que faut-il modifier dans les tests ?

## Étape 3 – Documenter les cas de test (15 min)

```bash
cp ~/iroise/tp/06-documentation/cas-de-test.md ~/iroise/embarq/docs/
```

Complétez l'index et les fiches des trois tests end-to-end. Chaque cas a un identifiant (`CT-E2E-01`…).

Reliez ensuite les tests aux cas : dans `ReservationE2E`, ajoutez un `@DisplayName` à la classe et à chaque test, qui commence par l'identifiant du cas :

```java
@Test
@DisplayName("CT-E2E-01 · Une famille réserve une traversée et reçoit sa référence")
void un_passager_reserve_une_traversee_en_famille() {
```

Pour que Jenkins affiche ces noms, ajoutez dans `embarq/pom.xml`, dans la `<configuration>` de **maven-surefire-plugin** et dans celle de **maven-failsafe-plugin** :

```xml
<!-- Rapports XML avec les noms lisibles (@DisplayName) : Jenkins affiche des phrases -->
<statelessTestsetReporter implementation="org.apache.maven.plugin.surefire.extensions.junit5.JUnit5Xml30StatelessReporter">
  <usePhrasedTestSuiteClassName>true</usePhrasedTestSuiteClassName>
  <usePhrasedTestCaseClassName>true</usePhrasedTestCaseClassName>
  <usePhrasedTestCaseMethodName>true</usePhrasedTestCaseMethodName>
</statelessTestsetReporter>
```

## Étape 4 – Documenter l'exécution (10 min)

Écrivez `embarq/docs/tests.md`, pour Tom pendant les congés d'Inès. Une page, pas plus :

- les niveaux de tests, où ils sont, la commande qui les lance, leur durée ;
- comment lancer les tests end-to-end sur un environnement donné (`-Dapp.url`) ;
- où lire les résultats, en local et dans Jenkins ;
- que faire quand un test échoue ;
- les règles de maintenance que vous venez d'appliquer.

```bash
cd ~/iroise
git add embarq
git commit -m "Tests end-to-end sur data-testid, cas de test et guide d'exécution documentés"
git push gitlab HEAD
```

Quand le pipeline est vert, regardez ce que votre documentation est devenue :

**Dans Jenkins : les noms des tests.**

1. Ouvrez http://localhost:8080/job/embarq/, puis le dernier build (en bas à gauche, **Builds**).
2. Dans le menu de gauche du build, cliquez sur **Tests**.
3. Dans la liste, ouvrez **Parcours de réservation (navigateur)** : les tests end-to-end s'appellent maintenant `CT-E2E-01 · Une famille réserve une traversée et reçoit sa référence`, et non plus `un_passager_reserve_une_traversee_en_famille`. Les tests sans `@DisplayName` gardent leur nom de méthode.

Avec l'identifiant `CT-E2E-01`, on passe du rapport de Jenkins à la fiche du cas de test, et inversement.

**Dans GitLab : les documents.**

1. Ouvrez votre projet `embarq` sur gitlab.com, puis **Code** > **Repository** dans le menu de gauche.
2. Dans la liste des branches, en haut, choisissez `tp-06-documentation`.
3. Ouvrez le dossier `embarq`, puis `docs`, puis `cas-de-test.md` et `tests.md`.

GitLab affiche le Markdown mis en forme : titres, tableaux, liens entre les deux documents. Le bouton **Display source** montre le texte brut, celui que vous avez écrit. La documentation vit avec le code : chaque branche a la sienne, et elle se relit dans les mêmes demandes de fusion.

**Question 4.** Qu'est-ce qu'une « documentation vivante » ? Lesquels de vos documents le sont, lesquels ne le sont pas ?

**Question 5.** `-Dapp.url` permet de lancer les mêmes tests sur l'environnement dev, e2e ou en préproduction. Quel problème de données peut-on rencontrer en préproduction ?

## Questions de fin

1. Citez trois bonnes pratiques pour qu'un test end-to-end survive aux évolutions de l'application.
2. Où ranger la documentation des tests : un wiki, un tableur, ou le dépôt ? Pourquoi ?
3. Qui doit mettre à jour les `data-testid` quand une page change ?

## Bonus

- Ajoutez des `@DisplayName` aux tests unitaires et d'intégration, et des identifiants `CT-…` dans l'index.
- Ajoutez au `Jenkinsfile` l'archivage des captures d'écran des tests en échec (bonus du TP 05).
- Écrivez un test qui échoue si une page perd un `data-testid` utilisé par les tests.
