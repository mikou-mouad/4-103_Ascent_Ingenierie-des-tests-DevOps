package fr.iroise.embarq.reservation;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/** Ce que le passager saisit dans le formulaire de réservation. */
public record DemandeReservation(
        @NotBlank(message = "Le nom est obligatoire") String nom,
        @NotBlank(message = "L'adresse e-mail est obligatoire") @Email(message = "Adresse e-mail invalide") String email,
        @Min(value = 1, message = "Au moins un adulte par réservation") @Max(value = 30, message = "30 adultes au maximum") int adultes,
        @Min(value = 0, message = "Nombre d'enfants invalide") @Max(value = 30, message = "30 enfants au maximum") int enfants,
        @Min(value = 0, message = "Nombre de vélos invalide") @Max(value = 10, message = "10 vélos au maximum") int velos) {
}
