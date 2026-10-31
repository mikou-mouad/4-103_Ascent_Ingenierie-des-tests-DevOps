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
        // TODO : trouver le premier lien « Réserver » et cliquer dessus.
        //        Inspectez la page dans Firefox (clic droit > Inspecter) : quel attribut repère ces liens ?
        //        Exemple de sélecteur CSS : By.cssSelector("a[id^='reserver-']") (id qui commence par reserver-)
        return new PageReservation(navigateur);
    }
}
