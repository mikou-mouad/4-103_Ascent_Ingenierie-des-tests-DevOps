package fr.iroise.embarq.web;

import fr.iroise.embarq.reservation.DemandeReservation;
import fr.iroise.embarq.reservation.Reservation;
import fr.iroise.embarq.reservation.ReservationService;
import fr.iroise.embarq.reservation.TraverseeComplete;
import fr.iroise.embarq.reservation.TraverseeDisponible;
import fr.iroise.embarq.tarif.TarifService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** L'API REST : utilisée par l'application mobile, les tests de charge et les smoke tests. */
@RestController
@RequestMapping("/api")
public class ApiController {

    private final ReservationService service;
    private final TarifService tarifs;

    public ApiController(ReservationService service, TarifService tarifs) {
        this.service = service;
        this.tarifs = tarifs;
    }

    @GetMapping("/traversees")
    public List<TraverseeApi> traversees() {
        return service.traverseesAVenir().stream().map(TraverseeApi::de).toList();
    }

    @GetMapping("/traversees/{id}")
    public TraverseeApi traversee(@PathVariable Long id) {
        return TraverseeApi.de(service.traversee(id));
    }

    @PostMapping("/traversees/{id}/reservations")
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationApi reserver(@PathVariable Long id, @Valid @RequestBody DemandeReservation demande) {
        return ReservationApi.de(service.reserver(id, demande));
    }

    @GetMapping("/reservations/{reference}")
    public ReservationApi reservation(@PathVariable String reference) {
        return ReservationApi.de(service.reservation(reference));
    }

    @PostMapping("/reservations/{reference}/annulation")
    public ReservationApi annuler(@PathVariable String reference) {
        return ReservationApi.de(service.annuler(reference));
    }

    @GetMapping("/tarifs")
    public Map<String, BigDecimal> tarif(@RequestParam int adultes, @RequestParam(defaultValue = "0") int enfants,
                                         @RequestParam(defaultValue = "0") int velos) {
        return Map.of("montant", tarifs.calculer(adultes, enfants, velos));
    }

    @ExceptionHandler(TraverseeComplete.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> complete(TraverseeComplete e) {
        return Map.of("erreur", e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> invalide(MethodArgumentNotValidException e) {
        return Map.of("erreur", "Demande invalide", "champs", e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + " : " + f.getDefaultMessage()).toList());
    }

    public record TraverseeApi(Long id, String depart, String arrivee, LocalDateTime dateHeure, String navire,
                               int capacite, long placesRestantes) {

        static TraverseeApi de(TraverseeDisponible d) {
            var t = d.traversee();
            return new TraverseeApi(t.getId(), t.getDepart(), t.getArrivee(), t.getDateHeure(), t.getNavire(),
                    t.getCapacite(), d.placesRestantes());
        }
    }

    public record ReservationApi(String reference, Long traverseeId, String nom, int adultes, int enfants,
                                 int velos, BigDecimal montant, String statut) {

        static ReservationApi de(Reservation r) {
            return new ReservationApi(r.getReference(), r.getTraversee().getId(), r.getNom(), r.getAdultes(),
                    r.getEnfants(), r.getVelos(), r.getMontant(), r.getStatut().name());
        }
    }
}
