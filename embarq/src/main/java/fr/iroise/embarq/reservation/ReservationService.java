package fr.iroise.embarq.reservation;

import fr.iroise.embarq.tarif.TarifService;
import fr.iroise.embarq.traversee.Traversee;
import fr.iroise.embarq.traversee.TraverseeRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReservationService {

    private final TraverseeRepository traversees;
    private final ReservationRepository reservations;
    private final TarifService tarifs;
    private final GenerateurReference generateur;

    public ReservationService(TraverseeRepository traversees, ReservationRepository reservations,
                              TarifService tarifs, GenerateurReference generateur) {
        this.traversees = traversees;
        this.reservations = reservations;
        this.tarifs = tarifs;
        this.generateur = generateur;
    }

    /** Les traversées à venir, avec les places restantes. */
    @Transactional(readOnly = true)
    public List<TraverseeDisponible> traverseesAVenir() {
        return traversees.findByDateHeureAfterOrderByDateHeure(LocalDateTime.now()).stream()
                .map(t -> new TraverseeDisponible(t, placesRestantes(t)))
                .toList();
    }

    @Transactional(readOnly = true)
    public TraverseeDisponible traversee(Long traverseeId) {
        Traversee traversee = trouverTraversee(traverseeId);
        return new TraverseeDisponible(traversee, placesRestantes(traversee));
    }

    @Transactional
    public Reservation reserver(Long traverseeId, DemandeReservation demande) {
        Traversee traversee = trouverTraversee(traverseeId);
        long restantes = placesRestantes(traversee);
        int passagers = demande.adultes() + demande.enfants();
        if (passagers > restantes) {
            throw new TraverseeComplete(Math.max(restantes, 0));
        }
        BigDecimal montant = tarifs.calculer(demande.adultes(), demande.enfants(), demande.velos());
        Reservation reservation = new Reservation(generateur.nouvelleReference(), traversee,
                demande.nom(), demande.email(), demande.adultes(), demande.enfants(), demande.velos(), montant);
        return reservations.save(reservation);
    }

    @Transactional(readOnly = true)
    public Reservation reservation(String reference) {
        return reservations.findByReference(reference)
                .orElseThrow(() -> new IntrouvableException("Réservation inconnue : " + reference));
    }

    @Transactional
    public Reservation annuler(String reference) {
        Reservation reservation = reservation(reference);
        reservation.annuler();
        return reservation;
    }

    private long placesRestantes(Traversee traversee) {
        return traversee.getCapacite() - reservations.placesReservees(traversee.getId());
    }

    private Traversee trouverTraversee(Long traverseeId) {
        return traversees.findById(traverseeId)
                .orElseThrow(() -> new IntrouvableException("Traversée inconnue : " + traverseeId));
    }
}
