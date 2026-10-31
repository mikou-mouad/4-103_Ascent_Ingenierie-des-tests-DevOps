# Stratégie de test d'Embarq

> À copier dans `embarq/docs/strategie-de-test.md`, puis à compléter (les `TODO`).
> Une stratégie tient en une ou deux pages : elle dit **quoi** tester, **à quel niveau**, **avec quel outil**, **à quel moment du pipeline**, et ce qu'on **ne** teste pas.

## 1. Les risques

Impact et probabilité : 1 (faible) à 3 (fort). Niveau = impact × probabilité.

| Fonctionnalité | Ce qui peut mal tourner | Impact | Probabilité | Niveau |
|----------------|-------------------------|--------|-------------|--------|
| Calcul du prix | Un passager paie trop ou pas assez (enfants, vélos, groupes) | TODO | TODO | TODO |
| Capacité des navires | TODO | TODO | TODO | TODO |
| Formulaire de réservation | TODO | TODO | TODO | TODO |
| Référence de réservation | TODO | TODO | TODO | TODO |
| API REST (application mobile) | TODO | TODO | TODO | TODO |
| Ouverture de la saison (1er avril, 9h) | TODO | TODO | TODO | TODO |
| Déploiement | TODO | TODO | TODO | TODO |

## 2. Ce qu'on teste, et où

| Risque | Type de test | Outil | Étape du pipeline | Qui l'écrit |
|--------|--------------|-------|-------------------|-------------|
| Calcul du prix | Unitaire | JUnit 5 | Tests unitaires, à chaque push | Tom |
| TODO | TODO | TODO | TODO | TODO |

Types possibles : unitaire, intégration, contrat d'API, acceptation (BDD), end-to-end, performance, smoke test, exploratoire (manuel).

## 3. La pyramide visée

| Niveau | Nombre de tests visé | Durée visée dans le pipeline |
|--------|----------------------|------------------------------|
| End-to-end (navigateur) | TODO | TODO |
| Intégration (vraie base) | TODO | TODO |
| Unitaires | TODO | TODO |

## 4. Ce qu'on ne teste pas (et pourquoi)

- TODO

## 5. Quand peut-on livrer ?

- TODO (par exemple : tous les tests verts, couverture minimale, aucun bug bloquant ouvert…)

## 6. La dette de test

Ce qui manque aujourd'hui et qu'on rattrapera, par ordre de priorité :

1. TODO
