package com.ogb.fidelity;

import com.ogb.fidelity.entities.CDR;
import com.ogb.fidelity.utils.RequetteSql;

import java.util.ArrayList;


public class Main {
    public static void main(String[] args) {
//        ArrayList<Client> listeClients = new ArrayList<>();
//        QueryUpdateFidelityDB requetteSql = new QueryUpdateFidelityDB();
//        listeClients = (ArrayList<Client>) requetteSql.listClients();
//        listeClients.forEach(System.out::println);
        ArrayList<CDR> listeCdr = new ArrayList<>();
        RequetteSql requetteSql = new RequetteSql();
        listeCdr = (ArrayList<CDR>) requetteSql.listCdr();
//        compareCDRToClient(listeCdr);
//        System.out.println("Je vous affice la liste des cdr: \n");
        listeCdr.forEach(System.out::println);

//        for (CDR cdr : listeCdr){
//            for (Client client : listeClients){
//                if (cdr.getNumEnvoyeur() == client.getNumTelephone()){
//                    System.out.println("EXISTE!");
//                }
////                System.out.println("N'existe pas!");
//            }
//        }


//        ArrayList<Integer> phoneNumbers = new ArrayList<>();
//        for (Client client : listeClients) {
//            phoneNumbers.add(client.getNumTelephone());
//        }
//        System.out.println("Je vous affice la liste des number phone: \n");
//        phoneNumbers.forEach(System.out::println);
    }

//    public static void compareCDRToClient(List<CDR> listCdr){
//        ArrayList<Client> listeClients = new ArrayList<>();
//        QueryUpdateFidelityDB queryUpdateFidelityDB = new QueryUpdateFidelityDB();
//        listeClients = (ArrayList<Client>) queryUpdateFidelityDB.listClients();
////      System.out.println("Je vous affice la liste des clients: \n");
////        listeClients.forEach(System.out::println);
////        listCdr.forEach(System.out::println);
//
//        for (CDR cdrItem : listCdr){
//            for (Client client : listeClients){
//                if (cdrItem.getNumEnvoyeur() == client.getNumTelephone()){
//                    System.out.println(cdrItem.getChannel()+" "+cdrItem.getMontantRecharge()
//                            +" "+cdrItem.getServiceName()+" "+cdrItem.getNumEnvoyeur());
//                }
////              System.out.println("N'existe pas!");
//            }
//        }
//    }
//













}