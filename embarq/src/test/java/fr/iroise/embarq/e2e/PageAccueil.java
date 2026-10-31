package fr.iroise.embarq.e2e;

import static fr.iroise.embarq.e2e.TestId.testId;

import org.openqa.selenium.WebDriver;

/** La page d'accueil : les prochaines traversées. */
public class PageAccueil {

    private final WebDriver navigateur;

    private PageAccueil(WebDriver navigateur) {
        this.navigateur = navigateur;
    }

    public static PageAccueil ouvrir(WebDriver navigateur, String urlApplication) {
        navigateur.get(urlApplication + "/");
        return new PageAccueil(navigateur);
    }

    /** Clique sur le premier bouton « Réserver » de la liste. */
    public PageReservation reserverLaPremiereTraversee() {
        navigateur.findElement(testId("reserver")).click();
        return new PageReservation(navigateur);
    }

    /** Clique sur « Réserver » pour la première traversée assurée par ce navire. */
    public PageReservation reserverSur(String navire) {
        navigateur.findElements(testId("traversee")).stream()
                .filter(traversee -> traversee.findElement(testId("navire")).getText().equals(navire))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Aucune traversée sur le " + navire))
                .findElement(testId("reserver"))
                .click();
        return new PageReservation(navigateur);
    }
}
