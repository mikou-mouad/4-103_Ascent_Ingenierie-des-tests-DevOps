# Correction du TP 02 – Tests d'intégration et de performance

Fichiers corrigés, aux mêmes chemins que dans le projet :

- `embarq/src/test/java/fr/iroise/embarq/reservation/ReservationRepositoryIT.java` : les tests d'intégration, avec le bonus ;
- `embarq/src/main/java/fr/iroise/embarq/reservation/ReservationRepository.java` : la requête corrigée ;
- `embarq/Jenkinsfile` : l'étape « Tests d'intégration ».

Pour comparer avec votre travail : `diff -r solutions/02-integration/embarq embarq`.

## Étape 1

Les deux tests complétés :

```java
@Test
void les_enfants_comptent_dans_les_places_vendues() {
    reserver("IR-001", 2, 3);

    assertThat(reservations.placesReservees(molenez.getId())).isEqualTo(5);
}

@Test
void les_reservations_annulees_liberent_leurs_places() {
    Reservation annulee = reserver("IR-002", 4, 0);
    annulee.annuler();
    reserver("IR-003", 2, 0);

    assertThat(reservations.placesReservees(molenez.getId())).isEqualTo(2);
}
```

`annuler()` modifie l'entité, sans appel à `save` : l'entité est suivie par JPA, et la modification part en base avant la requête suivante.

**Question 1.** Les deux tests complétés échouent :

```
les_enfants_comptent_dans_les_places_vendues      expected: 5L but was: 2L
les_reservations_annulees_liberent_leurs_places   expected: 2L but was: 6L
```

**Question 2.** La requête compte `sum(r.adultes)` : les enfants ne sont jamais comptés. Elle compte aussi les réservations annulées. Le 14 juillet, les familles et les groupes scolaires ont réservé des places « invisibles » : Embarq croyait qu'il en restait, et a vendu 312 places pour 280. Les annulations, elles, bloquaient des places libres : le navire pouvait aussi s'afficher complet à tort.

## Étape 2

```java
@Query("""
        select coalesce(sum(r.adultes + r.enfants), 0) from Reservation r
        where r.traversee.id = :traverseeId
          and r.statut = fr.iroise.embarq.reservation.StatutReservation.CONFIRMEE
        """)
long placesReservees(@Param("traverseeId") Long traverseeId);
```

**Question 3.** Non : verts avant, verts après. Ils remplacent `placesReservees` par un mock qui renvoie la valeur choisie par le test. La requête n'est jamais exécutée. C'est le rôle d'un test unitaire (vérifier la décision du service), et sa limite : il ne voit pas l'intégration avec la base.

## Étape 3

**Question 4.** Le conteneur Jenkins a accès au Docker de la machine : le fichier `/var/run/docker.sock` est monté dans le conteneur, et Jenkins fait partie du groupe `docker` (`group_add`). Testcontainers démarre donc PostgreSQL sur le Docker de la machine, à côté de Jenkins. La variable `TESTCONTAINERS_HOST_OVERRIDE=host.docker.internal` lui indique comment joindre les ports de ce conteneur depuis Jenkins.

## Étape 4

**Question 5.** Les valeurs dépendent de la machine ; ordre de grandeur sur 4 vCPU : 50 à 60 requêtes par seconde (20 visiteurs qui font 3 requêtes puis attendent 1 s), un `p(95)` de 10 à 50 ms, et 0 % d'erreurs. Les seuils sont tenus : k6 affiche une coche verte devant `http_req_duration` et `http_req_failed`, et sort avec le code 0.

**Question 6.** `target: 300` dans les étapes (et une montée plus longue). Ce test est long et dépend de la machine : il ne tourne pas à chaque push. On le lance sur un environnement proche de la production (la préproduction, TP 07), avant une mise en production importante, ou chaque nuit. Ses seuils en font un test automatique : il échoue si les temps de réponse se dégradent.

## Questions de fin

1. H2 n'est pas PostgreSQL : syntaxe SQL, types, comportement des contraintes et des transactions diffèrent. Les migrations Flyway d'Embarq utilisent `generate_series` et les intervalles de PostgreSQL : elles ne passeraient même pas sur H2. Un test d'intégration doit tourner sur la même base que la production.
2. Séparer les étapes (unitaires d'abord, pour un retour rapide ; intégration ensuite), réutiliser le conteneur entre les classes de test, paralléliser, ne garder en intégration que ce qui a besoin de la base, et pousser la logique dans des tests unitaires.
3. Non : la machine du lab n'a ni la puissance, ni le réseau, ni les données, ni la configuration de la production. Il donne une tendance et détecte les régressions (« deux fois plus lent que la semaine dernière »). Le test qui compte se fait sur un environnement dimensionné comme la production.

## Bonus

- `les_places_d_une_autre_traversee_ne_comptent_pas` est dans la correction.
- À 100 visiteurs, la machine du lab sature souvent sur le processeur (Embarq, PostgreSQL et k6 se partagent les mêmes vCPU) : le `p(95)` monte. C'est une limite du lab, pas forcément d'Embarq.
- En réservant en boucle, le navire se remplit : l'API répond `409 Conflict` avec `{"erreur":"Cette traversée est complète."}`. Un test de charge qui écrit doit préparer ses données, ou viser plusieurs traversées.
