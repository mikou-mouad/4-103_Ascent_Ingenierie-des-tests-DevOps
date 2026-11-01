# TP 03 – Indicateurs qualité et quality gate

**Durée :** 1 h 15 (14h20 – 14h30, pause, puis 14h45 – 15h50) · **Branche :** `tp-03-indicateurs` · **Correction :** `solutions/03-indicateurs/`

## L'incident

> Lundi, comité de direction. Maëlle demande : « Le 1er avril, on est prêts ? »
>
> **Tom :** « Tous les tests sont verts. Enfin, presque toujours. »
> **Inès :** « Verts, mais qu'est-ce qu'ils couvrent ? Et "presque toujours", c'est combien ? »
> **Maëlle :** « Je ne vous demande pas d'avoir raison. Je vous demande des chiffres. »

## Objectifs

- Mesurer la couverture des tests avec JaCoCo, et savoir ce qu'elle dit et ce qu'elle ne dit pas.
- Mettre une quality gate dans le pipeline : la couverture ne doit pas baisser.
- Démasquer un test instable, et comprendre ce qu'il cachait.
- Choisir les indicateurs qualité d'Embarq, avec une cible et quelqu'un qui les regarde.

## Point de départ

```bash
cd ~/iroise && git fetch
git stash -u
git checkout tp-03-indicateurs
git push -u gitlab HEAD     # -u : la branche suit votre projet GitLab, pas le dépôt du cours
```

> **N'oubliez pas le `-u`** sur ce premier push. Sans lui, la nouvelle branche suit le dépôt GitHub du cours, et les boutons **Sync** et **Push** de VS Code partent au mauvais endroit. Vérifiez avec `git branch -vv` : la branche courante doit afficher `[gitlab/tp-03-indicateurs]`.

## Étape 1 – Mesurer la couverture (10 min, avant la pause)

Dans `embarq/pom.xml`, ajoutez ce plugin à la fin de la section `<plugins>`, juste avant `</plugins>` :

```xml
<!-- Couverture des tests : agent JaCoCo pendant les tests, rapport et seuil à l'étape verify -->
<plugin>
  <groupId>org.jacoco</groupId>
  <artifactId>jacoco-maven-plugin</artifactId>
  <version>0.8.16</version>
  <executions>
    <execution>
      <id>agent</id>
      <goals>
        <goal>prepare-agent</goal>
      </goals>
    </execution>
    <execution>
      <id>rapport</id>
      <phase>verify</phase>
      <goals>
        <goal>report</goal>
      </goals>
    </execution>
    <execution>
      <id>seuil</id>
      <goals>
        <goal>check</goal>
      </goals>
      <configuration>
        <rules>
          <rule>
            <element>BUNDLE</element>
            <limits>
              <limit>
                <counter>LINE</counter>
                <value>COVEREDRATIO</value>
                <minimum>0.60</minimum>
              </limit>
            </limits>
          </rule>
        </rules>
      </configuration>
    </execution>
  </executions>
</plugin>
```

```bash
cd ~/iroise/embarq
mvn -B clean verify
```

Le `clean` est indispensable : JaCoCo **ajoute** ses mesures au fichier `target/jacoco.exec` au lieu de le remplacer. Sans `clean`, les mesures d'un build précédent (d'une autre version du code) se mélangent aux nouvelles, la couverture est fausse, et Maven affiche `Execution data for class … does not match`. Pour la même raison, le `Jenkinsfile` commence par `mvn -B clean compile`.

**Question 1.** Le build échoue. Quel message donne JaCoCo ? Quelle est la couverture des lignes d'Embarq ?

## Étape 2 – Lire le rapport (10 min)

Ouvrez le rapport dans Firefox :

```bash
firefox target/site/jacoco/index.html &
```

Descendez dans les paquetages, jusqu'au code coloré : vert = exécuté, rouge = jamais exécuté, jaune = condition partiellement testée.

