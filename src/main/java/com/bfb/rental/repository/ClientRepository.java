package com.bfb.rental.repository;

import com.bfb.rental.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {

    // Utilisé pour vérifier l'unicité
    boolean existsByNumPermis(String numPermis);
    boolean existsByNomAndPrenomAndDateNaissance(String nom, String prenom, LocalDate dateNaissance);

    
}