package fr.iroise.embarq.web;

import fr.iroise.embarq.reservation.DemandeReservation;
import fr.iroise.embarq.reservation.Reservation;
import fr.iroise.embarq.reservation.ReservationService;
import fr.iroise.embarq.reservation.TraverseeComplete;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

/** Les pages web vues par les passagers. */
@Controller
public class PagesController {

    private final ReservationService service;

    public PagesController(ReservationService service) {
        this.service = service;
    }

    @GetMapping("/")
    public String accueil(Model model) {
        model.addAttribute("traversees", service.traverseesAVenir());
        return "index";
    }

    @GetMapping("/traversees/{id}/reservation")
    public String formulaire(@PathVariable Long id, Model model) {
        model.addAttribute("traversee", service.traversee(id));
        model.addAttribute("demande", new DemandeReservation("", "", 1, 0, 0));
        return "reserver";
    }

    @PostMapping("/traversees/{id}/reservation")
    public String reserver(@PathVariable Long id, @Valid @ModelAttribute("demande") DemandeReservation demande,
                           BindingResult erreurs, Model model) {
        model.addAttribute("traversee", service.traversee(id));
        if (erreurs.hasErrors()) {
            return "reserver";
        }
        try {
            Reservation reservation = service.reserver(id, demande);
            return "redirect:/reservations/" + reservation.getReference();
        } catch (TraverseeComplete e) {
            model.addAttribute("erreur", e.getMessage());
            return "reserver";
        }
    }

    @GetMapping("/reservations/{reference}")
    public String confirmation(@PathVariable String reference, Model model) {
        model.addAttribute("reservation", service.reservation(reference));
        return "confirmation";
    }
}
