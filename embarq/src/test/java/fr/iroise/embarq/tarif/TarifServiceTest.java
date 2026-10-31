package fr.iroise.embarq.tarif;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

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
}
