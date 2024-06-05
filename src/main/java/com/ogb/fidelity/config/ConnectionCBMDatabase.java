/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ogb.fidelity.config;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 *
 * @author alassanedoumbia
 */
public class ConnectionCBMDatabase {
    
    static String url = "jdbc:mysql://10.210.102.185:3306/cbm?allowPublicKeyRetrieval=true&useSSL=false";
    static String user = "cbm";
    static String pwd = "CbM2020$$";
    
    static Connection con = null;
    
    public static Connection initiate() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(url, user, pwd);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace(System.err);
        }
        return con;
    }
    
    public static void close(){
        try {
            con.close();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace(System.err);
        }
        
    }
}
