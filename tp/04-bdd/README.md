# TP 04 – Shift-left et BDD avec Cucumber

**Durée :** 1 h (9h30 – 10h30) · **Branche :** `tp-04-bdd` · **Correction :** `solutions/04-bdd/`

## L'incident

> Pendant l'hiver, Maëlle a obtenu du Département un tarif pour les habitants des îles. Elle a appelé Tom, qui l'a codé dans la journée : le tarif résident est à moitié prix, et une case à cocher est apparue dans le formulaire.
>
> Inès l'a testé la veille de la livraison, en relisant le courrier du Département. Rien ne correspondait.
>
> **Maëlle :** « Ce n'est pas ce que je voulais. »
> **Tom :** « C'est ce que j'ai compris. »
> **Inès :** « Et moi, je l'ai découvert à la fin. »

## Objectifs

- Écrire les règles du tarif résident en Gherkin, avec Maëlle, **avant** de modifier le code (shift-left, BDD).
- Relier les phrases Gherkin à du code Java avec Cucumber.
- Corriger le tarif jusqu'à ce que les scénarios passent.
- Publier les scénarios dans Jenkins : une documentation que Maëlle peut lire.

## Point de départ

```bash
cd ~/iroise && git fetch
git stash -u
git checkout tp-04-bdd
git push gitlab HEAD
```

Le code de Tom est dans la branche : `calculer(adultes, enfants, velos, resident)` dans `TarifService`, la case « Résident des îles » dans le formulaire. Ouvrez http://localhost:8090 après avoir redéployé (`mvn -B -q package -DskipTests && docker build -t embarq:local . && bash deploy/deployer.sh dev` dans `embarq/`) si vous voulez la voir.

## L'e-mail de Maëlle

> **De :** Maëlle
> **Objet :** Tarif résident, ce que dit la convention avec le Département
>
> Bonjour à tous,
>
> Voici la règle, telle qu'elle est écrite dans la convention :
>
> - un adulte résident de Molène ou d'Ouessant paie **9 €** la traversée ;
> - les enfants résidents voyagent **gratuitement** ;
> - les vélos restent au **plein tarif**, 17 € ;
> - la remise de groupe ne s'ajoute **pas** au tarif résident, qui est déjà très réduit.
>
> Le tarif s'applique à toute la réservation : les résidents réservent entre eux.
>
> Maëlle

## Étape 1 – Les « trois amigos » (10 min)

Avant d'écrire une ligne de code, Maëlle (le métier), Tom (le développement) et Inès (le test) se mettent d'accord sur des **exemples**.

**Question 1.** Pour chaque phrase de l'e-mail, écrivez un exemple chiffré : nombre d'adultes, d'enfants, de vélos, et le prix attendu.

**Question 2.** Avec la version de Tom (moitié prix sur tout), combien paient 10 adultes résidents ? Et 2 adultes avec 2 vélos ? Comparez avec vos exemples.

## Étape 2 – Les scénarios en Gherkin (15 min)

Copiez le fichier de scénarios et les étapes Java dans le projet :

```bash
cd ~/iroise
mkdir -p embarq/src/test/resources/features
cp tp/04-bdd/tarifs.feature embarq/src/test/resources/features/
cp tp/04-bdd/TarifsSteps.java embarq/src/test/java/fr/iroise/embarq/bdd/
```

Ouvrez `tarifs.feature`. Il est écrit en français (`# language: fr`) :

- `Fonctionnalité`, `Règle`, `Scénario`, `Plan du scénario` et `Exemples` structurent les règles ;
- `Étant donné`, `Quand`, `Alors` décrivent le contexte, l'action, le résultat attendu ;
- le `Contexte` d'une règle s'applique à tous ses scénarios.

Sous `Règle: Tarif résident des îles`, ajoutez un scénario par phrase de l'e-mail, à partir de vos exemples de l'étape 1. Utilisez seulement les phrases déjà présentes dans le fichier.

## Étape 3 – Relier les phrases au code (10 min)

Ouvrez `TarifsSteps.java`. Chaque méthode est liée à une phrase par une **expression Cucumber** : `{int}` capture un nombre, `{bigdecimal}` un montant (avec une virgule, puisque le fichier est en français), `adulte(s)` accepte « adulte » et « adultes ».

Complétez les trois `TODO`, puis lancez les tests :

```bash
cd ~/iroise/embarq
mvn -B test
```

Les scénarios sont lancés avec les tests unitaires, par `CucumberTest`.

**Question 3.** Quels scénarios échouent ? Les messages d'échec sont-ils compréhensibles par Maëlle ?

Si Cucumber écrit « The step … is undefined », la phrase du scénario ne correspond à aucune expression : comparez-les mot à mot.

## Étape 4 – Corriger le tarif (10 min)

Corrigez `calculer(adultes, enfants, velos, resident)` dans `TarifService` : si les passagers ne sont pas résidents, rien ne change ; sinon, 9 € par adulte, 0 € par enfant, 17 € par vélo, sans remise de groupe. Ajoutez le tarif résident au commentaire de la classe.

Relancez `mvn -B test` jusqu'à ce que tous les scénarios passent. Ouvrez le rapport :

```bash
firefox target/cucumber/rapport.html &
```

## Étape 5 – Les scénarios dans le pipeline (10 min)

Dans `embarq/Jenkinsfile`, à l'étape « Tests unitaires », publiez le rapport Cucumber dans le bloc `always`, sous la ligne `junit` :

```groovy
// Les scénarios Cucumber, lisibles par Maëlle : la documentation vivante des règles métier
publishHTML target: [
    reportName : 'Scénarios Cucumber',
    reportDir  : 'embarq/target/cucumber',
    reportFiles: 'rapport.html',
    keepAll    : true,
    alwaysLinkToLastBuild: true,
    allowMissing: true
]
```

```bash
cd ~/iroise
git add embarq
git commit -m "Tarif résident des îles décrit en scénarios Cucumber, et corrigé"
git push gitlab HEAD
```

Dans Jenkins, la page du job affiche un lien **Scénarios Cucumber**. Les scénarios apparaissent aussi dans **Tests**, avec les tests unitaires.

**Question 4.** Qu'est-ce que l'approche « shift-left » ? À quel moment de l'histoire du tarif résident aurait-on dû écrire ces scénarios ?

## Questions de fin

1. Quelle différence entre un test JUnit paramétré (TP 01) et un scénario Cucumber ? Pour qui écrit-on chacun ?
2. Cucumber est-il un outil de test d'interface ? À quel niveau de la pyramide se trouvent vos scénarios ?
3. Placez JUnit, Cucumber, Selenium et Jenkins dans une chaîne de test : qui fait quoi ?

## Bonus

- Ajoutez une phrase « Alors aucun billet n'est payant » pour un scénario à 0 adulte résident et 3 enfants. Qu'en dit la règle « au moins un adulte par réservation » ? Où faut-il la tester ?
- Utilisez un `Plan du scénario` pour les cas résidents, comme pour le tarif normal.
- Ajoutez un tag `@resident` aux scénarios résidents, et lancez-les seuls : `mvn -B test -Dtest=CucumberTest -Dcucumber.filter.tags=@resident`.
