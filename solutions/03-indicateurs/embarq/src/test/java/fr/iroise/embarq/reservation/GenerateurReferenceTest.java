package fr.iroise.embarq.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class GenerateurReferenceTest {

    private final GenerateurReference generateur = new GenerateurReference();

    @Test
    void la_reference_a_le_format_IR_et_huit_caracteres_lisibles() {
        assertThat(generateur.nouvelleReference()).matches("IR-[A-HJKMNP-Z2-9]{8}");
    }

    @Test
    void mille_reservations_n_ont_jamais_la_meme_reference() {
        // Probabilité d'une collision parmi 1 000 références : moins d'une sur un million
        Set<String> references = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            references.add(generateur.nouvelleReference());
        }
        assertThat(references).hasSize(1000);
    }
}
