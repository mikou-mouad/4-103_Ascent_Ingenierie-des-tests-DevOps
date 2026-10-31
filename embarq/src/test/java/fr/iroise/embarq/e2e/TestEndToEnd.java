package fr.iroise.embarq.e2e;

import java.net.MalformedURLException;
import java.net.URI;
import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

/**
 * Base des tests end-to-end : un navigateur Chrome de la Selenium Grid, ouvert avant chaque test et fermé après.
 *
 * <p>Lancement : mvn -B -Pe2e verify -Dapp.url=http://embarq-e2e:8080 -Dselenium.url=http://localhost:4444
 *
 * <p>L'adresse de l'application est vue depuis le navigateur, donc depuis le conteneur Selenium :
 * embarq-e2e est le nom de l'application dans le réseau Docker « lab ».
 */
public abstract class TestEndToEnd {

    protected static final String APP_URL = System.getProperty("app.url", "http://embarq-e2e:8080");
    private static final String SELENIUM_URL = System.getProperty("selenium.url", "http://localhost:4444");

    protected WebDriver navigateur;

    @BeforeEach
    void ouvrirLeNavigateur() throws MalformedURLException {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--window-size=1280,900", "--lang=fr-FR");
        navigateur = new RemoteWebDriver(URI.create(SELENIUM_URL).toURL(), options);
        // Pas d'attente implicite : les pages attendent explicitement ce dont elles ont besoin
        navigateur.manage().timeouts().implicitlyWait(Duration.ZERO);
    }

    @AfterEach
    void fermerLeNavigateur() {
        if (navigateur != null) {
            navigateur.quit();
        }
    }
}
