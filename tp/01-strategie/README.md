# TP 01 – Stratégie de test et tests unitaires

**Durée :** 1 h 15 (11h05 – 12h00, puis 13h00 – 13h20 après le déjeuner) · **Branche :** `tp-01-strategie` · **Correction :** `solutions/01-strategie/`

## L'incident

> En juin, l'école de Molène a emmené ses 10 élèves de CM2 à Ouessant. La directrice a appelé le lendemain : « Votre site nous a fait payer plein tarif. Vous annoncez pourtant une remise pour les groupes, à partir de 10 passagers. »
>
> **Tom :** « Le calcul est testé ! Il y a des tests dans `TarifServiceTest`. »
> **Inès :** « Deux tests. Un adulte seul, et une famille. »

## Objectifs

- Écrire une stratégie de test fondée sur les risques : quoi tester, à quel niveau, avec quel outil, à quel moment.
- Écrire des tests unitaires JUnit 5 lisibles, dont des tests paramétrés.
- Trouver le bug du tarif de groupe avec un test, le corriger, et le voir passer dans le pipeline.

## Point de départ

```bash
cd ~/iroise && git fetch
git stash -u
git checkout tp-01-strategie
git push gitlab HEAD
```

## Partie A – La stratégie de test (20 min)

Copiez le modèle dans le projet :

```bash
mkdir -p ~/iroise/embarq/docs
cp ~/iroise/tp/01-strategie/strategie-de-test.md ~/iroise/embarq/docs/
```

Ouvrez `embarq/docs/strategie-de-test.md` dans votre éditeur et complétez les sections 1 à 3. Appuyez-vous sur :

- ce que fait Embarq (étape 1 du TP 00) ;
- les incidents de l'été dernier : le surbooking du 14 juillet, la réservation cassée un week-end, l'école de Molène ;
- l'ouverture de la saison, le 1er avril : tout le monde réserve en même temps.

**Question 1.** Quels sont vos trois risques les plus élevés ? Pour chacun, quel type de test le couvre le mieux, et pourquoi pas un test end-to-end pour tout ?

**Question 2.** Dessinez la pyramide d'Embarq aujourd'hui (les tests existants, TP 00) et celle que vous visez. Pourquoi la base est-elle plus large que le sommet ?

Les sections 4 à 6 sont à finir en bonus, ou à la fin de la formation.

## Partie B – Tester le calcul des tarifs (35 min)

Ouvrez `embarq/src/main/java/fr/iroise/embarq/tarif/TarifService.java`. La grille est dans le commentaire de la classe :

| Tarif | Prix |
|-------|------|
| Adulte | 36,00 € |
| Enfant de 4 à 11 ans | 18,00 € |
| Vélo | 17,00 € |
| Groupe : à partir de 10 passagers | 10 % de remise sur les billets passagers, pas sur les vélos |

**1. Les cas de test.** Avant d'écrire du code, listez sur papier les cas qui couvrent cette grille : chaque tarif seul, les combinaisons, et surtout les **valeurs limites** du tarif de groupe.

**Question 3.** Pour la règle « à partir de 10 passagers », quelles valeurs de nombre de passagers faut-il tester au minimum ? Pourquoi ?

**2. Un test par règle.** Dans `embarq/src/test/java/fr/iroise/embarq/tarif/TarifServiceTest.java`, ajoutez un test pour un enfant seul, et un test pour un adulte avec un vélo. Suivez la forme des tests existants : un nom qui dit la règle, une seule vérification.

```bash
cd ~/iroise/embarq
mvn -B test -Dtest=TarifServiceTest
```

**3. Un test paramétré.** Plutôt qu'un test par cas, un seul test parcourt un tableau de cas. Ajoutez dans la classe :

```java
@ParameterizedTest(name = "{0} adulte(s), {1} enfant(s), {2} vélo(s) : {3} €")
@CsvSource({
        "1, 0, 0,  36.00",
        "2, 1, 0,  90.00",
        // TODO : vos cas, dont les valeurs limites du tarif de groupe
})
void calcule_le_prix_selon_la_grille(int adultes, int enfants, int velos, String prixAttendu) {
    assertThat(tarifs.calculer(adultes, enfants, velos)).isEqualByComparingTo(prixAttendu);
}
```

avec les imports :

```java
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
```

Ajoutez vos cas, calculez le prix attendu **à la main**, à partir de la grille, pas à partir du code. Lancez les tests.

**Question 4.** Quel cas échoue ? Que dit le message d'échec ? Retrouvez l'école de Molène.

## Partie C – Corriger, puis livrer par le pipeline (20 min, après le déjeuner)

**4. La correction.** Corrigez `TarifService` pour que tous les tests passent. Relancez :

```bash
mvn -B test
```

**Question 5.** Pourquoi a-t-on écrit le test **avant** de corriger le code ? Que se serait-il passé si Tom avait corrigé directement ?

**5. Dans le pipeline.**

```bash
cd ~/iroise
git add embarq
git commit -m "Tarif de groupe à partir de 10 passagers, avec ses tests"
git push gitlab HEAD
```

Dans Jenkins, ouvrez le build, puis **Tests** : vos tests paramétrés apparaissent, un par cas.

**6. Et la capacité des navires ?** Dans `ReservationServiceTest`, ajoutez un test : le Molenez (12 places) a déjà 10 places réservées, une famille demande 1 adulte et 2 enfants ; la réservation doit être refusée.

**Question 6.** Le test passe. Ce test prouve-t-il qu'Embarq compte correctement les places déjà vendues ? Qu'est-ce qui, dans ce test, décide du nombre de places réservées ?

## Questions de fin

1. Quelle différence entre un test unitaire et un test d'intégration ? Où classez-vous `ReservationServiceTest` ?
2. Une couverture de 100 % sur `TarifService` aurait-elle suffi à trouver le bug ?
3. Qu'appelle-t-on « dette technique » dans les tests ? Donnez un exemple dans Embarq.

## Bonus

- Finissez les sections 4 à 6 de la stratégie.
- Ajoutez un test qui vérifie que la remise ne s'applique pas aux vélos.
- Testez un cas absurde : `calculer(0, 0, 0)`. Que renvoie le service ? Est-ce le rôle du service ou du formulaire de l'empêcher ?
