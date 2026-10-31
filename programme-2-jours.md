# Ingénierie des tests DevOps : programme sur 2 jours

**Référence :** Ascent Formation 4-103 · **Durée :** 2 jours – 12 h · **Public :** ingénieurs DevOps, testeurs et ingénieurs QA, développeurs impliqués dans l'intégration et la livraison continues

**Rythme d'une journée**

| Demi-journée | Horaires      | Pause café    |
|--------------|---------------|---------------|
| Matin        | 9h00 – 12h00  | 10h30 – 10h45 |
| Après-midi   | 13h00 – 16h00 | 14h30 – 14h45 |

Une journée fait 6 h, pauses café comprises : il reste **5 h 30 de contenu par jour**, soit 11 h sur la formation.

**Répartition (hors pauses : 11 h)**

| Type        | Durée   | Part   |
|-------------|---------|--------|
| Pratique (TP) | 8 h 15 | **75 %** |
| Théorie (incident + concept) | 1 h 55 | 17 % |
| Accueil, récapitulatif, quiz, bilans | 50 min | 8 % |

Le programme officiel demande au moins 60 % de pratique.

**Les modules officiels par jour** (la répartition est celle du programme Ascent) :

| Jour | Modules officiels | Durée officielle |
|------|-------------------|------------------|
| 1 – Fondamentaux DevOps et stratégies de test | Introduction et rappel DevOps (1 h 30) + Stratégies de tests (2 h 30) + Culture du test (2 h) | 6 h |
| 2 – Automatisation et déploiement | Automatisation et industrialisation (3 h) + Documentation et maintenance (1 h 30) + Méthodes de déploiement (1 h 30) | 6 h |

La théorie est volontairement courte (10 à 20 min par chapitre) : on présente l'incident, le concept, et on passe tout de suite au TP. Chaque participant travaille seul, sur sa propre machine Ubuntu.

---

## Le fil rouge : l'histoire d'Iroise Traversées

**Iroise Traversées** est une petite compagnie maritime qui relie Brest et Le Conquet aux îles de **Molène** et d'**Ouessant**.
Ses passagers réservent en ligne avec **Embarq**, l'application maison. La saison ouvre le **1er avril**.

L'été dernier s'est mal fini :

- le **14 juillet**, la compagnie a vendu **312 places pour un navire de 280** : 32 passagers sont restés sur le quai du Conquet ;
- le correctif, livré un vendredi soir après deux jours de tests à la main, a **cassé la réservation** pendant tout le week-end.

Embarq (code dans `embarq/`) est une application Java Spring Boot : des pages web pour les passagers, une API REST, une base PostgreSQL.
Avant la formation, la compagnie n'a ni tests automatisés dignes de ce nom, ni pipeline : Tom livre à la main.

```
 passagers ─▶ Embarq (Spring Boot : pages web + API REST) ─▶ PostgreSQL
              tarifs, capacité des navires, réservations
```

Les personnages :

- **Maëlle**, directrice commerciale : « Le 1er avril, on ouvre. Plus jamais un passager laissé sur le quai. »
- **Tom**, développeur : il a écrit Embarq. « Je teste à la main avant de livrer, ça suffit, non ? »
- **Inès**, ingénieure QA, arrivée en janvier : « Je passe deux jours à recliquer tout le site avant chaque livraison. »

**Principe du cours :** chaque chapitre commence par un **incident** chez Iroise Traversées.
On l'analyse ensemble, on présente le **concept** qui y répond, puis on le met en œuvre en **TP** sur Embarq.
Le pipeline monté au TP 00 sert jusqu'au dernier TP : **chaque TP repart de l'état laissé par le précédent**, et chaque TP ajoute un étage au pipeline.

| Ch. | Incident chez Iroise Traversées | Concept | Module officiel | Branche du TP |
|-----|---------------------------------|---------|-----------------|---------------|
| 0 | Le correctif du vendredi a cassé la réservation tout le week-end | CI, CD, infrastructure as code, impact de DevOps sur les tests, anatomie d'un pipeline | Introduction et rappel DevOps | `tp-00-pipeline` |
| 1 | L'école de Molène a payé plein tarif pour ses 10 élèves | Types de tests, pyramide, stratégie de test, tests unitaires (JUnit 5) | Stratégies de tests | `tp-01-strategie` |
| 2 | 312 passagers pour 280 places, et tous les tests sont verts | Tests d'intégration (Testcontainers), tests de performance (k6) | Stratégies de tests | `tp-02-integration` |
| 3 | « On est prêts pour la saison ? » Personne ne sait répondre | Culture qualité, couverture, tests instables, indicateurs, quality gate | Culture du test | `tp-03-indicateurs` |
| 4 | Le tarif « résident des îles » : Tom l'a codé, Maëlle voulait autre chose | Shift-left, BDD, Cucumber, outils (JUnit, Cucumber, Selenium, Jenkins) | Automatisation et industrialisation | `tp-04-bdd` |
| 5 | Deux jours à recliquer le site avant chaque livraison | Selenium WebDriver, Selenium Grid, Page Object, attentes | Automatisation et industrialisation | `tp-05-selenium` |
| 6 | Inès est en congés : la nouvelle page casse tous les tests Selenium, et personne ne sait pourquoi | Documenter les cas de test, documentation vivante, maintenance des tests, environnements | Documentation et maintenance | `tp-06-documentation` |
| 7 | Livrer le 1er avril, sans refaire le vendredi noir | Déploiement continu, préprod et prod, smoke tests, approbation, rollback | Méthodes de déploiement | `tp-07-deploiement` |

