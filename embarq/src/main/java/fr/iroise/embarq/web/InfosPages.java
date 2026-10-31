package fr.iroise.embarq.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Version et environnement affichés en bas de chaque page : on sait toujours ce qu'on teste. */
@ControllerAdvice(assignableTypes = PagesController.class)
public class InfosPages {

    private final String version;
    private final String environnement;

    public InfosPages(@Value("${embarq.version}") String version,
                      @Value("${embarq.environnement}") String environnement) {
        this.version = version;
        this.environnement = environnement;
    }

    @ModelAttribute("version")
    public String version() {
        return version;
    }

    @ModelAttribute("environnement")
    public String environnement() {
        return environnement;
    }
}
