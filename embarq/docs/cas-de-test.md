# Les cas de test d'Embarq

Un cas de test documenté répond à cinq questions : **quelle exigence** il vérifie, **dans quel état** on part, **quoi faire**, **quoi attendre**, et **où il est automatisé**. L'identifiant du cas est repris dans le `@DisplayName` du test : on passe du rapport Jenkins à cette page, et inversement.

## Index

| Id | Titre | Exigence | Niveau | Automatisé par | Étape du pipeline |
|----|-------|----------|--------|----------------|-------------------|
| CT-E2E-01 | Une famille réserve une traversée et reçoit sa référence | Un passager peut réserver en ligne | End-to-end | `ReservationE2E#un_passager_reserve_une_traversee_en_famille` | Tests end-to-end |
| CT-E2E-02 | Le formulaire signale le nom manquant et l'e-mail invalide | Une réservation a un nom et un e-mail valide | End-to-end | `ReservationE2E#le_formulaire_signale_les_champs_mal_remplis` | Tests end-to-end |
| CT-E2E-03 | Le Molenez refuse plus de passagers que de places | Pas de surbooking | End-to-end | `ReservationE2E#le_molenez_refuse_plus_de_passagers_que_de_places` | Tests end-to-end |
| CT-INT-01 | Les enfants comptent dans les places vendues | Pas de surbooking | Intégration | `ReservationRepositoryIT#les_enfants_comptent_dans_les_places_vendues` | Tests d'intégration |
| CT-INT-02 | Les réservations annulées libèrent leurs places | Pas de surbooking, pas de faux « complet » | Intégration | `ReservationRepositoryIT#les_reservations_annulees_liberent_leurs_places` | Tests d'intégration |
| CT-TAR-01 | Tarif de groupe à partir de 10 passagers | Grille tarifaire | Unitaire | `TarifServiceTest#calcule_le_prix_selon_la_grille` | Tests unitaires |
| CT-TAR-02 | Tarif résident des îles | Convention avec le Département | Acceptation (Cucumber) | `features/tarifs.feature`, règle « Tarif résident des îles » | Tests unitaires |

## CT-E2E-01 – Une famille réserve une traversée et reçoit sa référence

| | |
|-|-|
| **Exigence** | Un passager peut réserver une traversée en ligne et reçoit une référence à présenter à l'embarquement. |
| **Préconditions** | Embarq déployé (environnement e2e, base neuve) ; au moins une traversée à venir avec des places. |
| **Données** | Nom « Le Gall », e-mail legall@example.org, 2 adultes, 1 enfant, 0 vélo |
| **Étapes** | 1. Ouvrir la page d'accueil.<br>2. Cliquer sur « Réserver ma place » pour la première traversée.<br>3. Saisir les données et confirmer. |
| **Résultat attendu** | La page « Bon voyage ! » affiche une référence `IR-` suivie de 8 caractères, le statut « confirmée », et un total de 90,00 €. |
| **Automatisé par** | `ReservationE2E#un_passager_reserve_une_traversee_en_famille` |
| **Exécution** | À chaque push, étape « Tests end-to-end ». À la main : `mvn -B -Pe2e verify -Dapp.url=http://embarq-e2e:8080` (voir [tests.md](tests.md)). |

## CT-E2E-02 – Le formulaire signale le nom manquant et l'e-mail invalide

| | |
|-|-|
| **Exigence** | Une réservation a toujours un nom et une adresse e-mail valide ; le passager voit ce qu'il doit corriger. |
| **Préconditions** | Embarq déployé ; au moins une traversée à venir. |
| **Données** | Nom vide, e-mail « pas-un-email », 1 adulte (le navigateur refuse 0 adulte avant l'envoi). |
| **Étapes** | 1. Ouvrir la page d'accueil, choisir la première traversée.<br>2. Saisir les données et confirmer. |
| **Résultat attendu** | Le formulaire reste affiché, avec « Le nom est obligatoire » et « Adresse e-mail invalide ». Aucune réservation n'est créée. |
| **Automatisé par** | `ReservationE2E#le_formulaire_signale_les_champs_mal_remplis` |
| **Exécution** | À chaque push, étape « Tests end-to-end ». |

## CT-E2E-03 – Le Molenez refuse plus de passagers que de places

| | |
|-|-|
| **Exigence** | Embarq ne vend jamais plus de places que la capacité du navire (incident du 14 juillet). |
| **Préconditions** | Embarq déployé ; une traversée du Molenez (12 places) à venir. |
| **Données** | « École de Molène », ecole@example.org, 13 adultes. |
| **Étapes** | 1. Ouvrir la page d'accueil, choisir une traversée du Molenez.<br>2. Saisir les données et confirmer. |
| **Résultat attendu** | Le formulaire reste affiché, avec un message « Il ne reste que N place(s) » ou « Cette traversée est complète ». |
| **Automatisé par** | `ReservationE2E#le_molenez_refuse_plus_de_passagers_que_de_places` |
| **Exécution** | À chaque push, étape « Tests end-to-end ». Le calcul des places est vérifié en détail par CT-INT-01 et CT-INT-02. |

## CT-INT-01 – Les enfants comptent dans les places vendues

| | |
|-|-|
| **Exigence** | Pas de surbooking : adultes et enfants occupent une place. |
| **Préconditions** | Base PostgreSQL neuve (Testcontainers), migrations Flyway appliquées. |
| **Données** | Une traversée du Molenez ; une réservation de 2 adultes et 3 enfants. |
| **Étapes** | Compter les places vendues de la traversée. |
| **Résultat attendu** | 5 places vendues. |
| **Automatisé par** | `ReservationRepositoryIT#les_enfants_comptent_dans_les_places_vendues` |
| **Exécution** | À chaque push, étape « Tests d'intégration ». À la main : `mvn -B verify`. |
