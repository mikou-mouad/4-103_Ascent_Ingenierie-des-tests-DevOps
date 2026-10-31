package fr.iroise.embarq.e2e;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/** La page d'accueil : la liste des prochaines traversées. */
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
        navigateur.findElement(By.cssSelector("a[id^='reserver-']")).click();
        return new PageReservation(navigateur);
    }

    /** Clique sur « Réserver » pour la première traversée assurée par ce navire. */
    public PageReservation reserverSur(String navire) {
        navigateur.findElement(By.xpath(
                "//tr[td[normalize-space()='" + navire + "']]//a[starts-with(@id, 'reserver-')]")).click();
        return new PageReservation(navigateur);
    }
}
