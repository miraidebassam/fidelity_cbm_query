package com.ogb.fidelity.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Offre {
    private int idOffre;
    private String cleOffre;
    private String description;
    private boolean isActive;
    private int nbrPointsOffert;
    private String nomOffre;
    private float prixOffre;
}
