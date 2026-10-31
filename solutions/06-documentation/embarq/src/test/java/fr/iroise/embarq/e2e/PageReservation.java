package fr.iroise.embarq.e2e;

import static fr.iroise.embarq.e2e.TestId.testId;

import java.time.Duration;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/** Le formulaire de réservation d'une traversée. */
public class PageReservation {

    private final WebDriver navigateur;

    PageReservation(WebDriver navigateur) {
        this.navigateur = navigateur;
        // On attend que le formulaire soit affiché avant de s'en servir
        new WebDriverWait(navigateur, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(testId("valider")));
    }

    public PageReservation saisir(String nom, String email, int adultes, int enfants, int velos) {
        remplir(testId("nom"), nom);
        remplir(testId("email"), email);
        remplir(testId("adultes"), String.valueOf(adultes));
        remplir(testId("enfants"), String.valueOf(enfants));
        remplir(testId("velos"), String.valueOf(velos));
        return this;
    }

    /** Valide un formulaire correct : on arrive sur la confirmation. */
    public PageConfirmation valider() {
        navigateur.findElement(testId("valider")).click();
        return new PageConfirmation(navigateur);
    }

    /** Valide un formulaire incorrect : on reste sur le formulaire, avec des messages d'erreur. */
    public PageReservation validerSansSucces() {
        navigateur.findElement(testId("valider")).click();
        new WebDriverWait(navigateur, Duration.ofSeconds(10))
                .until(ExpectedConditions.presenceOfElementLocated(testId("erreur")));
        return this;
    }

    /** Les messages d'erreur affichés. */
    public List<String> erreurs() {
        return navigateur.findElements(testId("erreur")).stream()
                .map(WebElement::getText)
                .toList();
    }

    private void remplir(By champ, String valeur) {
        WebElement element = navigateur.findElement(champ);
        element.clear();
        element.sendKeys(valeur);
    }
}
