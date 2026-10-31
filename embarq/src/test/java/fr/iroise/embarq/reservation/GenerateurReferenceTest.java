package fr.iroise.embarq.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class GenerateurReferenceTest {

    private final GenerateurReference generateur = new GenerateurReference();

    @Test
    void la_reference_a_le_format_IR_et_trois_chiffres() {
        assertThat(generateur.nouvelleReference()).matches("IR-\\d{3}");
    }

    @Test
    void deux_reservations_n_ont_pas_la_meme_reference() {
        List<String> references = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            references.add(generateur.nouvelleReference());
        }
        assertThat(references).doesNotHaveDuplicates();
    }
}
