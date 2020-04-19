/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package condominiummanagement;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author Jozhua Ten
 */
public class Database {
    public static Connection conn;
    
    // Open connection to database
    public static boolean openConnection() {
        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/jp?zeroDateTimeBehavior=convertToNull&serverTimezone=UTC", "root", "root");
            return true;
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            return false;
        }
    }
    
    // Close connection to database
    public static boolean closeConnection() {
        try {
            conn.close();
            return true;
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            return false;
        }
    }
}
