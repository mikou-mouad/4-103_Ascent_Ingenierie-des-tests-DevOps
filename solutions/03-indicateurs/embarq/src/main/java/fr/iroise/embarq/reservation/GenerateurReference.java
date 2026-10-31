package fr.iroise.embarq.reservation;

import java.security.SecureRandom;
import org.springframework.stereotype.Component;

/**
 * Fabrique la référence donnée au passager, lisible au téléphone : IR- suivi de 8 caractères,
 * sans les caractères qu'on confond (0 et O, 1, I et L). 31^8 combinaisons : une collision est improbable,
 * et la contrainte d'unicité en base la refuserait.
 */
@Component
public class GenerateurReference {

    static final String ALPHABET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
    static final int LONGUEUR = 8;

    private final SecureRandom random = new SecureRandom();

    public String nouvelleReference() {
        StringBuilder reference = new StringBuilder("IR-");
        for (int i = 0; i < LONGUEUR; i++) {
            reference.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return reference.toString();
    }
}
