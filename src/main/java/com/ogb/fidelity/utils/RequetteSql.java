package com.ogb.fidelity.utils;

import com.ogb.fidelity.config.ConectionFidelityDatabase;
import com.ogb.fidelity.config.ConnectionCBMDatabase;
import com.ogb.fidelity.entities.CDR;
import com.ogb.fidelity.entities.Client;
import com.ogb.fidelity.entities.Offre;


import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class RequetteSql {
    Connection con = null;
    // Obtenir la date du jour
    LocalDate today = LocalDate.now();
    ArrayList<Client> listeDesClients = new ArrayList<>();
    public List<CDR> listCdr() {
        ArrayList<CDR> listeM = new ArrayList<>();
        //Recuperation de la liste des clienst isncrit dans le programme de fidelite
        ArrayList<Client> listeClients = new ArrayList<>();
        listeClients = (ArrayList<Client>) listClients();
        List<Integer> nombersPhone = new ArrayList<Integer>();
        listeClients.forEach(client ->
                nombersPhone.add(client.getNumTelephone()));
        nombersPhone.forEach(System.out::println);
        String phoneNumbers = nombersPhone.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        //Recuperation des offres existants dans la BDD de fidelite
        ArrayList<Offre> listeOffre = new ArrayList<>();
        listeOffre = (ArrayList<Offre>) listOffres();
        List<String> keysOffre = new ArrayList<String>();
        listeOffre.forEach(offre ->  keysOffre.add(offre.getCleOffre()));
        String keysOffreString = keysOffre.stream()
                .map(key -> "'" + key + "'")
                .collect(Collectors.joining(","));

        String cleRechargeUnique = "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH_mm_ss");
        try {
            con = ConnectionCBMDatabase.initiate();
            String query = "SELECT * \n"
                    + "FROM cbm.cbmzte_recharge_tbl \n"
                    + "WHERE DATE(date_chargement) = ? AND LoginType in ("+keysOffreString+") AND APARTY in ("+phoneNumbers+");";
//            System.out.println("sql cdr : " + query);
            // Préparer la requête
            PreparedStatement psHisot = con.prepareStatement(query);
            psHisot.setDate(1, java.sql.Date.valueOf(today));
            ResultSet rs = psHisot.executeQuery();

            //Je genere la cleUnique qui est une concatenation de numTel+Date+Hr+Mnt+Sec
            CDR cdr = null;
            while (rs.next()) {
                float montant = rs.getFloat("Amount");
                int numEnvoyeur = rs.getInt("APARTY");
                String imsi = rs.getString("IMSI");
                String serviceName = rs.getString("SERVICE");
                String transactionId = rs.getString("TRANSACTIONID");
                LocalDateTime dateChargement = rs.getTimestamp("date_chargement").toLocalDateTime();
                String finemane = rs.getString("FILENAME");
                String channel = rs.getString("channel");
                cleRechargeUnique = numEnvoyeur + "_" + dateChargement.format(formatter);
                cdr = new CDR(cleRechargeUnique, montant, numEnvoyeur, imsi,
                        serviceName, transactionId,
                        dateChargement, finemane, channel);
                listeM.add(cdr);
            }
            insertIntoTableRecharge(listeM);
            addPtsToClient(listeM,listeClients,listeOffre);
            listeM.forEach(System.out::println);
            con.close();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        return listeM;
    }

    public boolean insertIntoTableRecharge(ArrayList<CDR> listeM){
        String query = "INSERT IGNORE INTO db_fidelity_ogb.t_recharge \n" +
                " (cle_recharge, montant_recharge, num_envoyeur, imsi, service, transaction_id, date_chargement, filename, channel) \n" +
                "VALUES (?,?,?,?,?,?,?,?,?)";

        try (Connection con = ConectionFidelityDatabase.initiate();
             PreparedStatement psCdr = con.prepareStatement(query)) {
            for (CDR cdr : listeM) {
                psCdr.setString(1, cdr.getCleRecharge());
                psCdr.setFloat(2, cdr.getMontantRecharge());
                psCdr.setInt(3, cdr.getNumEnvoyeur());
                psCdr.setString(4, cdr.getIMSI());
                psCdr.setString(5, cdr.getServiceName());
                psCdr.setString(6, cdr.getTransactionId());
                psCdr.setTimestamp(7, Timestamp.valueOf(cdr.getDateChargement()));
                psCdr.setString(8, cdr.getFilename());
                psCdr.setString(9, cdr.getChannel());
                psCdr.addBatch(); // Ajouter la requête à un batch
            }

            int[] updateCounts = psCdr.executeBatch(); // Exécuter toutes les insertions en une fois

            System.out.println("Inserts effectués: " + updateCounts.length);
            return true;
        } catch (SQLException e) {
            e.printStackTrace(System.err);
            return false;
        }
    }

    public List<Client> listClients() {
        ArrayList<Client> listeClient = new ArrayList<>();
        ArrayList<Offre> offres = (ArrayList<Offre>) listOffres();
//        offres.forEach(System.out::println);
        try {
            con = ConectionFidelityDatabase.initiate();
            String query = "SELECT * FROM db_fidelity_ogb.t_clients WHERE active=true;";
//            System.out.println("sql cdr : " + query);
            // Préparer la requête
            PreparedStatement psHisot = con.prepareStatement(query);
            ResultSet rs = psHisot.executeQuery();

            //Je genere la cleUnique qui est une concatenation de numTel+Date+Hr+Mnt+Sec
            while (rs.next()) {
                int id_client = rs.getInt("id_client");
                int compteur_points = rs.getInt("compteur_points");
                int num_telephone = rs.getInt("num_telephone");
                LocalDateTime date_first_inscription = rs.getTimestamp("date_first_inscription").toLocalDateTime();
                LocalDateTime date_mise_jour = rs.getTimestamp("date_mise_jour").toLocalDateTime();
                Client client = new Client(id_client,compteur_points,date_first_inscription,date_mise_jour,num_telephone);
                listeClient.add(client);
            }
//            listeClient.forEach(System.out::println);
            con.close();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        return listeClient;
    }

    public List<Offre> listOffres() {
        ArrayList<Offre> listeOffre = new ArrayList<>();
        try {
            con = ConectionFidelityDatabase.initiate();
            String query = "SELECT * FROM db_fidelity_ogb.t_offres WHERE is_active = true;";
//            System.out.println("sql cdr : " + query);
            // Préparer la requête
            PreparedStatement psHisot = con.prepareStatement(query);
            ResultSet rs = psHisot.executeQuery();

            while (rs.next()) {
                int id_offre = rs.getInt("id_offre");
                String cle_offre = rs.getString("cle_offre");
                String description = rs.getString("description");
                boolean is_active = rs.getBoolean("is_active");
                int nbr_points_offert = rs.getInt("nbr_points_offert");
                String nom_offre = rs.getString("nom_offre");
                float prix_offre = rs.getFloat("prix_offre");
                Offre offre = new Offre(id_offre,cle_offre,description,is_active,
                        nbr_points_offert, nom_offre,prix_offre);
                listeOffre.add(offre);
            }
            con.close();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        return listeOffre;
    }

    public void addPtsToClient(ArrayList<CDR> listCdr, ArrayList<Client> listClient, ArrayList<Offre> listOffre){
        // Pour chaque CDR
        for (CDR cdr : listCdr) {
            // Trouver le client correspondant
            Optional<Client> clientOpt = listClient.stream()
                    .filter(client -> client.getNumTelephone() == cdr.getNumEnvoyeur())
                    .findFirst();

            if (clientOpt.isPresent()) {
                Client client = clientOpt.get();
//                client.setDateMiseJour(new Date());
                float montantRecharge = cdr.getMontantRecharge();
                String cleOffre = cdr.getChannel();

                // Trouver l'offre correspondante
                Optional<Offre> offreOpt = listOffre.stream()
                        .filter(offre -> offre.getCleOffre().equals(cleOffre))
                        .findFirst();

                if (offreOpt.isPresent()) {
                    Offre offre = offreOpt.get();
                    float prixOffre = offre.getPrixOffre();

                    // Calculer le nombre de points
                    int points = (int) ((montantRecharge/prixOffre)*offre.getNbrPointsOffert());

                    System.out.println("Le client: "+client.getNumTelephone()
                    +" a recharge: "+cdr.getMontantRecharge()+" et gagne: "+points+" pts"
                    +" le prix offre: "+offre.getPrixOffre()+" pour avoir "+offre.getNbrPointsOffert()+"pt");

                    //Ici je fais un update de la table t_client
                    updateTableClient(points, client.getNumTelephone(),
                            offre.getCleOffre(), cdr.getMontantRecharge(), Timestamp.valueOf(cdr.getDateChargement()));
                    // Ajouter les points au client
//                    client.getComptPoints()+=points;
                }
            }
        }
    }

    public void updateTableClient(int points, int numTelephone, String cleOffre, float montaRecharge, Timestamp dateDeChargement){
        String query = "UPDATE db_fidelity_ogb.t_clients SET compteur_points = ? \n"
                + "WHERE num_telephone = ?;";

        String insertHistoriqueQuery = "INSERT INTO db_fidelity_ogb.t_historique \n" +
                "(code_operation, montant_operation, num_client, source, type_operation) \n" +
                "VALUES (?, ?, ?, ?, ?)";

        con = ConectionFidelityDatabase.initiate();
        try {
            con.setAutoCommit(false); // Commencer une transaction
            // Insertion dans la table principale
            try (PreparedStatement psClient = con.prepareStatement(query)) {
                psClient.setInt(1, points);
                psClient.setTimestamp(2, dateDeChargement);
                psClient.setInt(2, numTelephone);
                psClient.executeUpdate();
            }

            // Insertion dans la table historique
            try (PreparedStatement psHistorique = con.prepareStatement(insertHistoriqueQuery)) {
                psHistorique.setString(1, cleOffre);
//                psHistorique.setTimestamp(2, new Timestamp(new Date().getTime()));
                psHistorique.setFloat(2, montaRecharge);
                psHistorique.setInt(3, numTelephone);
                psHistorique.setString(4, "");
                psHistorique.setString(5, "AJP");
                psHistorique.executeUpdate();
            }
            con.commit(); // Valider la transaction
//            System.out.println("Client et historique insérés avec succès.");
        } catch (SQLException e) {
            try {
                con.rollback(); // Annuler la transaction en cas d'erreur
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace(System.err);
        }















//        try {
//            con.setAutoCommit(false);// Commencer une transaction
//            try {
//                con = ConectionFidelityDatabase.initiate();
//
//                System.out.println("sql cdr : " + query);
//                // Préparer la requête
//                PreparedStatement psHisot = con.prepareStatement(query);
//                psHisot.setInt(1, points);
//                psHisot.setInt(2, numTelephone);
//                int affectedRows = psHisot.executeUpdate();
//                if (affectedRows > 0) {
//                    System.out.println("Clients mis à jour avec succès.");
//                } else {
//                    System.out.println("Aucun client trouvé avec ce numéro de téléphone.");
//                }
//                con.close();
//            } catch (Exception e) {
//                e.printStackTrace(System.err);
//            }
//        }catch (Exception e){
//            e.printStackTrace(System.err);
//        }
    }

    public int pointsToAdd(float montantRecharge, float prixOffre, int nbrPointsOfferts){
        return (int) ((montantRecharge/prixOffre)*nbrPointsOfferts);
    }

}
