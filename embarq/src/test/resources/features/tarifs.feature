# language: fr
Fonctionnalité: Prix d'une traversée
  Pour payer le juste prix
  En tant que passager d'Iroise Traversées
  Je veux qu'Embarq applique la grille tarifaire de la compagnie

  Règle: Tarif normal : adulte 36 €, enfant 18 €, vélo 17 €, 10 % de remise à partir de 10 passagers

    Plan du scénario: Prix selon les passagers et les vélos
      Quand je réserve pour <adultes> adultes, <enfants> enfants et <velos> vélos
      Alors le prix est de <prix> €

      Exemples:
        | adultes | enfants | velos | prix   |
        | 1       | 0       | 0     | 36,00  |
        | 2       | 1       | 1     | 107,00 |
        | 2       | 8       | 0     | 194,40 |

  Règle: Tarif résident des îles

    Contexte:
      Étant donné des passagers résidents des îles

    Scénario: Un adulte résident paie 9 €
      Quand je réserve pour 1 adulte, 0 enfant et 0 vélo
      Alors le prix est de 9,00 €

    Scénario: Les enfants résidents voyagent gratuitement
      Quand je réserve pour 1 adulte, 2 enfants et 0 vélo
      Alors le prix est de 9,00 €

    Scénario: Les vélos des résidents paient plein tarif
      Quand je réserve pour 2 adultes, 0 enfant et 2 vélos
      Alors le prix est de 52,00 €

    Scénario: Pas de remise de groupe pour les résidents
      Quand je réserve pour 10 adultes, 0 enfant et 0 vélo
      Alors le prix est de 90,00 €
