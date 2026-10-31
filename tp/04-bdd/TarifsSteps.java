package fr.iroise.embarq.bdd;

import static org.assertj.core.api.Assertions.assertThat;

import fr.iroise.embarq.tarif.TarifService;
import io.cucumber.java.fr.Alors;
import io.cucumber.java.fr.Etantdonné;
import io.cucumber.java.fr.Quand;
import java.math.BigDecimal;

/**
 * Les étapes des scénarios de tarifs.fr : chaque phrase Gherkin est reliée à une méthode Java.
 * Cucumber crée une nouvelle instance de cette classe pour chaque scénario.
 */
public class TarifsSteps {

    private final TarifService tarifs = new TarifService();
    private boolean resident = false;
    private BigDecimal prix;

    @Etantdonné("des passagers résidents des îles")
    public void des_passagers_residents_des_iles() {
        // TODO : retenir que les passagers sont résidents
    }

    @Quand("je réserve pour {int} adulte(s), {int} enfant(s) et {int} vélo(s)")
    public void je_reserve_pour(int adultes, int enfants, int velos) {
        // TODO : calculer le prix avec TarifService, en tenant compte de « resident »
    }

    @Alors("le prix est de {bigdecimal} €")
    public void le_prix_est_de(BigDecimal attendu) {
        // TODO : vérifier le prix calculé (isEqualByComparingTo)
    }
}
