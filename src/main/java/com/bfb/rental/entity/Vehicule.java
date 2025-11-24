package com.bfb.rental.entity;

import com.bfb.rental.enums.EtatVehicule;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Data
public class Vehicule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String marque;
    private String modele;
    private String motorisation;
    private String couleur;

    // Immatriculation unique globalement
    @Column(nullable = false, unique = true)
    private String immatriculation;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateAcquisition;

    @Enumerated(EnumType.STRING)
    private EtatVehicule etat;

    private Double prixJournee; 
}