**Question 2.** Quel paquetage est à 0 % ? Qu'est-ce que cela veut dire pour le formulaire de réservation ?

**Question 3.** `TarifService` est à 100 %. Au TP 01, avant votre test paramétré, l'était-il déjà ? Le bug du tarif de groupe y était-il quand même ?

**Le seuil.** Avec 60 % de couverture minimale, le build échoue : Embarq n'est couvert qu'à 49 %. Deux options :

- écrire des tests tout de suite, jusqu'à dépasser 60 % ;
- ou fixer le seuil **au niveau actuel**, pour que la couverture **ne baisse plus**, puis le remonter à chaque nouveau test. C'est un « cliquet ».

Choisissez la seconde option :

1. Ouvrez `embarq/pom.xml` dans votre éditeur.
2. Dans le plugin `jacoco-maven-plugin` que vous venez d'ajouter, trouvez l'exécution `seuil`, puis la ligne `<minimum>` :

   ```xml
   <execution>
     <id>seuil</id>
     …
             <minimum>0.60</minimum>
   ```

3. Remplacez `0.60` par `0.45` (45 % des lignes), puis enregistrez le fichier.
4. Relancez la construction complète :

   ```bash
   cd ~/iroise/embarq
   mvn -B clean verify
   ```

5. Vérifiez la fin de la sortie : `All coverage checks have been met.`, puis `BUILD SUCCESS`.

Si le build échoue encore, relisez la valeur dans `pom.xml` (un point, pas une virgule : `0.45`). Le seuil est maintenant dans le `pom.xml` : il s'appliquera aussi dans Jenkins, dès que vous pousserez à l'étape 3.

## Étape 3 – La couverture dans le pipeline (10 min)

Dans `embarq/Jenkinsfile`, à l'étape « Tests d'intégration », ajoutez la publication de la couverture dans le bloc `always`, sous la ligne `junit` :

```groovy
// Couverture des tests unitaires et d'intégration (le seuil est vérifié par Maven : jacoco:check)
recordCoverage tools: [[parser: 'JACOCO', pattern: 'embarq/target/site/jacoco/jacoco.xml']]
```

```bash
cd ~/iroise
git add embarq
git commit -m "Couverture JaCoCo et seuil minimal"
git push gitlab HEAD
```

Attendez la fin du pipeline (job vert dans GitLab), puis regardez la couverture dans Jenkins.

> **Si le build est rouge sur `GenerateurReferenceTest`**, ne cherchez pas la couverture : le pipeline s'est arrêté aux tests unitaires, avant l'étape qui la publie, et le lien **Coverage** n'existe pas dans ce build. C'est le test instable de l'étape 4. Relancez le pipeline sans rien changer (GitLab : **Build** > **Pipelines** > **Retry** sur le job `jenkins`), et notez cet échec : vous en aurez besoin à l'étape 4.


