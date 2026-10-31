package fr.iroise.embarq.bdd;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

/**
 * Lance les scénarios Cucumber (src/test/resources/features) avec les tests unitaires : mvn test.
 * Le rapport HTML est écrit dans target/cucumber/rapport.html.
 */
@Suite(failIfNoTests = false)
@IncludeEngines("cucumber")
@SelectPackages("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "fr.iroise.embarq.bdd")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty, html:target/cucumber/rapport.html")
public class CucumberTest {
}
