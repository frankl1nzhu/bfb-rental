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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
public class WebController {

    private final VehiculeRepository vehiculeRepository;
    private final ContratRepository contratRepository;
    private final ClientRepository clientRepository;
    private final VehiculeService vehiculeService;
    private final ContratService contratService;
    private final DataGeneratorService dataGeneratorService;

    // === Page d'accueil (Tableau de bord) ===
    // [Attention] C'est la seule méthode qui gère "/", l'ancienne index() doit être supprimée
    @GetMapping("/")
    public String index(Model model) {
        // 1. Comptage de base
        long totalCars = vehiculeRepository.count();
        long totalClients = clientRepository.count();

        // 2. Statistiques d'état
        long rentedCars = vehiculeRepository.findAll().stream()
                .filter(v -> v.getEtat() == EtatVehicule.EN_LOCATION).count();

        // 3. Statistiques financières (calcul du revenu total des contrats terminés ou en cours, éviter les NullPointer)
        double totalRevenue = contratRepository.findAll().stream()
                .filter(c -> c.getPrixTotal() != null)
                .mapToDouble(Contrat::getPrixTotal)
                .sum();

        // 4. Statistiques d'alerte
        long overdueContracts = contratRepository.findAll().stream()
                .filter(c -> c.getEtat() == com.bfb.rental.enums.EtatContrat.EN_RETARD)
                .count();

        model.addAttribute("totalCars", totalCars);
        model.addAttribute("rentedCars", rentedCars);
        model.addAttribute("totalClients", totalClients);
        model.addAttribute("totalRevenue", String.format("%.2f", totalRevenue));
        model.addAttribute("overdueContracts", overdueContracts);

        return "index";
    }

    // Réinitialiser le système
    @GetMapping("/ui/reset")
    public String resetSystem() {
        dataGeneratorService.resetSystem();
        return "redirect:/?reset=success";
    }

    // ================= Gestion des véhicules =================
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
        // Si c'est nouveau et sans état, disponible par défaut
        if (vehicule.getId() == null && vehicule.getEtat() == null) {
            vehicule.setEtat(EtatVehicule.DISPONIBLE);
        }
        vehiculeRepository.save(vehicule);
        return "redirect:/ui/vehicules";
    }

    // Signaler une panne (déclenché via l'interface Web)
    @GetMapping("/ui/vehicules/panne/{id}")
    public String declarerPanne(@PathVariable Long id) {
        vehiculeService.declarerPanne(id);
        return "redirect:/ui/vehicules";
    }

    // ================= Gestion des clients =================
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

    // ================= Gestion des contrats =================
    @GetMapping("/ui/contrats")
    public String contrats(Model model) {
        model.addAttribute("contrats", contratRepository.findAll());
        return "contrats";
    }

    @GetMapping("/ui/contrats/new")
    public String showContratForm(Model model) {
        model.addAttribute("contrat", new Contrat()); // Objet vide
        model.addAttribute("clients", clientRepository.findAll());
        model.addAttribute("vehicules", vehiculeRepository.findAll());
        return "contrat-form";
    }

    @GetMapping("/ui/contrats/edit/{id}")
    public String showEditContratForm(@PathVariable Long id, Model model) {
        Contrat contrat = contratRepository.findById(id).orElseThrow();
        model.addAttribute("contrat", contrat); // Objet à afficher
        model.addAttribute("clients", clientRepository.findAll());
        model.addAttribute("vehicules", vehiculeRepository.findAll());
        return "contrat-form";
    }

    @GetMapping("/ui/contrats/delete/{id}")
    public String deleteContrat(@PathVariable Long id) {
        contratRepository.deleteById(id);
        return "redirect:/ui/contrats";
    }

    @GetMapping("/ui/contrats/retard/{id}")
    public String delayContrat(@PathVariable Long id) {
        contratService.declarerRetard(id);
        return "redirect:/ui/contrats";
    }

    @GetMapping("/ui/contrats/terminer/{id}")
    public String terminerContrat(@PathVariable Long id) {
        contratService.terminerContrat(id);
        return "redirect:/ui/contrats";
    }

    @GetMapping("/ui/contrats/random")
    public String randomContrat() {
        try {
            dataGeneratorService.generateRandomContrat();
        } catch (Exception e) {
            return "redirect:/ui/contrats?error=" + e.getMessage();
        }
        return "redirect:/ui/contrats";
    }

    // Générer un scénario de démonstration
    @GetMapping("/ui/contrats/scenario/retard")
    public String generateRetardScenario() {
        dataGeneratorService.generateRetardScenario();
        return "redirect:/ui/contrats";
    }

    // Sauvegarder le contrat (Création + Mise à jour)
    @PostMapping("/ui/contrats")
    public String saveContrat(
            @RequestParam(required = false) Long id,
            @RequestParam Long clientId,
            @RequestParam Long vehiculeId,
            @RequestParam String dateDebut,
            @RequestParam String dateFin) {
        try {
            LocalDate start = LocalDate.parse(dateDebut);
            LocalDate end = LocalDate.parse(dateFin);

            if (id != null) {
                contratService.updateContrat(id, clientId, vehiculeId, start, end);
            } else {
                contratService.createContrat(clientId, vehiculeId, start, end);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/ui/contrats?error=collision";
        }
        return "redirect:/ui/contrats";
    }
}