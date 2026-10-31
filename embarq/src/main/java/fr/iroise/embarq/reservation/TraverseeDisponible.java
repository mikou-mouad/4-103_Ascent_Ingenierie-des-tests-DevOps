package fr.iroise.embarq.reservation;

import fr.iroise.embarq.traversee.Traversee;

/** Une traversée vue par le passager : le trajet et les places qu'il reste. */
public record TraverseeDisponible(Traversee traversee, long placesRestantes) {

    public boolean complete() {
        return placesRestantes <= 0;
    }
}
