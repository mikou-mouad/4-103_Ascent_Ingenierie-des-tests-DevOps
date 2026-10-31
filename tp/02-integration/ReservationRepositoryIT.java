package fr.iroise.embarq.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import fr.iroise.embarq.traversee.Traversee;
import fr.iroise.embarq.traversee.TraverseeRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Test d'intégration du comptage des places : les vraies requêtes, sur une vraie base PostgreSQL.
 * Testcontainers démarre PostgreSQL dans un conteneur Docker, Flyway crée les tables, puis les tests s'exécutent.
 * Chaque test tourne dans une transaction annulée à la fin : les tests ne se gênent pas entre eux.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class ReservationRepositoryIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    TraverseeRepository traversees;
    @Autowired
    ReservationRepository reservations;

    Traversee molenez;

    @BeforeEach
    void preparer() {
        molenez = traversees.save(new Traversee("Le Conquet", "Ouessant", LocalDateTime.now().plusDays(1), "Molenez", 12));
    }

    private Reservation reserver(String reference, int adultes, int enfants) {
        return reservations.save(new Reservation(reference, molenez, "Test", "test@example.org",
                adultes, enfants, 0, BigDecimal.TEN));
    }

    @Test
    void une_traversee_sans_reservation_n_a_aucune_place_vendue() {
        assertThat(reservations.placesReservees(molenez.getId())).isZero();
    }

    @Test
    void les_enfants_comptent_dans_les_places_vendues() {
        // TODO : réserver 2 adultes et 3 enfants (référence IR-001),
        //        puis vérifier que placesReservees renvoie 5
    }

    @Test
    void les_reservations_annulees_liberent_leurs_places() {
        // TODO : réserver 4 adultes (IR-002), puis annuler cette réservation (méthode annuler()),
        //        réserver 2 adultes (IR-003), et vérifier que placesReservees renvoie 2
    }
}
