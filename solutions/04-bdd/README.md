# Correction du TP 04 – Shift-left et BDD avec Cucumber

Fichiers corrigés, aux mêmes chemins que dans le projet :

- `embarq/src/test/resources/features/tarifs.feature` : les scénarios du tarif résident ;
- `embarq/src/test/java/fr/iroise/embarq/bdd/TarifsSteps.java` : les étapes complétées ;
- `embarq/src/main/java/fr/iroise/embarq/tarif/TarifService.java` : le tarif résident corrigé ;
- `embarq/Jenkinsfile` : la publication du rapport Cucumber.

Pour comparer avec votre travail : `diff -r solutions/04-bdd/embarq embarq`.

## Étape 1

**Question 1.** Un exemple par phrase :

| Règle | Adultes | Enfants | Vélos | Prix attendu |
|-------|---------|---------|-------|--------------|
| Un adulte résident paie 9 € | 1 | 0 | 0 | 9,00 € |
| Les enfants résidents voyagent gratuitement | 1 | 2 | 0 | 9,00 € |
| Les vélos restent au plein tarif | 2 | 0 | 2 | 52,00 € |
| Pas de remise de groupe | 10 | 0 | 0 | 90,00 € |

**Question 2.** Version de Tom (moitié prix sur tout, remise de groupe comprise) : 10 adultes paient 324 / 2 = 162,00 € au lieu de 90 € ; 2 adultes et 2 vélos paient (72 + 34) / 2 = 53,00 € au lieu de 52 €. Un adulte seul paie 18 € au lieu de 9 €. Aucun exemple ne tombe juste : Tom a codé ce qu'il a compris d'un appel téléphonique.

## Étape 2

```gherkin
  Règle: Tarif résident des îles

    Contexte:
      Étant donné des passagers résidents des îles

    Scénario: Un adulte résident paie 9 €
      Quand je réserve pour 1 adulte, 0 enfant et 0 vélo
      Alors le prix est de 9,00 €

    Scénario: Les enfants résidents voyagent gratuitement
      Quand je réserve pour 1 adulte, 2 enfants et 0 vélo
      Alors le prix est de 9,00 €

    Scénario: Les vélos des résidents paient plein tarif
      Quand je réserve pour 2 adultes, 0 enfant et 2 vélos
      Alors le prix est de 52,00 €

    Scénario: Pas de remise de groupe pour les résidents
      Quand je réserve pour 10 adultes, 0 enfant et 0 vélo
      Alors le prix est de 90,00 €
```

## Étape 3

```java
@Etantdonné("des passagers résidents des îles")
public void des_passagers_residents_des_iles() {
    resident = true;
}

@Quand("je réserve pour {int} adulte(s), {int} enfant(s) et {int} vélo(s)")
public void je_reserve_pour(int adultes, int enfants, int velos) {
    prix = tarifs.calculer(adultes, enfants, velos, resident);
}

@Alors("le prix est de {bigdecimal} €")
public void le_prix_est_de(BigDecimal attendu) {
    assertThat(prix).isEqualByComparingTo(attendu);
}
```

**Question 3.** Les quatre scénarios résidents échouent ; les trois exemples du tarif normal passent :

```
Scénario: Un adulte résident paie 9 €                      expected: 9.00   but was: 18.00
Scénario: Les enfants résidents voyagent gratuitement      expected: 9.00   but was: 36.00
Scénario: Les vélos des résidents paient plein tarif       expected: 52.00  but was: 53.00
Scénario: Pas de remise de groupe pour les résidents       expected: 90.00  but was: 162.00
```

Le titre du scénario et la phrase en échec sont dans la langue de Maëlle : elle comprend quelle règle n'est pas respectée, sans lire de Java.

## Étape 4

```java
public BigDecimal calculer(int adultes, int enfants, int velos, boolean resident) {
    if (!resident) {
        return calculer(adultes, enfants, velos);
    }
    // Les enfants résidents voyagent gratuitement
    BigDecimal total = ADULTE_RESIDENT.multiply(BigDecimal.valueOf(adultes))
            .add(VELO.multiply(BigDecimal.valueOf(velos)));
    return total.setScale(2, RoundingMode.HALF_UP);
}
```

avec `static final BigDecimal ADULTE_RESIDENT = new BigDecimal("9.00");`.

## Étape 5

**Question 4.** Le shift-left consiste à déplacer les activités de test vers le début du cycle : on définit les tests (ici, les exemples) en même temps que le besoin, avant le code. Les scénarios auraient dû être écrits le jour de l'appel de Maëlle, avec elle : le malentendu serait apparu en dix minutes, au lieu de la veille de la livraison. Le coût d'un défaut augmente à chaque étape où il survit.

## Questions de fin

1. Un test paramétré JUnit est écrit par et pour les développeurs : il vérifie le code. Un scénario Cucumber est écrit **avec** le métier, dans sa langue : c'est à la fois une spécification, un test automatique, et une documentation qui reste à jour (si le code change sans elle, le scénario échoue).
2. Non. Cucumber relie des phrases à du code, quel qu'il soit. Ici les étapes appellent directement `TarifService` : les scénarios sont au niveau unitaire, ils tournent en une seconde. Les mêmes phrases pourraient piloter l'API ou un navigateur (Selenium), au prix de la vitesse.
3. **JUnit** exécute les tests et les assertions (moteur de tests) ; **Cucumber** décrit les règles métier en Gherkin et les relie au code, exécuté par JUnit ; **Selenium** pilote un vrai navigateur pour les tests de bout en bout (TP 05) ; **Jenkins** orchestre le tout à chaque push, et publie les rapports.

## Bonus

- `0 adulte et 3 enfants` : `TarifService` renverrait 0,00 €, mais la règle « au moins un adulte par réservation » est portée par `DemandeReservation` (`@Min(1)`). On la teste au niveau du formulaire ou de l'API (TP 05, et `ApiIT`), pas dans les scénarios de tarif.
- Avec un plan de scénario :

  ```gherkin
  Plan du scénario: Prix pour des résidents
    Quand je réserve pour <adultes> adultes, <enfants> enfants et <velos> vélos
    Alors le prix est de <prix> €

    Exemples:
      | adultes | enfants | velos | prix  |
      | 1       | 0       | 0     | 9,00  |
      | 1       | 2       | 0     | 9,00  |
      | 2       | 0       | 2     | 52,00 |
      | 10      | 0       | 0     | 90,00 |
  ```

- `@resident` au-dessus de `Règle: Tarif résident des îles` étiquette tous ses scénarios ; `-Dcucumber.filter.tags=@resident` ne lance qu'eux.
