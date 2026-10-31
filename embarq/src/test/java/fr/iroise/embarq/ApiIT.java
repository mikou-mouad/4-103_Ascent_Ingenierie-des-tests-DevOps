package fr.iroise.embarq;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/** Test d'intégration de l'application complète : serveur web, API, base PostgreSQL. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class ApiIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @LocalServerPort
    int port;

    RestClient api;

    @BeforeEach
    void client() {
        // Les réponses 4xx sont rendues au test au lieu de lever une exception
        api = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .defaultStatusHandler(statut -> true, (requete, reponse) -> { })
                .build();
    }

    private List<Map<String, Object>> traversees() {
        return api.get().uri("/api/traversees").retrieve()
                .body(new ParameterizedTypeReference<List<Map<String, Object>>>() { });
    }

    private Number idDuMolenez() {
        return traversees().stream()
                .filter(t -> "Molenez".equals(t.get("navire")))
                .map(t -> (Number) t.get("id"))
                .findFirst().orElseThrow();
    }

    private ResponseEntity<Map> reserver(Number traversee, int adultes, int enfants) {
        return api.post().uri("/api/traversees/{id}/reservations", traversee)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("nom", "Le Gall", "email", "legall@example.org",
                        "adultes", adultes, "enfants", enfants, "velos", 0))
                .retrieve().toEntity(Map.class);
    }

    @Test
    void la_page_d_accueil_liste_les_traversees() {
        ResponseEntity<String> page = api.get().uri("/").retrieve().toEntity(String.class);

        assertThat(page.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(page.getBody()).contains("Prochaines traversées", "Molenez");
    }

    @Test
    void l_api_liste_les_traversees_a_venir() {
        assertThat(traversees()).isNotEmpty()
                .allSatisfy(t -> assertThat(t).containsKeys("id", "depart", "arrivee", "placesRestantes"));
    }

    @Test
    void l_api_calcule_un_tarif() {
        Map<?, ?> tarif = api.get().uri("/api/tarifs?adultes=2&enfants=1").retrieve().body(Map.class);

        assertThat(tarif.get("montant")).hasToString("90.0");
    }

    @Test
    void une_reservation_est_creee_puis_consultable() {
        ResponseEntity<Map> creee = reserver(idDuMolenez(), 2, 1);

        assertThat(creee.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String reference = (String) creee.getBody().get("reference");
        Map<?, ?> lue = api.get().uri("/api/reservations/{ref}", reference).retrieve().body(Map.class);
        assertThat(lue.get("statut")).isEqualTo("CONFIRMEE");
    }

    @Test
    void une_reservation_trop_grande_est_refusee_avec_409() {
        ResponseEntity<Map> refusee = reserver(idDuMolenez(), 13, 0);

        assertThat(refusee.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(refusee.getBody()).containsKey("erreur");
    }

    @Test
    void une_demande_invalide_est_refusee_avec_400() {
        ResponseEntity<Map> refusee = reserver(idDuMolenez(), 0, 0);

        assertThat(refusee.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void une_reservation_inconnue_renvoie_404() {
        ResponseEntity<String> reponse = api.get().uri("/api/reservations/IR-INCONNUE").retrieve().toEntity(String.class);

        assertThat(reponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
