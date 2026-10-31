package fr.iroise.embarq.tarif;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;

/**
 * Grille tarifaire d'Iroise Traversées (aller simple).
 *
 * <ul>
 *   <li>Adulte : 36,00 €</li>
 *   <li>Enfant de 4 à 11 ans : 18,00 €</li>
 *   <li>Vélo : 17,00 €</li>
 *   <li>Groupe : à partir de 10 passagers, 10 % de remise sur les billets passagers (pas sur les vélos)</li>
 * </ul>
 */
@Service
public class TarifService {

    static final BigDecimal ADULTE = new BigDecimal("36.00");
    static final BigDecimal ENFANT = new BigDecimal("18.00");
    static final BigDecimal VELO = new BigDecimal("17.00");
    static final int SEUIL_GROUPE = 10;
    static final BigDecimal REMISE_GROUPE = new BigDecimal("0.90");

    public BigDecimal calculer(int adultes, int enfants, int velos) {
        BigDecimal passagers = ADULTE.multiply(BigDecimal.valueOf(adultes))
                .add(ENFANT.multiply(BigDecimal.valueOf(enfants)));
        if (adultes + enfants >= SEUIL_GROUPE) {
            passagers = passagers.multiply(REMISE_GROUPE);
        }
        BigDecimal total = passagers.add(VELO.multiply(BigDecimal.valueOf(velos)));
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    /** Tarif résident des îles (Molène, Ouessant) : moitié prix, d'après l'appel de Maëlle. */
    public BigDecimal calculer(int adultes, int enfants, int velos, boolean resident) {
        BigDecimal total = calculer(adultes, enfants, velos);
        if (resident) {
            total = total.multiply(new BigDecimal("0.50"));
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }
}
