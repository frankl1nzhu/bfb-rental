package com.bfb.rental.controller;

import com.bfb.rental.entity.Vehicule;
import com.bfb.rental.service.VehiculeService;
import com.bfb.rental.repository.VehiculeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicules")
@RequiredArgsConstructor
public class VehiculeController {

    private final VehiculeService vehiculeService;
    private final VehiculeRepository vehiculeRepository;

    @PostMapping
    public Vehicule createVehicule(@RequestBody Vehicule vehicule) {
        return vehiculeService.createVehicule(vehicule);
    }

    @GetMapping
    public List<Vehicule> getAllVehicules() {
        return vehiculeRepository.findAll();
    }

    /**
     * Interface clé : Signaler une panne de véhicule
     * URL: POST /api/vehicules/1/panne
     */
    @PostMapping("/{id}/panne")
    public ResponseEntity<String> declarerPanne(@PathVariable Long id) {
        vehiculeService.declarerPanne(id);
        return ResponseEntity.ok("Le véhicule a été marqué en panne, les contrats en attente associés ont été automatiquement annulés.");
    }
}