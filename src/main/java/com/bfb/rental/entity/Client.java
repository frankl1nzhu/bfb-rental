package com.bfb.rental.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Data
// Règle : (Nom + Prenom + DateNaissance) doit être unique
@Table(uniqueConstraints = {
    @UniqueConstraint(columnNames = {"nom", "prenom", "dateNaissance"})
})
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateNaissance;

    // Numéro de permis unique globalement
    @Column(nullable = false, unique = true)
    private String numPermis;

    private String adresse;
    
    // Coordonnées réservées pour l'évolution future, utilisées pour notifier les annulations de contrat
    private String email; 
}