package fr.iroise.embarq.e2e;

import org.openqa.selenium.By;

/**
 * Les sélecteurs des tests end-to-end : l'attribut data-testid, posé dans les pages pour les tests.
 * Il ne dépend ni du style (classes CSS), ni de la structure (balises), ni des id utilisés par le JavaScript :
 * une refonte graphique ne casse pas les tests tant qu'elle garde ces attributs.
 */
final class TestId {

    private TestId() {
    }

    static By testId(String valeur) {
        return By.cssSelector("[data-testid='" + valeur + "']");
    }
}
