package com.ogb.fidelity.config;

import java.sql.Connection;
import java.sql.DriverManager;

public class ConectionFidelityDatabase {
    static String url = "jdbc:mysql://127.0.0.1:3306/db_fidelity_ogb?allowPublicKeyRetrieval=true&useSSL=false";
    static String user = "root";
    static String pwd = "buildme";

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
