package com.bfb.rental.controller;

import com.bfb.rental.dto.CreateContratRequest;
import com.bfb.rental.entity.Contrat;
import com.bfb.rental.repository.ContratRepository;
import com.bfb.rental.service.ContratService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contrats")
@RequiredArgsConstructor
public class ContratController {

    private final ContratService contratService;
    private final ContratRepository contratRepository;

    @PostMapping
    public ResponseEntity<?> createContrat(@RequestBody CreateContratRequest request) {
        try {
            Contrat contrat = contratService.createContrat(
                request.getClientId(),
                request.getVehiculeId(),
                request.getDateDebut(),
                request.getDateFin()
            );
            return ResponseEntity.ok(contrat);
        } catch (Exception e) {
            // Capturer les exceptions de la logique métier (par exemple : véhicule déjà loué, véhicule en panne)
            return ResponseEntity.badRequest().body("Échec de la création : " + e.getMessage());
        }
    }

    @GetMapping
    public List<Contrat> getAllContrats() {
        return contratRepository.findAll();
    }

    /**
     * Interface clé : Signaler un retard de contrat
     * URL: POST /api/contrats/1/retard
     */
    @PostMapping("/{id}/retard")
    public ResponseEntity<String> declarerRetard(@PathVariable Long id) {
        contratService.declarerRetard(id);
        return ResponseEntity.ok("Le contrat a été marqué comme en retard. Si cela affecte les contrats suivants, ceux-ci ont été annulés.");
    }
}