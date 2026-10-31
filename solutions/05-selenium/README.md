# Correction du TP 05 – Tests end-to-end avec Selenium

Fichiers corrigés, aux mêmes chemins que dans le projet :

- `embarq/src/test/java/fr/iroise/embarq/e2e/PageAccueil.java`, `PageReservation.java`, `PageConfirmation.java` : les Page Objects ;
- `embarq/src/test/java/fr/iroise/embarq/e2e/ReservationE2E.java` : les tests, avec le bonus du Molenez ;
- `embarq/Jenkinsfile` : l'étape « Tests end-to-end ».

Pour comparer avec votre travail : `diff -r solutions/05-selenium/embarq embarq`.

## Étape 1

**Question 1.** Le navigateur ne tourne pas sur votre machine, mais dans le conteneur `selenium`. Pour lui, `localhost` est ce conteneur. Il joint Embarq par le réseau Docker `lab`, où l'application de l'environnement e2e s'appelle `embarq-e2e` et écoute sur le port 8080 (le port 8089 n'existe que sur votre machine).

## Étape 2

Les sélecteurs utilisés :

| Élément | Sélecteur |
|---------|-----------|
| Lien « Réserver » | `By.cssSelector("a[id^='reserver-']")` (le premier de la liste) |
| Champs | `By.id("nom")`, `email`, `adultes`, `enfants`, `velos` |
| Bouton | `By.id("valider")` |
| Confirmation | `By.id("reference")`, `montant`, `statut` |
| Erreurs | `By.cssSelector(".erreur, .erreur-champ")` |

**Question 2.** `Thread.sleep` attend toujours la durée fixée : trop court sur un lab chargé (le test échoue au hasard, il devient instable), trop long le reste du temps (le pipeline ralentit). Une attente explicite rend la main dès que la condition est vraie, et échoue avec un message clair au bout du délai maximum.

## Étape 3

```java
@Test
void un_passager_reserve_une_traversee_en_famille() {
    PageConfirmation confirmation = PageAccueil.ouvrir(navigateur, APP_URL)
            .reserverLaPremiereTraversee()
            .saisir("Le Gall", "legall@example.org", 2, 1, 0)
            .valider();

    assertThat(confirmation.reference()).matches("IR-[A-Z0-9]{8}");
    assertThat(confirmation.statut()).isEqualTo("confirmée");
    assertThat(confirmation.montant()).isEqualTo("90,00 €");
}

@Test
void le_formulaire_signale_les_champs_mal_remplis() {
    PageReservation formulaire = PageAccueil.ouvrir(navigateur, APP_URL)
            .reserverLaPremiereTraversee()
            .saisir("", "pas-un-email", 1, 0, 0)
            .validerSansSucces();

    assertThat(formulaire.erreurs()).contains("Le nom est obligatoire", "Adresse e-mail invalide");
}
```

**Question 3.** Le champ « Adultes » a l'attribut HTML `min="1"`. Avec 0, c'est **le navigateur** qui bloque l'envoi du formulaire et affiche une bulle : la requête ne part pas, le serveur ne répond rien, et le test attend en vain un message d'erreur de la page. Un test end-to-end voit ce que voit le passager, y compris les contrôles du navigateur. La règle « au moins un adulte » est vérifiée aussi côté serveur (`@Min(1)`), et testée par `ApiIT`.

**Question 4.** Un test end-to-end prend 2 à 5 secondes (ouvrir un navigateur, charger des pages), un test unitaire quelques millisecondes : un facteur 1 000. On garde les tests end-to-end pour les **parcours critiques** (réserver, voir une erreur), et on teste les règles en dessous (tarifs en unitaire et en Cucumber, capacité en intégration). C'est le sommet étroit de la pyramide.

## Étape 4

**Question 5.** Oui : l'étape « Image Docker » construit `embarq:1.0.<numéro du build>` une seule fois, à partir du jar testé. L'environnement e2e déploie cette image, et c'est elle qui partira en préproduction et en production (TP 07). On teste l'artefact qu'on livre, et pas une recompilation (« build once, deploy many »).

**Question 6.** Le bloc `post { always { … } }` du pipeline le supprime, base comprise (`down -v`), que le build réussisse ou échoue. Chaque build part d'un environnement neuf, avec des données connues : les tests ne dépendent pas des réservations laissées par le build précédent.

## Questions de fin

1. La Grid sépare les navigateurs de Jenkins : on peut en avoir plusieurs (versions, navigateurs, systèmes), en parallèle, sans rien installer sur Jenkins. Les tests se connectent à une seule adresse, et la Grid répartit les sessions.
2. Ajouter un nœud Firefox à la Grid (image `selenium/node-firefox` ou `selenium/standalone-firefox`), et paramétrer le navigateur dans `TestEndToEnd` (`FirefoxOptions` au lieu de `ChromeOptions`), par exemple avec une propriété `-Dnavigateur=firefox`, puis lancer les tests pour chaque valeur.
3. Seul `PageReservation` change : une ligne. Les tests, qui ne connaissent que `valider()`, ne bougent pas. Sans Page Object, le sélecteur serait recopié dans chaque test.

## Bonus

- `le_molenez_refuse_plus_de_passagers_que_de_places` et `PageAccueil.reserverSur` sont dans la correction. Le message dépend des réservations déjà faites : « Il ne reste que 12 place(s) » ou « Cette traversée est complète », d'où `containsAnyOf("place", "complète")`.
- Tarif résident : ajouter à `PageReservation` une méthode `commeResident()` qui coche `By.id("resident")`, puis vérifier « 18,00 € » pour 2 adultes résidents.
- Capture d'écran : dans un `@AfterEach` qui reçoit l'état du test, ou plus simplement avec une extension JUnit `TestWatcher` ; enregistrer le fichier dans `target/captures/`, puis `archiveArtifacts artifacts: 'embarq/target/captures/*.png', allowEmptyArchive: true` dans le `post` de l'étape.
