package fr.iroise.embarq.reservation;

import java.util.Random;
import org.springframework.stereotype.Component;

/** Fabrique la référence donnée au passager, courte pour être lue au téléphone. */
@Component
public class GenerateurReference {

    private final Random random = new Random();

    public String nouvelleReference() {
        return String.format("IR-%03d", random.nextInt(1000));
    }
}
