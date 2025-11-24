package com.bfb.rental.service;

import com.bfb.rental.entity.Vehicule;
import com.bfb.rental.enums.EtatVehicule;
import com.bfb.rental.event.VehiculePanneEvent;
import com.bfb.rental.repository.VehiculeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VehiculeService {

    private final VehiculeRepository vehiculeRepository;
    private final ApplicationEventPublisher eventPublisher;

    // Créer un véhicule (avec validation d'unicité)
    public Vehicule createVehicule(Vehicule vehicule) {
        if (vehiculeRepository.existsByImmatriculation(vehicule.getImmatriculation())) {
            throw new IllegalArgumentException("Immatriculation déjà existante: " + vehicule.getImmatriculation());
        }
        // État par défaut
        if (vehicule.getEtat() == null) {
            vehicule.setEtat(EtatVehicule.DISPONIBLE);
        }
        return vehiculeRepository.save(vehicule);
    }

    // Métier principal : marquer un véhicule en panne
    @Transactional
    public void declarerPanne(Long vehiculeId) {
        Vehicule vehicule = vehiculeRepository.findById(vehiculeId)
                .orElseThrow(() -> new RuntimeException("Véhicule inexistant"));

        vehicule.setEtat(EtatVehicule.EN_PANNE);
        vehiculeRepository.save(vehicule);

        // Pattern Observateur
        // Publier l'événement "véhicule en panne", je ne me soucie pas de qui le traite.
        // Ainsi VehiculeService n'a pas besoin de dépendre de ContratService.
        eventPublisher.publishEvent(new VehiculePanneEvent(this, vehiculeId));
    }
    
    public Vehicule findById(Long id) {
        return vehiculeRepository.findById(id).orElseThrow(() -> new RuntimeException("Véhicule introuvable"));
    }
}