package com.ogb.fidelity.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class ClientPointsInfo {
    private int points;
    private int numTelephone;
    private String cleOffre;
    private float montant;

}
