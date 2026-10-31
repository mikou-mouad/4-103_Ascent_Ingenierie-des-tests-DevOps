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
        new WebDriverWait(navigateur, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(By.id("reference")));
    }

    public String reference() {
        return navigateur.findElement(By.id("reference")).getText();
    }

    public String montant() {
        return navigateur.findElement(By.id("montant")).getText();
    }

    public String statut() {
        return navigateur.findElement(By.id("statut")).getText();
    }
}
