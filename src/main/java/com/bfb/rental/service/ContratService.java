package com.bfb.rental.service;

import com.bfb.rental.entity.Client;
import com.bfb.rental.entity.Contrat;
import com.bfb.rental.entity.Vehicule;
import com.bfb.rental.enums.EtatContrat;
import com.bfb.rental.enums.EtatVehicule;
import com.bfb.rental.event.VehiculePanneEvent;
import com.bfb.rental.repository.ClientRepository;
import com.bfb.rental.repository.ContratRepository;
import com.bfb.rental.repository.VehiculeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContratService {

    private final ContratRepository contratRepository;
    private final VehiculeRepository vehiculeRepository;
    private final ClientRepository clientRepository;



    private Double calculatePrice(Vehicule vehicule, LocalDate start, LocalDate end) {
        if (vehicule.getPrixJournee() == null) return 0.0;
        long days = ChronoUnit.DAYS.between(start, end);
        if (days == 0) days = 1;
        return days * vehicule.getPrixJournee();
    }


    @Transactional
    public Contrat createContrat(Long clientId, Long vehiculeId, LocalDate debut, LocalDate fin) {
        // 1. Validation de base
        if (fin.isBefore(debut)) {
            throw new IllegalArgumentException("La date de fin ne peut pas être antérieure à la date de début");
        }

        Vehicule vehicule = vehiculeRepository.findById(vehiculeId)
                .orElseThrow(() -> new RuntimeException("Le véhicule n'existe pas"));
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Le client n'existe pas"));

        // 2. Règle : Un véhicule en panne ne peut pas être loué
        if (vehicule.getEtat() == EtatVehicule.EN_PANNE) {
            throw new IllegalStateException("Ce véhicule est en panne et ne peut pas être loué");
        }

        // 3. Règle : Détection des conflits temporels
        List<Contrat> conflicts = contratRepository.findConflictingContrats(vehiculeId, debut, fin);
        if (!conflicts.isEmpty()) {
            throw new IllegalStateException("Ce véhicule est déjà réservé pendant la période spécifiée");
        }

        // 4. Créer et sauvegarder
        Contrat contrat = new Contrat();
        contrat.setClient(client);
        contrat.setVehicule(vehicule);
        contrat.setDateDebut(debut);
        contrat.setDateFin(fin);
        contrat.setEtat(EtatContrat.EN_ATTENTE);


        // 5. Calcul du prix total
        Double price = calculatePrice(vehicule, debut, fin);
        contrat.setPrixTotal(price);

        return contratRepository.save(contrat);
    }

    /**
     * Implémentation du pattern Observer :
     * Écoute l'événement VehiculePanneEvent.
     * Lorsqu'une notification de panne est reçue, annule automatiquement les contrats associés.
     */
    @EventListener
    @Transactional
    public void handleVehiculePanne(VehiculePanneEvent event) {
        System.out.println("Événement de panne de véhicule reçu, ID du véhicule : " + event.getVehiculeId());

        // Règle : Si un véhicule est en panne, tous les contrats EN_ATTENTE doivent être annulés
        List<Contrat> affectedContrats = contratRepository.findByVehiculeIdAndEtat(
                event.getVehiculeId(), 
                EtatContrat.EN_ATTENTE
        );

        for (Contrat c : affectedContrats) {
            c.setEtat(EtatContrat.ANNULE);
            System.out.println("Contrat automatiquement annulé, ID : " + c.getId());
        }
        
        contratRepository.saveAll(affectedContrats);
    }

    /**
     * Règle : Gestion du retard
     * Si le contrat actuel est en retard et affecte le contrat suivant, 
     * le contrat suivant est automatiquement annulé.
     */
    @Transactional
    public void declarerRetard(Long contratId) {
        Contrat currentContrat = contratRepository.findById(contratId)
            .orElseThrow(() -> new RuntimeException("Le contrat n'existe pas"));

        // 1. Marquer le contrat actuel comme étant en retard
        currentContrat.setEtat(EtatContrat.EN_RETARD);
        contratRepository.save(currentContrat);

        System.out.println("Le contrat " + contratId + " est en retard. Vérification des conflits...");

        // 2. [Stratégie agressive]
        // Tant que le véhicule n'est pas revenu, par sécurité, annuler tous les contrats "EN_ATTENTE" pour ce véhicule.
        List<Contrat> pendingContrats = contratRepository.findByVehiculeIdAndEtat(
            currentContrat.getVehicule().getId(),
            EtatContrat.EN_ATTENTE
        );

        for (Contrat nextContrat : pendingContrats) {
            nextContrat.setEtat(EtatContrat.ANNULE);
            System.out.println("Réaction en chaîne : Annulation automatique du contrat ID " + nextContrat.getId());
        }
        
        contratRepository.saveAll(pendingContrats);
    }


    @Transactional
    public void updateContrat(Long id, Long clientId, Long vehiculeId, LocalDate debut, LocalDate fin) {
        // 1. Validation de base
        if (fin.isBefore(debut)) {
            throw new IllegalArgumentException("La date de fin ne peut pas être antérieure à la date de début");
        }

        Contrat contrat = contratRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Le contrat n'existe pas"));

        // 2. Vérification des conflits
        List<Contrat> conflicts = contratRepository.findConflictingContrats(vehiculeId, debut, fin);
        
        // Parcourir la liste : si l'ID du contrat en conflit n'est pas celui qu'on modifie, c'est un vrai conflit
        for (Contrat c : conflicts) {
            if (!c.getId().equals(id)) {
                throw new IllegalStateException("Ce véhicule est déjà réservé pendant la période spécifiée (ID conflit : " + c.getId() + ")");
            }
        }

        // 3. Mise à jour des données
        Client client = clientRepository.findById(clientId).orElseThrow();
        Vehicule vehicule = vehiculeRepository.findById(vehiculeId).orElseThrow();

        contrat.setClient(client);
        contrat.setVehicule(vehicule);
        contrat.setDateDebut(debut);
        contrat.setDateFin(fin);
        
        Double price = calculatePrice(vehicule, debut, fin);
        contrat.setPrixTotal(price);

        contratRepository.save(contrat);
    }

    /**
     * Tâche planifiée : Mise à jour automatique de l'état du contrat
     * Logique : Si aujourd'hui est la date de début du contrat et que l'état est EN_ATTENTE -> passer à EN_COURS
     * En même temps : L'état du véhicule passe à EN_LOCATION
     * 
     * fixedRate = 5000 signifie une exécution toutes les 5 secondes.
     */
    @Scheduled(fixedRate = 5000)
    @Transactional
    public void updateContratStatusAutomatic() {
        LocalDate today = LocalDate.now();
        List<Contrat> contrats = contratRepository.findAll();

        for (Contrat c : contrats) {
            // Condition 1 : Doit être "EN_ATTENTE"
            // Condition 2 : La date de début est atteinte (ou passée)
            if (c.getEtat() == EtatContrat.EN_ATTENTE && !c.getDateDebut().isAfter(today)) {
                
                // Si le véhicule n'est pas revenu (ex: EN_LOCATION ou EN_PANNE), on ne peut absolument pas démarrer un nouveau contrat !
                // Le laisser en EN_ATTENTE, en attendant une annulation ou une intervention manuelle.
                if (c.getVehicule().getEtat() != EtatVehicule.DISPONIBLE) {
                    System.out.println("Attention : Le contrat " + c.getId() + " devrait commencer, mais le véhicule " + c.getVehicule().getImmatriculation() + " n'est pas disponible. Démarrage ignoré.");
                    continue; // Passer à l'itération suivante
                }

                // Si le véhicule est DISPONIBLE, démarrage normal
                c.setEtat(EtatContrat.EN_COURS);
                c.getVehicule().setEtat(EtatVehicule.EN_LOCATION);
                
                contratRepository.save(c);
                vehiculeRepository.save(c.getVehicule());
            }
        }
    }

    @Transactional
    public void terminerContrat(Long id) {
        Contrat contrat = contratRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Le contrat n'existe pas"));

        // Seuls les contrats "EN_COURS" ou "EN_RETARD" peuvent être terminés
        if (contrat.getEtat() == EtatContrat.EN_COURS || contrat.getEtat() == EtatContrat.EN_RETARD) {
            
            // 1. Terminer le contrat
            contrat.setEtat(EtatContrat.TERMINE);
            
            // 2. Libérer le véhicule
            Vehicule vehicule = contrat.getVehicule();
            // Seulement si le véhicule n'est pas "EN_PANNE", le rendre disponible (par sécurité)
            if (vehicule.getEtat() != EtatVehicule.EN_PANNE) {
                vehicule.setEtat(EtatVehicule.DISPONIBLE);
            }

            contratRepository.save(contrat);
            vehiculeRepository.save(vehicule);
            
            System.out.println("Le contrat " + id + " est terminé normalement, le véhicule est restitué.");
        }
    }
}