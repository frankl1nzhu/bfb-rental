package com.bfb.rental.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class CreateContratRequest {
    private Long clientId;
    private Long vehiculeId;
    private LocalDate dateDebut;
    private LocalDate dateFin;
}