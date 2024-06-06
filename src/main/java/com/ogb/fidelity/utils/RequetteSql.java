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

    public List<CDR> listCdr() {
        ArrayList<CDR> listeM = new ArrayList<>();
        ArrayList<CDR> listeBDDFidelity = new ArrayList<>();
        //Recuperation de la liste des clienst isncrit dans le programme de fidelite
        ArrayList<Client> listeClients = new ArrayList<>();
        listeClients = (ArrayList<Client>) listClients();
        List<Integer> nombersPhone = new ArrayList<Integer>();
        listeClients.forEach(client ->
                nombersPhone.add(client.getNumTelephone()));
        String phoneNumbers = nombersPhone.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        //Recuperation des offres existants dans la BDD de fidelite
        ArrayList<Offre> listeOffre = new ArrayList<>();
        listeOffre = (ArrayList<Offre>) listOffres();
        listeOffre.forEach(System.out::println);
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
            System.out.println("sql cdr : " + query);
            // Préparer la requête
            PreparedStatement psHisot = con.prepareStatement(query);
            psHisot.setDate(1, Date.valueOf(today));
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
            //Ici recuperer la liste des cdr de la base de donnees fidelity
            listeBDDFidelity = getListCDRFidelityBase();
            addPtsToClient(listeBDDFidelity, listeClients, listeOffre);
            con.close();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        return listeM;
    }

    public ArrayList<CDR> getListCDRFidelityBase(){
        ArrayList<CDR> listeCdr = new ArrayList<>();
        try {
            con = ConectionFidelityDatabase.initiate();
            String query = "SELECT * FROM db_fidelity_ogb.t_recharge where isCDRProcessed = false;";
            // Préparer la requête
            PreparedStatement psHisot = con.prepareStatement(query);
            ResultSet rs = psHisot.executeQuery();
            //Je genere la cleUnique qui est une concatenation de numTel+Date+Hr+Mnt+Sec
            CDR cdr = null;
            while (rs.next()) {
                String cleRechargeUnique = rs.getString("cle_recharge");
                float montant = rs.getFloat("montant_recharge");
                int numEnvoyeur = rs.getInt("num_envoyeur");
                String imsi = rs.getString("imsi");
                String serviceName = rs.getString("service");
                String transaction_id = rs.getString("transaction_id");
                LocalDateTime date_chargement = rs.getTimestamp("date_chargement").toLocalDateTime();
                String filename = rs.getString("filename");
                String channel = rs.getString("channel");
                cdr = new CDR(cleRechargeUnique, montant, numEnvoyeur, imsi,
                        serviceName, transaction_id,
                        date_chargement, filename, channel);
                listeCdr.add(cdr);
            }
            con.close();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        return listeCdr;
    }

    public boolean insertIntoTableRecharge(ArrayList<CDR> listeM){
        String query = "INSERT IGNORE INTO db_fidelity_ogb.t_recharge \n" +
                " (cle_recharge, montant_recharge, num_envoyeur, imsi, service, transaction_id, date_chargement, filename, channel, isCDRProcessed) \n" +
                "VALUES (?,?,?,?,?,?,?,?,?,?)";

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
                psCdr.setBoolean(10, false);
                psCdr.addBatch(); // Ajouter la requête à un batch
            }
            int[] updateCounts = psCdr.executeBatch(); // Exécuter toutes les insertions en une fois
//            System.out.println("Inserts effectués: " + updateCounts.length);
            return true;
        } catch (SQLException e) {
            e.printStackTrace(System.err);
            return false;
        }
    }

    public List<Client> listClients() {
        ArrayList<Client> listeClient = new ArrayList<>();
        try {
            con = ConectionFidelityDatabase.initiate();
            String query = "SELECT * FROM db_fidelity_ogb.t_clients WHERE active=true;";
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
        listOffre.forEach(System.out::println);
        int compteurClient = 0;
        // Pour chaque CDR
        for (CDR cdr : listCdr) {
            if (!cdr.isCDRProcessed()){
                // Trouver le client correspondant
                Optional<Client> clientOpt = listClient.stream()
                        .filter(client -> client.getNumTelephone() == cdr.getNumEnvoyeur())
                        .findFirst();

                if (clientOpt.isPresent()) {
                    Client client = clientOpt.get();
                    compteurClient = client.getComptPoints();

                    client.setDateMiseJour(LocalDateTime.now());
                    float montantRecharge = cdr.getMontantRecharge();
                    String cleOffre = cdr.getChannel();
                    System.out.println("Montant recharge: "+montantRecharge);

                    // Trouver l'offre correspondante
                    Optional<Offre> offreOpt = listOffre.stream()
                            .filter(offre -> offre.getCleOffre().equals(cleOffre))
                            .findFirst();

                    if (offreOpt.isPresent()) {
                        Offre offre = offreOpt.get();
                        float prixOffre = offre.getPrixOffre();
                        System.out.println("prix: "+prixOffre);
                        // Calculer le nombre de points
                        int pointsToAdd = pointsToAdd(montantRecharge, offre.getPrixOffre(), offre.getNbrPointsOffert());
                        compteurClient += pointsToAdd;
                        client.setComptPoints(compteurClient);
                        System.out.println("Le compteur du client: "+ client.getComptPoints());
                        System.out.println("Le client: "+client.getNumTelephone()
                                +" a recharge: "+cdr.getMontantRecharge()+" et gagne: "+compteurClient+" pts"
                                +" le prix offre: "+offre.getPrixOffre()+" pour avoir "+offre.getNbrPointsOffert()+"pt");

                        //Ici je fais un update de la table t_client
                        updateTableClient(compteurClient, client.getNumTelephone(),
                                offre.getCleOffre(), cdr.getMontantRecharge(), Timestamp.valueOf(client.getDateMiseJour()));
                    }else{
                        System.out.println("L'offre n'est pas present!");
                    }
                }
            }
        }
        updateTableCdrToTrue(listCdr);
    }

    public void updateTableCdrToTrue(ArrayList<CDR> list){
        try {
            con = ConectionFidelityDatabase.initiate();
            String query = "UPDATE db_fidelity_ogb.t_recharge SET isCDRProcessed = TRUE WHERE cle_recharge = ?";
            PreparedStatement psUpdate = con.prepareStatement(query);

            for (CDR cdr : list) {
                psUpdate.setString(1, cdr.getCleRecharge());
                psUpdate.addBatch(); // Ajouter la requête à un batch
            }
            int[] updateCounts = psUpdate.executeBatch(); // Exécuter toutes les mises à jour en une fois
            System.out.println("Mises à jour effectuées : " + updateCounts.length);
            con.close();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    public void updateTableClient(int points, int numTelephone, String cleOffre, float montaRecharge, Timestamp dateDeChargement){
        String query = "UPDATE db_fidelity_ogb.t_clients SET compteur_points = ?, date_mise_jour = ? \n"
                + "WHERE num_telephone = ?;";
        String insertHistoriqueQuery = "INSERT INTO db_fidelity_ogb.t_historique \n" +
                "(code_operation, date_ajout, montant_operation, num_client, source, type_operation) \n" +
                "VALUES (?, ?, ?, ?, ?, ?)";
        con = ConectionFidelityDatabase.initiate();
        try {
            con.setAutoCommit(false); // Commencer une transaction
            // Insertion dans la table principale
            try (PreparedStatement psClient = con.prepareStatement(query)) {
                psClient.setInt(1, points);
                psClient.setTimestamp(2, dateDeChargement);
                psClient.setInt(3, numTelephone);
                psClient.executeUpdate();
            }

            // Insertion dans la table historique
            try (PreparedStatement psHistorique = con.prepareStatement(insertHistoriqueQuery)) {
                psHistorique.setString(1, cleOffre);
                psHistorique.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                psHistorique.setFloat(3, montaRecharge);
                psHistorique.setInt(4, numTelephone);
                psHistorique.setString(5, "");
                psHistorique.setString(6, "AJP");
                psHistorique.executeUpdate();
            }
            con.commit(); // Valider la transaction
        } catch (SQLException e) {
            try {
                con.rollback(); // Annuler la transaction en cas d'erreur
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace(System.err);
        }
    }

    public int pointsToAdd(float montantRecharge, float prixOffre, int nbrPointsOfferts){
        return (int) ((montantRecharge/prixOffre)*nbrPointsOfferts);
    }
}
