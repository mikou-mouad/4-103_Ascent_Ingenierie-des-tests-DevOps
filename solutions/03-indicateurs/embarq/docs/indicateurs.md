# Les indicateurs qualité d'Embarq

Un indicateur n'a de sens que s'il a une **cible**, une **source** fiable, et quelqu'un qui le **regarde** et **agit**. Les valeurs « aujourd'hui » sont celles du lab à la fin du TP 03.

| Indicateur | Ce qu'il mesure | Valeur aujourd'hui | Cible | Source | Qui le regarde, quand |
|------------|-----------------|--------------------|-------|--------|-----------------------|
| Taux de réussite du pipeline | Part des builds qui finissent en succès | 60 à 80 % (TP en cours) | plus de 90 % | `ci/indicateurs.sh`, API Jenkins | Toute l'équipe, à chaque échec ; tendance le lundi |
| Durée du pipeline | Temps entre le push et le résultat | 2 à 3 min | moins de 10 min | Jenkins, vue des étapes | Tom, à chaque nouvelle étape |
| Couverture des lignes | Part des lignes exécutées par au moins un test | 49 %, 83 % avec le test de l'API | ne baisse jamais ; 75 % minimum | JaCoCo, seuil `jacoco:check` | Le pipeline (bloquant), Inès chaque semaine |
| Tests par niveau (pyramide) | Nombre de tests unitaires, d'intégration, end-to-end | 19 / 11 / 0 | base large, sommet de 3 à 5 parcours | Rapports JUnit de Jenkins | Inès, à chaque sprint |
| Tests instables | Tests qui échouent puis passent sans changement de code | 1 (`GenerateurReferenceTest`), corrigé | 0 toléré | Relances, historique des tests | Celui qui relance un build : il ouvre un ticket |
| Défauts échappés en production | Bugs trouvés par les passagers plutôt que par les tests | 3 l'été dernier | 0 bloquant par saison | Tickets du support | Maëlle, chaque lundi |
| Fréquence de déploiement | Nombre de mises en production | 4 par an | 1 par semaine | Jenkins (TP 07) | Maëlle, chaque mois |
| Délai de mise en production | Temps entre un commit et sa mise en production | plusieurs semaines | moins d'une journée | GitLab et Jenkins (TP 07) | Tom, chaque mois |
| Taux d'échec des changements | Part des mises en production qui causent un incident ou un rollback | 1 sur 4 (le vendredi noir) | moins de 15 % | Jenkins (rollbacks, TP 07) | Maëlle et Tom, chaque mois |
| Temps de rétablissement | Temps pour revenir à un service normal après un incident | un week-end | moins d'une heure | Jenkins (`ci/indicateurs.sh`), tickets | Tom, après chaque incident |

Les quatre derniers sont les indicateurs **DORA** : ils mesurent la livraison, pas seulement les tests.

## Les trois indicateurs que Maëlle verra chaque lundi

1. **Défauts échappés en production** : c'est ce que vivent les passagers.
2. **Taux d'échec des changements** : livre-t-on sans casser ?
3. **Fréquence de déploiement** : livre-t-on souvent, donc par petits morceaux, donc avec moins de risque ?

## Ce qu'on ne mesurera pas (et pourquoi)

- **Le nombre de tests écrits par personne** : il pousse à écrire des tests inutiles.
- **Un objectif de couverture à 100 %** : il pousse à tester des getters et des tests sans vérification. Le seuil sert à ne pas reculer.
- **Le nombre de bugs trouvés par Inès** : il oppose les testeurs et les développeurs, alors que le but commun est qu'il n'y en ait pas.
