package fr.iroise.embarq.e2e;

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
                .until(ExpectedConditions.visibilityOfElementLocated(By.id("valider")));
    }

    public PageReservation saisir(String nom, String email, int adultes, int enfants, int velos) {
        remplir(By.id("nom"), nom);
        remplir(By.id("email"), email);
        remplir(By.id("adultes"), String.valueOf(adultes));
        remplir(By.id("enfants"), String.valueOf(enfants));
        remplir(By.id("velos"), String.valueOf(velos));
        return this;
    }

    /** Valide un formulaire correct : on arrive sur la confirmation. */
    public PageConfirmation valider() {
        navigateur.findElement(By.id("valider")).click();
        return new PageConfirmation(navigateur);
    }

    /** Valide un formulaire incorrect : on reste sur le formulaire, avec des messages d'erreur. */
    public PageReservation validerSansSucces() {
        navigateur.findElement(By.id("valider")).click();
        new WebDriverWait(navigateur, Duration.ofSeconds(10))
                .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".erreur, .erreur-champ")));
        return this;
    }

    /** Les messages d'erreur affichés. */
    public List<String> erreurs() {
        return navigateur.findElements(By.cssSelector(".erreur, .erreur-champ")).stream()
                .map(WebElement::getText)
                .toList();
    }

    private void remplir(By champ, String valeur) {
        WebElement element = navigateur.findElement(champ);
        element.clear();
        element.sendKeys(valeur);
    }
}
