package com.bfb.rental.service;

import com.bfb.rental.entity.Client;
import com.bfb.rental.entity.Contrat;
import com.bfb.rental.entity.Vehicule;
import com.bfb.rental.enums.EtatContrat;
import com.bfb.rental.enums.EtatVehicule;
import com.bfb.rental.repository.ClientRepository;
import com.bfb.rental.repository.VehiculeRepository;
import com.bfb.rental.repository.ContratRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DataGeneratorService {

    private final ClientRepository clientRepository;
    private final VehiculeRepository vehiculeRepository;
    private final ContratRepository contratRepository;
    private final ContratService contratService;
    private final Random random = new Random();

    // === Client aléatoire ===
    public void generateRandomClient() {
        String[] noms = {"Dupont", "Martin", "Durand", "Lefebvre", "Moreau", "Dubois", "Garcia"};
        String[] prenoms = {"Jean", "Paul", "Marie", "Sophie", "Pierre", "Thomas", "Lucas"};
        
        Client c = new Client();
        c.setNom(noms[random.nextInt(noms.length)]);
        c.setPrenom(prenoms[random.nextInt(prenoms.length)]);
        c.setDateNaissance(LocalDate.of(1970 + random.nextInt(30), 1 + random.nextInt(12), 1 + random.nextInt(28)));
        c.setNumPermis("PERMIS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        c.setAdresse("Rue de Paris " + random.nextInt(100));
        clientRepository.save(c);
    }

    // === Véhicule aléatoire ===
    public void generateRandomVehicule() {
        String[] marques = {"Peugeot", "Renault", "Citroen", "Toyota", "Tesla", "BMW"};
        String[] modeles = {"208", "Clio", "C3", "Yaris", "Model 3", "Serie 1"};
        String[] couleurs = {"Blanc", "Noir", "Gris", "Bleu", "Rouge"};
        String[] moteurs = {"Essence", "Diesel", "Electrique", "Hybride"};

        Vehicule v = new Vehicule();
        v.setMarque(marques[random.nextInt(marques.length)]);
        v.setModele(modeles[random.nextInt(modeles.length)]);
        v.setCouleur(couleurs[random.nextInt(couleurs.length)]);
        v.setMotorisation(moteurs[random.nextInt(moteurs.length)]);
        v.setImmatriculation("AA-" + (100 + random.nextInt(899)) + "-BB");
        v.setDateAcquisition(LocalDate.now().minusDays(random.nextInt(1000)));
        v.setEtat(EtatVehicule.DISPONIBLE);
        
        // Logique simple pour éviter les doublons d'immatriculation
        if(!vehiculeRepository.existsByImmatriculation(v.getImmatriculation())) {
            vehiculeRepository.save(v);
        }
    }

    // === Contrat aléatoire ===
    public void generateRandomContrat() throws Exception {
        List<Client> clients = clientRepository.findAll();
        List<Vehicule> vehicules = vehiculeRepository.findAll();

        if (clients.isEmpty() || vehicules.isEmpty()) {
            throw new RuntimeException("Pas assez de données pour générer un contrat");
        }

        Client randomClient = clients.get(random.nextInt(clients.size()));
        Vehicule randomVehicule = vehicules.get(random.nextInt(vehicules.size()));

        // Date future aléatoire
        LocalDate start = LocalDate.now().plusDays(random.nextInt(30)); 
        LocalDate end = start.plusDays(1 + random.nextInt(10));

        // Appel du Service pour s'assurer de la détection des conflits
        contratService.createContrat(randomClient.getId(), randomVehicule.getId(), start, end);
    }

    /**
     * Générer un scénario spécifique de "test de retard"
     */
    public void generateRetardScenario() {
        // 1. Créer un véhicule de test dédié
        Vehicule v = new Vehicule();
        v.setMarque("TEST-CAR");
        v.setModele("Scenario-Retard");
        v.setImmatriculation("RETARD-" + random.nextInt(9999));
        v.setEtat(EtatVehicule.EN_LOCATION);
        v.setCouleur("Rouge");
        v.setMotorisation("Essence");
        v.setDateAcquisition(LocalDate.now().minusYears(1));
        vehiculeRepository.save(v);

        // 2. Créer un client
        Client c = clientRepository.findAll().stream().findFirst().orElse(null);
        if(c == null) {
            // S'il n'y a pas de client, en créer un
            c = new Client();
            c.setNom("TestUser");
            c.setPrenom("Demo");
            c.setNumPermis("TEST-PERMIS");
            c.setDateNaissance(LocalDate.of(1990, 1, 1));
            clientRepository.save(c);
        }

        // 3. Créer le contrat A (le fautif) :
        Contrat c1 = new Contrat();
        c1.setClient(c);
        c1.setVehicule(v);
        c1.setDateDebut(LocalDate.now().minusDays(5));
        c1.setDateFin(LocalDate.now().minusDays(1)); // Aurait dû se terminer hier
        c1.setEtat(EtatContrat.EN_COURS); // Toujours pas rendu !
        contratRepository.save(c1);

        // 4. Créer le contrat B (la victime) :
        Contrat c2 = new Contrat();
        c2.setClient(c);
        c2.setVehicule(v);
        
        // Cela rend la démo plus fluide et la logique plus claire : "Comme il n'a pas été rendu hier, on annule la réservation de demain".
        c2.setDateDebut(LocalDate.now().plusDays(1)); 
        c2.setDateFin(LocalDate.now().plusDays(4));
        
        c2.setEtat(EtatContrat.EN_ATTENTE);
        contratRepository.save(c2);
    }
}