**Les bugs de l'été dernier sont dans le code de départ.** Les participants les retrouvent avec les bons tests : le tarif de groupe (TP 01), le surbooking (TP 02), les références de réservation en double (TP 03).

**Les branches :** chaque branche `tp-NN` part de la branche du TP précédent. Elle contient l'énoncé du TP (`tp/NN-*/README.md`) et Embarq dans l'état attendu au début du TP.
La correction du TP NN est dans la même branche (`solutions/NN-*/`), sans rien dévoiler du TP suivant.
Un participant bloqué consulte la correction, ou fait `git checkout tp-NN` pour repartir du bon point de départ.
`solution-finale` contient l'état final d'Embarq et de son pipeline.

**La chaîne CI du lab :** GitLab CI/CD déclenche Jenkins. Chaque participant pousse Embarq dans son propre projet gitlab.com. Un GitLab Runner installé sur sa machine reçoit le job, qui lance le pipeline Jenkins local et attend son résultat. Le cours détaille Jenkins (le `Jenkinsfile`), et GitLab reste la porte d'entrée, comme dans l'environnement réel des participants.

---

## Jour 1 – Fondamentaux DevOps et stratégies de test

**Modules officiels :** Introduction et rappel DevOps (1 h 30) + Stratégies de tests (2 h 30) + Culture du test (2 h) · **Théorie 1 h · Pratique 4 h 05 · Autre 25 min**

