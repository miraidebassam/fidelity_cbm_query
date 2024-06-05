package com.ogb.fidelity.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CDR {
    private Long id;
    private String cleRecharge;
    private float montantRecharge;
    private int numEnvoyeur;
    private int numEnvoye;
    private String IMSI;
    private String serviceName;
    private String transactionId;
    private LocalDateTime dateChargement;
    private String filename;
    private String channel;

    public CDR(String cleRecharge, float montantRecharge,
               int numEnvoyeur, String IMSI,
               String serviceName, String transactionId,
               LocalDateTime dateChargement, String filename, String channel) {
//      this.id = id;
        this.cleRecharge = cleRecharge;
        this.montantRecharge = montantRecharge;
        this.numEnvoyeur = numEnvoyeur;
        this.numEnvoye = numEnvoye;
        this.IMSI = IMSI;
        this.serviceName = serviceName;
        this.transactionId = transactionId;
        this.dateChargement = dateChargement;
        this.filename = filename;
        this.channel = channel;
    }
}
