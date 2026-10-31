package fr.iroise.embarq.reservation;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Optional<Reservation> findByReference(String reference);

    /** Nombre de places déjà vendues sur une traversée : adultes et enfants des réservations confirmées. */
    @Query("""
            select coalesce(sum(r.adultes + r.enfants), 0) from Reservation r
            where r.traversee.id = :traverseeId
              and r.statut = fr.iroise.embarq.reservation.StatutReservation.CONFIRMEE
            """)
    long placesReservees(@Param("traverseeId") Long traverseeId);
}
