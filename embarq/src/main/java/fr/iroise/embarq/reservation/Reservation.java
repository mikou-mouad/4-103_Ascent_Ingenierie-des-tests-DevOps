package fr.iroise.embarq.reservation;

import fr.iroise.embarq.traversee.Traversee;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Référence donnée au passager, par exemple IR-482. */
    private String reference;

    @ManyToOne(optional = false)
    private Traversee traversee;

    private String nom;
    private String email;
    private int adultes;
    private int enfants;
    private int velos;
    private BigDecimal montant;

    @Enumerated(EnumType.STRING)
    private StatutReservation statut;

    private LocalDateTime creeLe;

    protected Reservation() {
    }

    public Reservation(String reference, Traversee traversee, String nom, String email,
                       int adultes, int enfants, int velos, BigDecimal montant) {
        this.reference = reference;
        this.traversee = traversee;
        this.nom = nom;
        this.email = email;
        this.adultes = adultes;
        this.enfants = enfants;
        this.velos = velos;
        this.montant = montant;
        this.statut = StatutReservation.CONFIRMEE;
        this.creeLe = LocalDateTime.now();
    }

    public void annuler() {
        this.statut = StatutReservation.ANNULEE;
    }

    public int getPassagers() {
        return adultes + enfants;
    }

    public Long getId() {
        return id;
    }

    public String getReference() {
        return reference;
    }

    public Traversee getTraversee() {
        return traversee;
    }

    public String getNom() {
        return nom;
    }

    public String getEmail() {
        return email;
    }

    public int getAdultes() {
        return adultes;
    }

    public int getEnfants() {
        return enfants;
    }

    public int getVelos() {
        return velos;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public StatutReservation getStatut() {
        return statut;
    }

    public LocalDateTime getCreeLe() {
        return creeLe;
    }
}