| Horaire       | Type | Contenu |
|---------------|------|---------|
| 9h00 – 9h15   | Accueil | Tour de table (rôle, outils de test utilisés, attentes), présentation d'Iroise Traversées |
| 9h15 – 9h30   | Théorie | **Ch. 0 – « Le correctif du vendredi »** · CI, CD (livraison et déploiement continus), infrastructure as code, ce que DevOps change pour les tests, anatomie d'un pipeline |
| 9h30 – 10h30  | **TP 00** | Le lab « as code » (Jenkins et Selenium Grid avec Docker Compose), projet gitlab.com, GitLab Runner, premier pipeline GitLab → Jenkins, étude de cas : analyse du pipeline d'Embarq |
| 10h30 – 10h45 | | *Pause* |
| 10h45 – 11h05 | Théorie | **Ch. 1 – « Plein tarif pour l'école de Molène »** · Types de tests (unitaires, intégration, performance, end-to-end), pyramide des tests, stratégie selon les risques, dette technique, couverture |
| 11h05 – 12h00 | **TP 01** | Stratégie de test d'Embarq (matrice des risques), tests unitaires JUnit 5 du calcul des tarifs, tests paramétrés |
| 12h00 – 13h00 | | *Déjeuner* |
| 13h00 – 13h20 | **TP 01** | Le bug du tarif de groupe : test rouge, correction, test vert, dans le pipeline |
| 13h20 – 13h30 | Théorie | **Ch. 2 – « 312 passagers pour 280 places »** · Ce qu'un mock ne voit pas, tests d'intégration avec une vraie base (Testcontainers), tests de performance |
| 13h30 – 14h05 | **TP 02** | Test d'intégration de la capacité des navires, le bug du surbooking, étage « intégration » du pipeline, test de charge k6 |
| 14h05 – 14h20 | Théorie | **Ch. 3 – « On est prêts pour la saison ? »** · La qualité dès le début, collaboration dev/test/ops, indicateurs (couverture, taux d'échec, durée du pipeline, tests instables, défauts échappés, DORA), quality gate |
| 14h20 – 14h30 | **TP 03** | Mesurer la couverture avec JaCoCo |
| 14h30 – 14h45 | | *Pause* |
| 14h45 – 15h50 | **TP 03** | Couverture dans Jenkins, quality gate, chasse au test instable, tableau des indicateurs d'Embarq |
| 15h50 – 16h00 | Bilan | Quiz du jour 1, « demain chez Iroise Traversées » |

---

## Jour 2 – Automatisation et déploiement

**Modules officiels :** Automatisation et industrialisation (3 h) + Documentation et maintenance (1 h 30) + Méthodes de déploiement (1 h 30) · **Théorie 55 min · Pratique 4 h 10 · Autre 25 min**

| Horaire       | Type | Contenu |
|---------------|------|---------|
| 9h00 – 9h10   | Récap | Récapitulatif du jour 1, état du pipeline d'Embarq |
| 9h10 – 9h30   | Théorie | **Ch. 4 – « Ce n'est pas ce que je voulais »** · Outils et frameworks (JUnit, Cucumber, Selenium, Jenkins), shift-left, BDD, les « trois amigos », Gherkin |
| 9h30 – 10h30  | **TP 04** | Le tarif « résident des îles » : scénarios Gherkin écrits avant le code, étapes Cucumber, implémentation, rapport Cucumber dans Jenkins |
| 10h30 – 10h45 | | *Pause* |
| 10h45 – 10h55 | Théorie | **Ch. 5 – « Deux jours à recliquer »** · Selenium WebDriver, Selenium Grid, Page Object, attentes explicites |
| 10h55 – 12h00 | **TP 05** | Automatisation du scénario de réservation avec Selenium, exécution sur la Grid (on regarde le navigateur travailler), étage « end-to-end » du pipeline |
| 12h00 – 13h00 | | *Déjeuner* |
| 13h00 – 13h15 | Théorie | **Ch. 6 – « Inès est en congés »** · Documenter les cas de test et leur exécution, documentation vivante, sélecteurs stables, configuration par environnement, tests instables |
| 13h15 – 14h20 | **TP 06** | La nouvelle page casse les tests Selenium : diagnostic, sélecteurs `data-testid`, cas de test documentés, documentation vivante publiée par Jenkins |
| 14h20 – 14h30 | Théorie | **Ch. 7 – « Livrer le 1er avril »** · Déploiement continu, préproduction et production, build once deploy many, smoke tests, approbation, rollback |
| 14h30 – 14h45 | | *Pause* |
| 14h45 – 15h45 | **TP 07** | Pipeline de déploiement : image Docker, déploiement en préprod, smoke tests et tests end-to-end en préprod, approbation, production, rollback automatique |
| 15h45 – 16h00 | Bilan | Épilogue (Embarq avant et après), QCM final, évaluations |

---

## Gérer les niveaux différents

- Le public mélange développeurs, testeurs et ingénieurs DevOps. Les TP donnent le code Java à compléter (`TODO`) : un testeur qui code peu s'en sort, un développeur va plus loin avec les **bonus**.
- **Chaque TP** a une partie obligatoire et des bonus pour ceux qui finissent en avance.
- **Rattrapage** : un participant en retard fait `git checkout tp-NN` pour repartir de l'état attendu, puis pousse cette branche dans son projet gitlab.com.

## Préparation du formateur

1. Pousser le dépôt et toutes les branches sur GitHub (dépôt public).
2. Faire préparer une machine Ubuntu 24.04 avec bureau par participant (4 vCPU minimum, 16 Go de RAM, 60 Go de disque, droits administrateur, accès Internet sortant), puis suivre [guide-installation-lab.md](guide-installation-lab.md) sur chaque machine, **la veille** : il installe les outils et télécharge les images Docker.
3. Demander aux participants de créer un compte gratuit sur gitlab.com **avant** la formation (ou prévoir 10 minutes en début de TP 00).
4. Faire une fois la formation complète sur une machine de test, jusqu'au TP 07.

## Correspondance avec le programme officiel

| Jour | Module officiel                                   | Durée officielle | Durée prévue (dont TP) |
|------|---------------------------------------------------|------------------|------------------------|
| 1    | Introduction et rappel DevOps                     | 1 h 30           | 1 h 30 (1 h) : accueil, ch. 0, TP 00 |
| 1    | Stratégies de tests dans un contexte DevOps       | 2 h 30           | 2 h 20 (1 h 50) : ch. 1, TP 01, ch. 2, TP 02 |
| 1    | Introduction à la culture du test                 | 2 h              | 1 h 40 (1 h 15) : ch. 3, TP 03, bilan |
| 2    | Automatisation et industrialisation des tests     | 3 h              | 2 h 45 (2 h 05) : récap, ch. 4, TP 04, ch. 5, TP 05 |
| 2    | Documentation et maintenance des tests            | 1 h 30           | 1 h 20 (1 h 05) : ch. 6, TP 06 |
| 2    | Méthodes de déploiement applicatives              | 1 h 30           | 1 h 25 (1 h) : ch. 7, TP 07, bilan |
| **Total** |                                              | **12 h**         | **11 h hors pauses (8 h 15)** + 1 h de pauses café |

Les 12 h officielles comprennent les quatre pauses café de 15 minutes. Chaque module perd entre 5 et 15 minutes pour les absorber. Les travaux pratiques demandés par le programme sont tous couverts : analyse d'un pipeline (TP 00), stratégie de test (TP 01), indicateurs clés (TP 03), scénario Selenium (TP 05), documentation de test automatisé (TP 06), déploiement intégrant des tests (TP 07).
