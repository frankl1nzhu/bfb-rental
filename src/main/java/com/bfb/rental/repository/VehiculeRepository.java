package com.bfb.rental.repository;

import com.bfb.rental.entity.Vehicule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehiculeRepository extends JpaRepository<Vehicule, Long> {
    boolean existsByImmatriculation(String immatriculation);
}