# Stratégie de test d'Embarq

Une stratégie tient en une ou deux pages : elle dit **quoi** tester, **à quel niveau**, **avec quel outil**, **à quel moment du pipeline**, et ce qu'on **ne** teste pas. Elle est relue à chaque incident.

## 1. Les risques

Impact et probabilité : 1 (faible) à 3 (fort). Niveau = impact × probabilité.

| Fonctionnalité | Ce qui peut mal tourner | Impact | Probabilité | Niveau |
|----------------|-------------------------|--------|-------------|--------|
| Calcul du prix | Un passager paie trop ou pas assez (enfants, vélos, groupes) | 2 | 3 | 6 |
| Capacité des navires | Surbooking : des passagers restent à quai (14 juillet) | 3 | 2 | **6** |
| Formulaire de réservation | Plus personne ne peut réserver (le week-end cassé) | 3 | 2 | **6** |
| Référence de réservation | Deux passagers reçoivent la même référence ; la seconde réservation échoue | 2 | 2 | 4 |
| API REST (application mobile) | Un changement casse l'application mobile | 2 | 1 | 2 |
| Ouverture de la saison (1er avril, 9h) | Le site ralentit ou tombe sous la charge | 3 | 2 | **6** |
| Déploiement | La mauvaise version part, ou la nouvelle ne démarre pas en production | 3 | 2 | **6** |

## 2. Ce qu'on teste, et où

| Risque | Type de test | Outil | Étape du pipeline | Qui l'écrit |
|--------|--------------|-------|-------------------|-------------|
| Calcul du prix | Unitaire, tests paramétrés sur la grille et ses limites | JUnit 5 | Tests unitaires, à chaque push | Tom |
| Calcul du prix (règles métier) | Acceptation, scénarios écrits avec Maëlle | Cucumber | Tests unitaires, à chaque push | Maëlle, Inès, Tom |
| Capacité des navires | Intégration, avec une vraie base PostgreSQL | JUnit 5, Testcontainers | Tests d'intégration, à chaque push | Tom, Inès |
| Formulaire de réservation | End-to-end, parcours d'un passager dans un vrai navigateur | Selenium | Tests end-to-end, à chaque push | Inès |
| Référence de réservation | Unitaire (format, unicité), intégration (contrainte d'unicité en base) | JUnit 5 | Tests unitaires et d'intégration | Tom |
| API REST | Intégration de l'API (statuts, format JSON) | JUnit 5, Spring | Tests d'intégration | Tom |
| Ouverture de la saison | Performance : charge de l'ouverture, seuils de temps de réponse | k6 | Préproduction, avant la mise en production | Inès |
| Déploiement | Smoke tests après chaque déploiement, rollback automatique | curl, Jenkins | Préproduction et production | Inès, Tom |

## 3. La pyramide visée

| Niveau | Nombre de tests visé | Durée visée dans le pipeline |
|--------|----------------------|------------------------------|
| End-to-end (navigateur) | 3 à 5 parcours critiques | moins de 2 min |
| Intégration (vraie base) | 10 à 20 | moins de 2 min |
| Unitaires et scénarios Cucumber | 50 et plus | moins de 30 s |

Les tests du bas sont rapides, stables et précis (un échec désigne une méthode). Ceux du haut sont lents, plus fragiles, et un échec désigne seulement un parcours : on les garde pour ce que seuls eux peuvent vérifier.

## 4. Ce qu'on ne teste pas (et pourquoi)

- Le paiement en ligne : il est délégué au prestataire bancaire, qui le teste. On teste seulement l'appel, avec un faux prestataire.
- Les navigateurs anciens : Chrome dans le pipeline ; Firefox et Safari à la main, une fois par version.
- L'ergonomie et les textes : relecture et tests exploratoires d'Inès, pas d'automatisation.

## 5. Quand peut-on livrer ?

- Tous les tests du pipeline sont verts, sans relance.
- La couverture des lignes ne baisse pas (seuil dans le pipeline, TP 03).
- Les smoke tests passent en préproduction.
- Aucun bug bloquant ouvert sur la version.
- Pour l'ouverture de la saison : le test de charge passe ses seuils en préproduction.

## 6. La dette de test

1. Aucun test n'exécute les requêtes SQL : le surbooking peut revenir.
2. Aucun test ne passe par les pages web.
3. Les tests du service utilisent des mocks qui « décident » des places vendues.
4. Pas de mesure de couverture, pas d'indicateurs.
5. Le déploiement est manuel et n'est pas vérifié.
