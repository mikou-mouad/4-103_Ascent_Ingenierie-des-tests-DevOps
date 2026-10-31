package fr.iroise.embarq.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fr.iroise.embarq.tarif.TarifService;
import fr.iroise.embarq.traversee.Traversee;
import fr.iroise.embarq.traversee.TraverseeRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Tests unitaires du service : les dépôts sont remplacés par des mocks. */
@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    TraverseeRepository traversees;
    @Mock
    ReservationRepository reservations;

    ReservationService service;
    Traversee molenez = new Traversee("Le Conquet", "Ouessant", LocalDateTime.now().plusDays(1), "Molenez", 12);

    @BeforeEach
    void preparer() {
        service = new ReservationService(traversees, reservations, new TarifService(), new GenerateurReference());
        when(traversees.findById(7L)).thenReturn(Optional.of(molenez));
    }

    @Test
    void reservation_acceptee_quand_il_reste_des_places() {
        when(reservations.placesReservees(any())).thenReturn(4L);
        when(reservations.save(any())).thenAnswer(appel -> appel.getArgument(0));

        Reservation reservation = service.reserver(7L, new DemandeReservation("Le Gall", "legall@example.org", 2, 1, 0));

        assertThat(reservation.getPassagers()).isEqualTo(3);
        assertThat(reservation.getMontant()).isEqualByComparingTo("90.00");
        assertThat(reservation.getReference()).startsWith("IR-");
    }

    @Test
    void reservation_refusee_quand_la_traversee_est_complete() {
        when(reservations.placesReservees(any())).thenReturn(12L);

        assertThatThrownBy(() -> service.reserver(7L, new DemandeReservation("Le Gall", "legall@example.org", 1, 0, 0)))
                .isInstanceOf(TraverseeComplete.class);
        verify(reservations, never()).save(any());
    }
}
