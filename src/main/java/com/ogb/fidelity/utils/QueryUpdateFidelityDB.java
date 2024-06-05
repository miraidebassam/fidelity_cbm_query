package com.ogb.fidelity.utils;

import com.ogb.fidelity.config.ConectionFidelityDatabase;
import com.ogb.fidelity.config.ConnectionCBMDatabase;
import com.ogb.fidelity.entities.CDR;
import com.ogb.fidelity.entities.Client;
import com.ogb.fidelity.entities.Offre;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class QueryUpdateFidelityDB {
    Connection con = null;

    public void insertDataCDR(List<CDR> list) {
        try {
            System.out.println("insertDataCDR:::::: ");
            con = ConectionFidelityDatabase.initiate();

            String query = "INSERT INTO `cbm`.`cbmzte_recharge_tbl` (`cle_recharge`, `montant_recharge`, `num_envoyeur`, `num_envoye`,\n"
                    + "`imsi`, `service`, `transaction_id`, `date_chargement`, `filename`, `channel`) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
            PreparedStatement statement = con.prepareStatement(query);
            for (CDR cdr : list) {
                statement.setString(1, cdr.getCleRecharge());
                statement.setFloat(2, cdr.getMontantRecharge());
                statement.setInt(3, cdr.getNumEnvoyeur());
                statement.setInt(4, cdr.getNumEnvoye());
                statement.setString(5, cdr.getIMSI());
                statement.setString(6, cdr.getServiceName());
                statement.setString(7, cdr.getTransactionId());
                statement.setTimestamp(8, Timestamp.valueOf(cdr.getDateChargement()));
                statement.setString(9, cdr.getFilename());
                statement.setString(10, cdr.getChannel());
                statement.addBatch();
            }
            statement.executeBatch();
            System.out.println("insertDataPurcharge succcess:::::: ");
            con.close();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
}
