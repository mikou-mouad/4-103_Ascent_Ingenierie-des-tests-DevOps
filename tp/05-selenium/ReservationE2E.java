package fr.iroise.embarq.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Le parcours d'un passager, dans un vrai navigateur, sur Embarq déployé. */
class ReservationE2E extends TestEndToEnd {

    @Test
    void un_passager_reserve_une_traversee_en_famille() {
        PageConfirmation confirmation = PageAccueil.ouvrir(navigateur, APP_URL)
                .reserverLaPremiereTraversee()
                .saisir("Le Gall", "legall@example.org", 2, 1, 0)
                .valider();

        assertThat(confirmation.reference()).matches("IR-[A-Z0-9]{8}");
        // TODO : vérifier le statut (« confirmée ») et le montant (2 adultes et 1 enfant)
    }

    @Test
    void le_formulaire_signale_les_champs_mal_remplis() {
        // TODO : ouvrir la page d'accueil, choisir la première traversée, saisir un nom vide
        //        et l'e-mail « pas-un-email » (1 adulte, 0 enfant, 0 vélo), valider sans succès,
        //        et vérifier les deux messages d'erreur.
    }
}
