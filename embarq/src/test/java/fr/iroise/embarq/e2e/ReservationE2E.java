package fr.iroise.embarq.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Le parcours d'un passager, dans un vrai navigateur, sur Embarq déployé.
 * Cas de test documentés dans docs/cas-de-test.md (CT-E2E-01 à CT-E2E-03).
 */
@DisplayName("Parcours de réservation (navigateur)")
class ReservationE2E extends TestEndToEnd {

    @Test
    @DisplayName("CT-E2E-01 · Une famille réserve une traversée et reçoit sa référence")
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
    @DisplayName("CT-E2E-02 · Le formulaire signale le nom manquant et l'e-mail invalide")
    void le_formulaire_signale_les_champs_mal_remplis() {
        PageReservation formulaire = PageAccueil.ouvrir(navigateur, APP_URL)
                .reserverLaPremiereTraversee()
                .saisir("", "pas-un-email", 1, 0, 0)
                .validerSansSucces();

        assertThat(formulaire.erreurs()).contains("Le nom est obligatoire", "Adresse e-mail invalide");
    }

    @Test
    @DisplayName("CT-E2E-03 · Le Molenez refuse plus de passagers que de places")
    void le_molenez_refuse_plus_de_passagers_que_de_places() {
        PageReservation formulaire = PageAccueil.ouvrir(navigateur, APP_URL)
                .reserverSur("Molenez")
                .saisir("École de Molène", "ecole@example.org", 13, 0, 0)
                .validerSansSucces();

        assertThat(formulaire.erreurs()).anySatisfy(erreur -> assertThat(erreur).containsAnyOf("place", "complète"));
    }
}
