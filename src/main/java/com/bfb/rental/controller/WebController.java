package com.bfb.rental.controller;

import com.bfb.rental.entity.Client;
import com.bfb.rental.entity.Contrat;
import com.bfb.rental.entity.Vehicule;
import com.bfb.rental.enums.EtatVehicule;
import com.bfb.rental.repository.ClientRepository;
import com.bfb.rental.repository.ContratRepository;
import com.bfb.rental.repository.VehiculeRepository;
import com.bfb.rental.service.ContratService;
import com.bfb.rental.service.DataGeneratorService;
import com.bfb.rental.service.VehiculeService;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class WebController {

    private final VehiculeRepository vehiculeRepository;
    private final ContratRepository contratRepository;
    private final ClientRepository clientRepository;
    private final VehiculeService vehiculeService;
    private final ContratService contratService;
    private final DataGeneratorService dataGeneratorService;

    @GetMapping("/")
    public String index() { return "index"; }

    // ================= Gestion des Véhicules =================
    @GetMapping("/ui/vehicules")
    public String vehicules(Model model) {
        model.addAttribute("vehicules", vehiculeRepository.findAll());
        return "vehicules";
    }

    @GetMapping("/ui/vehicules/new")
    public String showVehiculeForm(Model model) {
        model.addAttribute("vehicule", new Vehicule());
        return "vehicule-form";
    }

    @GetMapping("/ui/vehicules/edit/{id}")
    public String showEditVehiculeForm(@PathVariable Long id, Model model) {
        Vehicule v = vehiculeRepository.findById(id).orElseThrow();
        model.addAttribute("vehicule", v);
        return "vehicule-form";
    }

    @GetMapping("/ui/vehicules/delete/{id}")
    public String deleteVehicule(@PathVariable Long id) {
        try {
            vehiculeRepository.deleteById(id);
        } catch (Exception e) {
            return "redirect:/ui/vehicules?error=cannot_delete";
        }
        return "redirect:/ui/vehicules";
    }

    @GetMapping("/ui/vehicules/random")
    public String randomVehicule() {
        dataGeneratorService.generateRandomVehicule();
        return "redirect:/ui/vehicules";
    }

    @PostMapping("/ui/vehicules")
    public String saveVehicule(@ModelAttribute Vehicule vehicule) {
        if (vehicule.getId() == null && vehicule.getEtat() == null) {
            vehicule.setEtat(EtatVehicule.DISPONIBLE);
        }
        vehiculeRepository.save(vehicule);
        return "redirect:/ui/vehicules";
    }

    // ================= Gestion des Clients =================
    @GetMapping("/ui/clients")
    public String clients(Model model) {
        model.addAttribute("clients", clientRepository.findAll());
        return "clients";
    }

    @GetMapping("/ui/clients/new")
    public String showClientForm(Model model) {
        model.addAttribute("client", new Client());
        return "client-form";
    }

    @GetMapping("/ui/clients/edit/{id}")
    public String showEditClientForm(@PathVariable Long id, Model model) {
        Client c = clientRepository.findById(id).orElseThrow();
        model.addAttribute("client", c);
        return "client-form";
    }

    @GetMapping("/ui/clients/delete/{id}")
    public String deleteClient(@PathVariable Long id) {
        try {
            clientRepository.deleteById(id);
        } catch (Exception e) {
            return "redirect:/ui/clients?error=cannot_delete";
        }
        return "redirect:/ui/clients";
    }

    @GetMapping("/ui/clients/random")
    public String randomClient() {
        dataGeneratorService.generateRandomClient();
        return "redirect:/ui/clients";
    }

    @PostMapping("/ui/clients")
    public String saveClient(@ModelAttribute Client client) {
        clientRepository.save(client);
        return "redirect:/ui/clients";
    }

    // ================= Gestion des Contrats =================
    @GetMapping("/ui/contrats")
    public String contrats(Model model) {
        model.addAttribute("contrats", contratRepository.findAll());
        return "contrats";
    }

    // Afficher le formulaire d'ajout de contrat
    @GetMapping("/ui/contrats/new")
    public String showContratForm(Model model) {
        model.addAttribute("contrat", new Contrat());
        model.addAttribute("clients", clientRepository.findAll());
        model.addAttribute("vehicules", vehiculeRepository.findAll());
        return "contrat-form";
    }
    
    @GetMapping("/ui/contrats/delete/{id}")
    public String deleteContrat(@PathVariable Long id) {
        contratRepository.deleteById(id);
        return "redirect:/ui/contrats";
    }

    // Retarder le contrat (déclenche l'Observer)
    @GetMapping("/ui/contrats/retard/{id}")
    public String delayContrat(@PathVariable Long id) {
        contratService.declarerRetard(id);
        return "redirect:/ui/contrats";
    }

    // Générer un contrat aléatoire
    @GetMapping("/ui/contrats/random")
    public String randomContrat() {
        try {
            dataGeneratorService.generateRandomContrat();
        } catch (Exception e) {
            return "redirect:/ui/contrats?error=" + e.getMessage();
        }
        return "redirect:/ui/contrats";
    }



    @GetMapping("/ui/contrats/edit/{id}")
    public String showEditContratForm(@PathVariable Long id, Model model) {
        Contrat contrat = contratRepository.findById(id).orElseThrow();
        
        model.addAttribute("contrat", contrat); // Passer le contrat actuel pour l'affichage
        model.addAttribute("clients", clientRepository.findAll());
        model.addAttribute("vehicules", vehiculeRepository.findAll());
        
        return "contrat-form"; // Réutiliser le même formulaire
    }

    // === Sauvegarder le contrat ===
    @PostMapping("/ui/contrats")
    public String saveContrat(
            @RequestParam(required = false) Long id,
            @RequestParam Long clientId, 
            @RequestParam Long vehiculeId, 
            @RequestParam String dateDebut, 
            @RequestParam String dateFin) {
        
        try {
            LocalDate start = java.time.LocalDate.parse(dateDebut);
            LocalDate end = java.time.LocalDate.parse(dateFin);

            if (id != null) {
                // Si un ID est présent, c'est une mise à jour
                contratService.updateContrat(id, clientId, vehiculeId, start, end);
            } else {
                // Si pas d'ID, c'est une création
                contratService.createContrat(clientId, vehiculeId, start, end);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/ui/contrats?error=" + "Conflit ou erreur";
        }
        return "redirect:/ui/contrats";
    }

    // Générer un scénario spécifique de "test de retard"
    @GetMapping("/ui/contrats/scenario/retard")
    public String generateRetardScenario() {
        dataGeneratorService.generateRetardScenario();
        return "redirect:/ui/contrats";
    }

    // Restituer le véhicule (terminer le contrat)
    @GetMapping("/ui/contrats/terminer/{id}")
    public String terminerContrat(@PathVariable Long id) {
        contratService.terminerContrat(id);
        return "redirect:/ui/contrats";
    }
}