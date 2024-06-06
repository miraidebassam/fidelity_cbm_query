package com.ogb.fidelity;

import com.ogb.fidelity.entities.CDR;
import com.ogb.fidelity.utils.RequetteSql;

import java.util.ArrayList;


public class Main {
    public static void main(String[] args) {
        ArrayList<CDR> listeCdr = new ArrayList<>();
        RequetteSql requetteSql = new RequetteSql();
        requetteSql.listCdr();
//        listeCdr.forEach(System.out::println);
    }













}