package fr.iroise.embarq.tarif;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;

/**
 * Grille tarifaire d'Iroise Traversées (aller simple).
 * Les règles sont décrites, avec des exemples, dans src/test/resources/features/tarifs.feature.
 *
 * <ul>
 *   <li>Adulte : 36,00 €</li>
 *   <li>Enfant de 4 à 11 ans : 18,00 €</li>
 *   <li>Vélo : 17,00 €</li>
 *   <li>Groupe : à partir de 10 passagers, 10 % de remise sur les billets passagers (pas sur les vélos)</li>
 *   <li>Résident des îles : adulte 9,00 €, enfant gratuit, vélo plein tarif, pas de remise de groupe</li>
 * </ul>
 */
@Service
public class TarifService {

    static final BigDecimal ADULTE = new BigDecimal("36.00");
    static final BigDecimal ENFANT = new BigDecimal("18.00");
    static final BigDecimal VELO = new BigDecimal("17.00");
    static final int SEUIL_GROUPE = 10;
    static final BigDecimal REMISE_GROUPE = new BigDecimal("0.90");
    static final BigDecimal ADULTE_RESIDENT = new BigDecimal("9.00");

    public BigDecimal calculer(int adultes, int enfants, int velos) {
        BigDecimal passagers = ADULTE.multiply(BigDecimal.valueOf(adultes))
                .add(ENFANT.multiply(BigDecimal.valueOf(enfants)));
        if (adultes + enfants >= SEUIL_GROUPE) {
            passagers = passagers.multiply(REMISE_GROUPE);
        }
        BigDecimal total = passagers.add(VELO.multiply(BigDecimal.valueOf(velos)));
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculer(int adultes, int enfants, int velos, boolean resident) {
        if (!resident) {
            return calculer(adultes, enfants, velos);
        }
        // Les enfants résidents voyagent gratuitement
        BigDecimal total = ADULTE_RESIDENT.multiply(BigDecimal.valueOf(adultes))
                .add(VELO.multiply(BigDecimal.valueOf(velos)));
        return total.setScale(2, RoundingMode.HALF_UP);
    }
}
