package fr.iroise.embarq.traversee;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

/** Une traversée : un navire, un trajet, une date. */
@Entity
public class Traversee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String depart;
    private String arrivee;
    private LocalDateTime dateHeure;
    private String navire;
    /** Nombre de passagers autorisés à bord (adultes et enfants). */
    private int capacite;

    protected Traversee() {
    }

    public Traversee(String depart, String arrivee, LocalDateTime dateHeure, String navire, int capacite) {
        this.depart = depart;
        this.arrivee = arrivee;
        this.dateHeure = dateHeure;
        this.navire = navire;
        this.capacite = capacite;
    }

    public Long getId() {
        return id;
    }

    public String getDepart() {
        return depart;
    }

    public String getArrivee() {
        return arrivee;
    }

    public LocalDateTime getDateHeure() {
        return dateHeure;
    }

    public String getNavire() {
        return navire;
    }

    public int getCapacite() {
        return capacite;
    }
}
