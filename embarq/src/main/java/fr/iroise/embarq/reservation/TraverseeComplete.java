package fr.iroise.embarq.reservation;

public class TraverseeComplete extends RuntimeException {

    public TraverseeComplete(long placesRestantes) {
        super(placesRestantes == 0
                ? "Cette traversée est complète."
                : "Il ne reste que " + placesRestantes + " place(s) sur cette traversée.");
    }
}
