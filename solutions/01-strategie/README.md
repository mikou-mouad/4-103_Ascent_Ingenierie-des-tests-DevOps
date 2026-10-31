# Correction du TP 01 – Stratégie de test et tests unitaires

Fichiers corrigés, aux mêmes chemins que dans le projet :

- `embarq/docs/strategie-de-test.md` : une stratégie complète ;
- `embarq/src/test/java/fr/iroise/embarq/tarif/TarifServiceTest.java` : les tests du calcul des tarifs ;
- `embarq/src/main/java/fr/iroise/embarq/tarif/TarifService.java` : le tarif de groupe corrigé ;
- `embarq/src/test/java/fr/iroise/embarq/reservation/ReservationServiceTest.java` : le test de capacité de l'étape 6.

Pour comparer avec votre travail : `diff -r solutions/01-strategie/embarq embarq`. Attention, `embarq/` est l'état de **départ** du TP, pas la correction.

## Partie A

**Question 1.** Dans la correction, cinq risques sont au niveau 6 : le prix, la capacité, le formulaire, l'ouverture de la saison et le déploiement. Chacun a son niveau de test :

- la capacité se joue dans une requête SQL : un **test d'intégration** avec une vraie base ;
- le formulaire se casse dans les pages : un **test end-to-end**, mais seulement pour le parcours principal ;
- la charge de l'ouverture : un **test de performance**, sur un environnement proche de la production.

Tout tester en end-to-end serait lent (plusieurs secondes par test), fragile (le moindre changement de page casse les tests) et peu précis (un échec dit « la réservation ne marche pas », pas « le seuil de groupe est faux »). On teste chaque règle au niveau le plus bas qui peut la vérifier.

**Question 2.** Aujourd'hui : une base de 6 tests unitaires, rien au-dessus. C'est une pyramide sans étages, qui laisse passer les bugs d'intégration et d'interface. Visée : beaucoup de tests unitaires, moins de tests d'intégration, quelques tests end-to-end. La base est large parce que ses tests sont rapides, stables et précis : c'est là qu'on vérifie la logique, cas par cas.

## Partie B

**Question 3.** Au minimum 9, 10 et 11 passagers : juste sous le seuil, pile au seuil, juste au-dessus. Les bugs se cachent aux frontières (`>` au lieu de `>=`). Il vaut aussi la peine de mélanger adultes et enfants (2 adultes + 8 enfants) pour vérifier que les enfants comptent dans le groupe.

**Question 4.** Les cas à 10 passagers échouent, par exemple :

```
10 adulte(s), 0 enfant(s), 0 vélo(s) : 324.00 €  FAILED
Expecting actual: 360.00 to be equal to: 324.00 when comparing values using 'BigDecimal.compareTo'
```

Avec 10 passagers, le code ne donne pas la remise : c'est l'école de Molène (10 élèves, ou 2 accompagnateurs et 8 enfants selon votre cas). Les cas à 9 et 11 passent : seule la valeur limite trahit le bug.

## Partie C

**4.** Dans `TarifService`, la condition devient `adultes + enfants >= SEUIL_GROUPE`.

**Question 5.** Le test écrit d'abord prouve deux choses : qu'il sait détecter le bug (il est rouge), puis que la correction le règle (il devient vert). Un test écrit après une correction peut être faux sans qu'on le sache : il n'a jamais été rouge. Et il reste dans le pipeline : le bug ne peut pas revenir sans qu'on le voie (test de non-régression).

**Question 6.** Le test passe, mais il ne prouve rien sur le comptage des places : c'est le mock `when(reservations.placesReservees(any())).thenReturn(10L)` qui décide qu'il y a 10 places vendues. Le test vérifie la décision du service (« 3 passagers pour 2 places : refus »), pas la requête SQL qui compte les places. Si cette requête oublie les enfants, ce test reste vert : c'est le sujet du TP 02.

## Questions de fin

1. Un test unitaire vérifie une unité de code isolée (une classe), sans base, sans réseau, en quelques millisecondes. Un test d'intégration vérifie que plusieurs éléments fonctionnent ensemble (le code et la base, l'application et ses dépendances). `ReservationServiceTest` est unitaire : les dépôts sont des mocks.
2. Non. Les deux tests de départ exécutaient déjà toutes les lignes de `calculer` sauf la remise. Un test à 11 passagers exécute la remise : 100 % de couverture, et le bug à 10 passagers passe toujours. La couverture dit ce qui a été exécuté, pas ce qui a été vérifié.
3. La dette de test, c'est ce qu'on n'a pas testé et qu'on paiera plus tard (en bugs, en tests manuels, en peur de modifier le code). Dans Embarq : aucun test des requêtes SQL, aucun test des pages, les mocks qui décident du résultat (section 6 de la stratégie).

## Bonus

- La remise ne s'applique pas aux vélos : `la_remise_de_groupe_ne_s_applique_pas_aux_velos` (10 adultes et 10 vélos : 324 + 170 = 494 €).
- `calculer(0, 0, 0)` renvoie 0,00 €. Le service calcule, il ne valide pas : c'est la `DemandeReservation` qui impose au moins un adulte (`@Min(1)`), et le formulaire qui l'affiche. On peut le tester aussi, au niveau du formulaire (TP 05).
