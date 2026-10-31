package fr.iroise.embarq.tarif;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TarifServiceTest {

    private final TarifService tarifs = new TarifService();

    @Test
    void un_adulte_paie_le_tarif_adulte() {
        assertThat(tarifs.calculer(1, 0, 0)).isEqualByComparingTo("36.00");
    }

    @Test
    void une_famille_avec_un_enfant() {
        assertThat(tarifs.calculer(2, 1, 0)).isEqualByComparingTo("90.00");
    }

    @Test
    void un_enfant_paie_le_tarif_enfant() {
        assertThat(tarifs.calculer(0, 1, 0)).isEqualByComparingTo("18.00");
    }

    @Test
    void un_velo_s_ajoute_au_billet() {
        assertThat(tarifs.calculer(1, 0, 1)).isEqualByComparingTo("53.00");
    }

    @Test
    void la_remise_de_groupe_ne_s_applique_pas_aux_velos() {
        // 10 adultes : 360 € - 10 % = 324 € ; 10 vélos plein tarif : 170 €
        assertThat(tarifs.calculer(10, 0, 10)).isEqualByComparingTo("494.00");
    }

    @ParameterizedTest(name = "{0} adulte(s), {1} enfant(s), {2} vélo(s) : {3} €")
    @CsvSource({
            "1, 0, 0,  36.00",
            "2, 1, 0,  90.00",
            // Juste sous le seuil de groupe : pas de remise
            "9, 0, 0, 324.00",
            "1, 8, 0, 180.00",
            // Pile au seuil : remise de 10 % (l'école de Molène)
            "10, 0, 0, 324.00",
            "2, 8, 0, 194.40",
            // Au-dessus du seuil
            "11, 0, 0, 356.40",
            "3, 8, 0, 226.80",
            "11, 0, 2, 390.40",
    })
    void calcule_le_prix_selon_la_grille(int adultes, int enfants, int velos, String prixAttendu) {
        assertThat(tarifs.calculer(adultes, enfants, velos)).isEqualByComparingTo(prixAttendu);
    }
}
