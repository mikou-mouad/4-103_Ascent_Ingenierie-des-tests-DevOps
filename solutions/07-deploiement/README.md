# Correction du TP 07 – Déploiement continu avec des tests à chaque étape

Fichiers corrigés, aux mêmes chemins que dans le projet :

- `embarq/deploy/smoke-test.sh` : les smoke tests complets ;
- `embarq/Jenkinsfile` : préproduction, approbation, production et rollback ;
- `embarq/src/main/resources/application-prod.properties` : la configuration de production, corrigée.

Pour comparer avec votre travail : `diff -r solutions/07-deploiement/embarq embarq`.

## Étape 1

Les deux contrôles à compléter :

```bash
# 2. C'est bien la version qu'on vient de déployer
DEPLOYEE=$(curl -fs -m 5 "$URL/actuator/info" | jq -r '.version') || echec "version : /actuator/info ne répond pas"
[ "$DEPLOYEE" = "$VERSION" ] || echec "version : $DEPLOYEE au lieu de $VERSION"
ok "version : $DEPLOYEE"

# 4. L'API liste des traversées
NOMBRE=$(curl -fs -m 5 "$URL/api/traversees" | jq 'length') || echec "API : /api/traversees ne répond pas"
[ "$NOMBRE" -gt 0 ] || echec "API : aucune traversée"
ok "API : $NOMBRE traversées"
```

Remarquez que chaque réponse est lue entière avant d'être vérifiée. Avec `set -o pipefail`, `curl … | grep -q …` échoue **au hasard** : `grep -q` s'arrête dès qu'il a trouvé, et `curl`, qui écrit encore, reçoit un signal SIGPIPE et sort en erreur. Un smoke test instable serait pire que pas de smoke test.

**Question 1.** Un déploiement peut « réussir » sans changer de version : image mal taguée, conteneur pas recréé, mauvais serveur. Vérifier la version prouve que ce qu'on a testé est bien ce qui tourne. L'été dernier, personne ne savait quelle version tournait après le jar copié à la main.

## Étape 2

**Question 2.** Le job GitLab reste en cours : sa console recopie celle de Jenkins, qui affiche `Mettre Embarq 1.0.N en production ?` et attend. Tout utilisateur connecté à Jenkins peut approuver ; en vrai, on limite l'approbation à certaines personnes (`submitter: 'maelle,tom'` dans `input`). Sans réponse en 15 minutes, le build est abandonné.

**Question 3.** Une seule fois, à l'étape « Image Docker ». La même image a tourné en e2e, en préproduction et en production. Ce qui part en production est exactement ce qui a été testé, à la configuration près (« build once, deploy many »). Reconstruire pour chaque environnement, c'est risquer de livrer autre chose que ce qu'on a testé.

## Étape 3

**Question 4.** Les tests unitaires, d'intégration et end-to-end ne chargent pas le profil `prod`, et la préproduction tourne avec le profil `preprod` : aucun n'a lu `application-prod.properties`. Le build échoue à l'étape « Production » : l'application ne démarre pas, `deployer.sh` attend 2 minutes en vain et affiche les logs :

```
APPLICATION FAILED TO START
Failed to bind properties under 'spring.datasource.hikari.maximum-pool-size' to int:
    Value: "2O"
    Reason: failed to convert java.lang.String to int
```

C'est la lettre O à la place du zéro.

**Question 5.** La version précédente : le bloc `catch` a redéployé l'image d'avant, et ses smoke tests sont passés. `/actuator/info` affiche son numéro. Les passagers ont perdu la production pendant environ deux minutes (le temps de constater l'échec et de revenir en arrière), pas un week-end. Le build est rouge, GitLab aussi : l'équipe sait que la nouvelle version n'est pas partie.

**Question 6.** Une préproduction **identique** à la production : même profil de configuration (seules les adresses et les secrets diffèrent, passés par des variables d'environnement), mêmes réglages. Et un test qui démarre l'application avec le profil `prod` dans le pipeline (bonus) : l'erreur aurait été vue en une minute, dès les tests d'intégration.

## Étape 4

**Question 7.** Exemple de valeurs à la fin du TP : 2 ou 3 mises en production réussies dans l'heure (fréquence), 1 échec sur 3 ou 4 tentatives (taux d'échec des changements), rétablissement en quelques minutes grâce au rollback automatique. À comparer avec l'été dernier : 4 livraisons par an, 1 sur 4 en échec, un week-end pour revenir.

## Questions de fin

1. **Livraison continue** : chaque version validée par le pipeline est prête, la mise en production attend un clic (l'approbation). Pour passer au **déploiement continu**, on retire l'approbation : il faut alors une confiance totale dans les tests et le rollback, une surveillance de la production, et des changements petits et fréquents.
2. **Blue/green** : deux productions identiques, on bascule le trafic de l'une à l'autre, et on rebascule en cas de problème. **Canary** : la nouvelle version reçoit d'abord une petite part du trafic, puis tout si les indicateurs restent bons. **Feature flags** : le code part désactivé, et on active la fonction à part.
3. Avant la production : unitaires, Cucumber, intégration, couverture, end-to-end, smoke tests en préproduction (et le test de charge en bonus). Après : smoke tests en production. En continu : une **surveillance synthétique** (un smoke test lancé toutes les minutes sur la production), les alertes sur les erreurs et les temps de réponse, les sondes de santé (`/actuator/health`).

## Bonus

- Test de charge en préproduction : une étape après les smoke tests, avec la commande k6. k6 sort en erreur si un seuil n'est pas tenu : le pipeline s'arrête avant l'approbation.
- Test du profil `prod` :

  ```java
  @SpringBootTest
  @ActiveProfiles("prod")
  @Testcontainers
  class ProfilProductionIT {

      @Container
      @ServiceConnection
      static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

      @Test
      void l_application_demarre_avec_la_configuration_de_production() {
      }
  }
  ```

  Avec la coquille, le contexte Spring ne démarre pas : le test échoue à l'étape « Tests d'intégration ».
- `when { expression { params.BRANCHE == 'main' } }` sur les étapes « Approbation » et « Production » : les branches de travail s'arrêtent en préproduction.
