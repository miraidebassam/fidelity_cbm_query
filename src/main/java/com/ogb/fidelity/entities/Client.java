package com.ogb.fidelity.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class Client {
    private int idClient;
    private int numTelephone;
    private int comptPoints;
    private LocalDateTime dateMiseJour;
    private LocalDateTime dateFirstInscription;

    public Client(int idClient, int compteurPoints,
                  LocalDateTime dateFirstInscription,
                  LocalDateTime dateMiseJour, int numTelephone) {
        this.idClient = idClient;
        this.comptPoints = compteurPoints;
        this.dateFirstInscription = dateFirstInscription;
        this.dateMiseJour = dateMiseJour;
        this.numTelephone = numTelephone;
    }
}
