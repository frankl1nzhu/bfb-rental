package com.bfb.rental.repository;

import com.bfb.rental.entity.Contrat;
import com.bfb.rental.enums.EtatContrat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ContratRepository extends JpaRepository<Contrat, Long> {
    
    // Rechercher s'il existe des contrats en conflit pour un véhicule dans une période donnée
    // Il y a chevauchement si (DébutA <= FinB) et (FinA >= DébutB)
    @Query("SELECT c FROM Contrat c WHERE c.vehicule.id = :vehiculeId " +
           "AND c.etat IN ('EN_ATTENTE', 'EN_COURS') " +
           "AND c.dateDebut <= :dateFin AND c.dateFin >= :dateDebut")
    List<Contrat> findConflictingContrats(@Param("vehiculeId") Long vehiculeId, 
                                          @Param("dateDebut") LocalDate dateDebut, 
                                          @Param("dateFin") LocalDate dateFin);



    // Utilisé pour la fonctionnalité : 
    // lorsque le véhicule est en panne, trouver tous les contrats "EN_ATTENTE" pour ce véhicule, prêts à être annulés
    List<Contrat> findByVehiculeIdAndEtat(Long vehiculeId, EtatContrat etat);
}