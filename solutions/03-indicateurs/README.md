# Correction du TP 03 – Indicateurs qualité et quality gate

Fichiers corrigés, aux mêmes chemins que dans le projet :

- `embarq/pom.xml` : le plugin JaCoCo et son seuil ;
- `embarq/Jenkinsfile` : la publication de la couverture ;
- `embarq/src/main/java/fr/iroise/embarq/reservation/GenerateurReference.java` et son test : la fin du test instable ;
- `embarq/docs/indicateurs.md` : les indicateurs d'Embarq ;
- `embarq/src/test/java/fr/iroise/embarq/ApiIT.java` : le bonus, le test d'intégration de l'API.

Pour comparer avec votre travail : `diff -r solutions/03-indicateurs/embarq embarq`.

Le seuil de la correction est à **0.75** : avec le test de l'API (bonus), la couverture monte à 83 %, et on remonte le seuil. Sans le bonus, gardez 0.45.

## Étape 1

**Question 1.**

```
[WARNING] Rule violated for bundle embarq: lines covered ratio is 0.49, but expected minimum is 0.60
[ERROR] Failed to execute goal org.jacoco:jacoco-maven-plugin:0.8.16:check (seuil) on project embarq:
        Coverage checks have not been met.
```

Environ 49 % des lignes sont exécutées par les tests unitaires et d'intégration.

## Étape 2

**Question 2.** `fr.iroise.embarq.web` est à 0 % : les pages, le formulaire et l'API ne sont exécutés par aucun test. Le week-end où la réservation était cassée pourrait se reproduire sans qu'aucun test ne le voie.

**Question 3.** Oui : les deux tests de départ (1 adulte, une famille) exécutaient toutes les lignes sauf la remise, et un seul cas à plus de 10 passagers aurait suffi pour atteindre 100 %. Le bug était sur une **valeur limite** (10 passagers) : une ligne exécutée n'est pas une ligne vérifiée. La couverture montre surtout ce qui n'est **pas** testé.

Le seuil à 0.45 est un **cliquet** : il fige le niveau actuel, et on le remonte à chaque nouveau test. Viser tout de suite un chiffre élevé pousse à écrire des tests sans assertion, juste pour la couverture.

## Étape 3

**Question 4.** La couverture baisse. Si elle passe sous le seuil, `jacoco:check` fait échouer `mvn verify` à l'étape « Tests d'intégration » : le build est rouge, le job GitLab aussi, et l'étape « Paquet » n'est pas exécutée. Le rapport est quand même publié, grâce au bloc `always`.

## Étape 4

**Question 5.** De 1 à 4 échecs sur 20 en général. La référence est `IR-` suivi d'un nombre tiré au hasard entre 000 et 999 : 1 000 références possibles. Le test en tire 15 et vérifie qu'elles sont toutes différentes. La probabilité que deux d'entre elles soient égales est d'environ 10 % (le « paradoxe des anniversaires »).

**Question 6.** C'est le code qui est mauvais, et le test qui avait raison. La colonne `reference` est `unique` : quand une référence déjà prise est tirée, l'enregistrement échoue et le passager voit une erreur. Après quelques centaines de réservations, une réservation sur deux ou trois échoue ; au-delà de 1 000, plus aucune ne passe. Relancer ce test jusqu'à ce qu'il passe, c'était masquer un bug de production.

La correction :

```java
static final String ALPHABET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
static final int LONGUEUR = 8;

private final SecureRandom random = new SecureRandom();

public String nouvelleReference() {
    StringBuilder reference = new StringBuilder("IR-");
    for (int i = 0; i < LONGUEUR; i++) {
        reference.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
    }
    return reference.toString();
}
```

31 caractères sur 8 positions : environ 850 milliards de références. Parmi 1 000, la probabilité d'un doublon est inférieure à un sur un million : le test devient stable.

## Étape 5

Un tableau complet est dans `embarq/docs/indicateurs.md`.

**Question 7.** Fréquence de déploiement, délai de mise en production, taux d'échec des changements, temps de rétablissement. Ils mesurent ce que l'équipe livre aux passagers, et à quel prix : c'est le langage de Maëlle. La couverture est un indicateur interne, utile à l'équipe, qui ne dit rien de ce que vivent les passagers.

**Question 8.** Par exemple : le nombre de tests écrits par personne (on écrit des tests inutiles), une couverture imposée à chacun (des tests sans assertion), le nombre de bugs trouvés par les testeurs (on oppose testeurs et développeurs). Quand un indicateur devient un objectif, il cesse d'être un bon indicateur (loi de Goodhart).

## Questions de fin

1. 80 % des lignes ont été exécutées au moins une fois pendant les tests. Cela ne dit pas si elles ont été vérifiées (un test sans assertion couvre), si les valeurs limites ont été testées, ni si les 20 % restants sont les plus risqués.
2. Une quality gate est un ensemble de critères qui bloquent le pipeline s'ils ne sont pas tenus. Autres critères : aucun test en échec, aucune vulnérabilité critique dans les dépendances, aucune nouvelle alerte d'analyse statique, durée du pipeline, seuils du test de charge.
3. On masque un vrai bug (comme ici), et on habitue l'équipe à ignorer les échecs : le jour où un échec est sérieux, personne ne le regarde.

## Bonus

- `ApiIT.java` est dans la correction : 7 tests sur l'application complète, avec PostgreSQL dans Testcontainers. La couverture passe à environ 83 %, et le seuil à 0.75.
- Règle sur les branches, à ajouter dans `<limits>` :

  ```xml
  <limit>
    <counter>BRANCH</counter>
    <value>COVEREDRATIO</value>
    <minimum>0.60</minimum>
  </limit>
  ```
