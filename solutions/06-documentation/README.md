# Correction du TP 06 – Documenter et maintenir les tests

Fichiers corrigés, aux mêmes chemins que dans le projet :

- `embarq/src/main/resources/templates/*.html` : les attributs `data-testid` ;
- `embarq/src/test/java/fr/iroise/embarq/e2e/TestId.java` et les trois Page Objects : les sélecteurs `data-testid` ;
- `embarq/src/test/java/fr/iroise/embarq/e2e/ReservationE2E.java` : les `@DisplayName` ;
- `embarq/pom.xml` : les rapports avec les noms lisibles ;
- `embarq/docs/cas-de-test.md` et `embarq/docs/tests.md` : la documentation.

Pour comparer avec votre travail : `diff -r solutions/06-documentation/embarq embarq`.

## Étape 1

**Question 1.** Les trois tests lèvent `NoSuchElementException` :

```
no such element: Unable to locate element: {"method":"css selector","selector":"a[id^='reserver-']"}
no such element: Unable to locate element: {"method":"xpath","selector":"//tr[td[normalize-space()='Molenez']]//a[starts-with(@id, 'reserver-')]"}
```

L'agence a remplacé le tableau des traversées par des cartes (`<article>`), supprimé les `id` des liens « Réserver », renommé les champs (`nom` devient `passager-nom`, `adultes` devient `nb-adultes`…), supprimé l'`id` du bouton, et changé les classes des messages d'erreur (`.erreur-champ` devient `.aide-erreur`). Les tests échouent dès la page d'accueil ; une fois celle-ci réparée, ils échoueraient sur le formulaire, puis sur la confirmation.

**Question 2.** Trois fichiers : `PageAccueil`, `PageReservation`, `PageConfirmation`. `ReservationE2E` n'utilise aucun sélecteur, seulement les actions des pages (`saisir`, `valider`, `montant`) : c'est l'intérêt du patron Page Object. Sans lui, chaque test aurait été à corriger.

## Étape 2

Les pages corrigées sont dans `solutions/06-documentation/embarq/src/main/resources/templates/`. Exemple de `PageAccueil.reserverSur` :

```java
public PageReservation reserverSur(String navire) {
    navigateur.findElements(testId("traversee")).stream()
            .filter(traversee -> traversee.findElement(testId("navire")).getText().equals(navire))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Aucune traversée sur le " + navire))
            .findElement(testId("reserver"))
            .click();
    return new PageReservation(navigateur);
}
```

**Question 3.** Rien, si l'agence conserve les attributs `data-testid` : ils ne dépendent ni du style, ni de la structure, ni des `id`. C'est la règle à donner à l'agence, et à vérifier en revue de code.

## Étape 3

Voir `embarq/docs/cas-de-test.md`. Avec la configuration du rapport, Jenkins affiche :

```
Parcours de réservation (navigateur)
  CT-E2E-01 · Une famille réserve une traversée et reçoit sa référence
  CT-E2E-02 · Le formulaire signale le nom manquant et l'e-mail invalide
  CT-E2E-03 · Le Molenez refuse plus de passagers que de places
```

Les noms des fichiers de rapport ne changent pas (`TEST-fr.iroise.embarq.e2e.ReservationE2E.xml`) : le motif `TEST-*E2E.xml` du `Jenkinsfile` reste valable.

## Étape 4

Voir `embarq/docs/tests.md`.

**Question 4.** Une documentation vivante est générée ou vérifiée par l'exécution des tests : si le comportement change, elle échoue ou se met à jour d'elle-même. Vivants : les scénarios Cucumber (`tarifs.feature` et son rapport), les noms des tests dans Jenkins, la couverture. Pas vivants : `cas-de-test.md` et `tests.md`, écrits à la main. On les garde courts, dans le dépôt, mis à jour dans le même commit que les tests, et reliés aux tests par les identifiants `CT-…`.

**Question 5.** La préproduction garde ses données d'un déploiement à l'autre, et d'autres personnes l'utilisent. Les tests y créent de vraies réservations : les traversées se remplissent (CT-E2E-03 voit un navire complet), les montants et les places changent. Il faut des tests qui créent leurs propres données ou s'adaptent (comme le `containsAnyOf` de CT-E2E-03), des données dédiées aux tests, ou un nettoyage.

## Questions de fin

1. Des sélecteurs dédiés aux tests (`data-testid`) ; des Page Objects qui sont les seuls à connaître les pages ; des attentes explicites plutôt que des pauses ; des tests indépendants qui créent leurs données ; peu de tests end-to-end, sur les parcours critiques.
2. Dans le dépôt, à côté du code : elle est versionnée avec lui (chaque branche a la documentation de son code), relue dans les mêmes demandes de fusion, et GitLab l'affiche. Un wiki ou un tableur se désynchronise du code.
3. Celui qui modifie la page, dans le même commit : les tests end-to-end du pipeline le lui signalent. L'équipe entière est responsable des tests, pas seulement Inès.

## Bonus

- Les `@DisplayName` des autres tests suivent le même principe : `@DisplayName("CT-INT-01 · Les enfants comptent dans les places vendues")`.
- Archivage des captures : `archiveArtifacts artifacts: 'embarq/target/captures/*.png', allowEmptyArchive: true` dans le `post` de l'étape « Tests end-to-end ».
- Un test d'intégration peut charger chaque page (`ApiIT`) et vérifier la présence des `data-testid` attendus dans le HTML : la page casse ce test unitaire rapide avant de casser les tests end-to-end.
