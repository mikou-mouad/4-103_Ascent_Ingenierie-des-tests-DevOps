package fr.iroise.embarq.e2e;

import static fr.iroise.embarq.e2e.TestId.testId;

import java.time.Duration;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/** La page « Bon voyage ! » affichée après une réservation. */
public class PageConfirmation {

    private final WebDriver navigateur;

    PageConfirmation(WebDriver navigateur) {
        this.navigateur = navigateur;
        new WebDriverWait(navigateur, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(testId("reference")));
    }

    public String reference() {
        return navigateur.findElement(testId("reference")).getText();
    }

    public String montant() {
        return navigateur.findElement(testId("montant")).getText();
    }

    public String statut() {
        return navigateur.findElement(testId("statut")).getText();
    }
}
