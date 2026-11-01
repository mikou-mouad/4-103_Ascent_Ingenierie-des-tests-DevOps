package fr.iroise.embarq.e2e;

import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/** La page « Bon voyage ! » affichée après une réservation. */
public class PageConfirmation {

    private final WebDriver navigateur;

    PageConfirmation(WebDriver navigateur) {
        this.navigateur = navigateur;
        // TODO : attendre (10 s au plus) que la référence soit visible, comme dans le constructeur de PageReservation,
        //        puis supprimez la ligne « throw ».
        throw new UnsupportedOperationException("TODO : constructeur de PageConfirmation");
    }

    public String reference() {
        return navigateur.findElement(By.id("reference")).getText();
    }

    public String montant() {
        // TODO : lire le texte de l'élément montant, puis supprimez la ligne « throw ».
        throw new UnsupportedOperationException("TODO : PageConfirmation.montant");
    }

    public String statut() {
        // TODO : lire le texte de l'élément statut, puis supprimez la ligne « throw ».
        throw new UnsupportedOperationException("TODO : PageConfirmation.statut");
    }
}