1. Ouvrez la page du job : http://localhost:8080/job/embarq/
2. En bas à gauche, dans **Builds** (l'historique), cliquez sur le numéro du dernier build (`#12`, par exemple).
3. La page du build affiche déjà un résumé, sous **Coverage** > **Whole project** : **Line Coverage** (environ 49 %), **Branch Coverage**, **Lines of Code** et **Cyclomatic Complexity**. C'est le même chiffre que le rapport local.
4. Pour le détail, cliquez sur **Coverage** dans le menu de gauche, sous **Tests**. La page montre la couverture par métrique, puis l'arbre des paquetages : cliquez sur `fr.iroise.embarq.web` pour retrouver les 0 %, puis sur un fichier pour voir son code coloré, comme dans le rapport local.
5. Revenez sur la page du job (lien **embarq** en haut de la page). Un graphique **Coverage** montre l'évolution de la couverture d'un build à l'autre. Il n'apparaît qu'à partir de **deux** builds avec couverture : il se remplira au prochain push (étape 4).

**Question 4.** Si un développeur ajoute du code sans test, que se passe-t-il dans le pipeline ? À quelle étape ?

## Étape 4 – Le test instable (20 min)

Au TP 00, certains pipelines ont échoué sur `GenerateurReferenceTest` sans aucun changement de code. Lancez ce test 20 fois de suite :

```bash
cd ~/iroise/embarq
for i in $(seq 20); do mvn -q -o test -Dtest=GenerateurReferenceTest > /dev/null 2>&1 && echo "réussi" || echo "ÉCHEC"; done | sort | uniq -c
```

**Question 5.** Combien d'échecs sur 20 ? Lisez le test et `GenerateurReference`. Pourquoi le test échoue-t-il parfois ?

**Question 6.** Est-ce le test qui est mauvais, ou le code ? Que se passera-t-il en production après quelques centaines de réservations ? (Indice : `V1__schema.sql`, colonne `reference`.)

Corrigez `GenerateurReference` : une référence de la forme `IR-` suivie de 8 caractères tirés de l'alphabet `ABCDEFGHJKMNPQRSTUVWXYZ23456789` (sans 0, O, 1, I ni L, qu'on confond au téléphone). Utilisez `java.security.SecureRandom`. Adaptez les deux tests : le format, et 1 000 références toutes différentes. Relancez la boucle : 20 réussites.

Il y a **deux** fichiers à modifier : `src/main/java/…/reservation/GenerateurReference.java` (le code) et `src/test/java/…/reservation/GenerateurReferenceTest.java` (le test). Avec le nouveau test seul, la boucle donne 20 échecs sur 20 : le test attend des références sur 8 caractères que l'ancien code ne fabrique pas. C'est normal, c'est un test rouge qui attend la correction du code. Si vous reprenez la correction (`solutions/03-indicateurs/`), copiez les deux fichiers.

```bash
cd ~/iroise
git add embarq
git commit -m "Références de réservation sur 8 caractères : fin du test instable"
git push gitlab HEAD
```

## Étape 5 – Les indicateurs d'Embarq (20 min)

Le script `embarq/ci/indicateurs.sh` calcule des indicateurs à partir de l'API de Jenkins. Lancez-le avec votre jeton d'API Jenkins (celui du TP 00) :

```bash
cd ~/iroise/embarq
JENKINS_USER=admin JENKINS_TOKEN=<votre-jeton> bash ci/indicateurs.sh
```

Copiez le modèle, puis complétez-le :

```bash
cp ~/iroise/tp/03-indicateurs/indicateurs.md ~/iroise/embarq/docs/
```

Pour chaque indicateur : ce qu'il mesure, sa valeur aujourd'hui (le script, Jenkins, l'histoire d'Iroise Traversées), une cible, la source, et qui le regarde.

**Question 7.** Quels indicateurs du tableau sont des indicateurs « DORA » ? Pourquoi Maëlle s'y intéresserait-elle plus qu'à la couverture ?

**Question 8.** Citez un indicateur qui ferait plus de mal que de bien s'il devenait un objectif individuel.

## Questions de fin

1. Que dit une couverture de 80 % ? Que ne dit-elle pas ?
2. Qu'est-ce qu'une quality gate ? Citez deux autres critères qu'on pourrait y mettre.
3. Un test instable est relancé jusqu'à passer. Quels sont les deux risques ?

## Bonus

- Écrivez `embarq/src/test/java/fr/iroise/embarq/ApiIT.java` : un test d'intégration de l'application complète (`@SpringBootTest(webEnvironment = RANDOM_PORT)`, un PostgreSQL Testcontainers comme au TP 02) qui appelle l'API avec un `RestClient` : liste des traversées, tarif, réservation, réservation trop grande (409). Mesurez la couverture, puis remontez le seuil.
- Ajoutez au seuil JaCoCo une règle sur les branches (`<counter>BRANCH</counter>`).
