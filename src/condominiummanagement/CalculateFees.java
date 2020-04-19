/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package condominiummanagement;

import static condominiummanagement.Database.closeConnection;
import static condominiummanagement.Database.conn;
import static condominiummanagement.Database.openConnection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;

/**
 *
 * @author Jozhua Ten
 */
public class CalculateFees {
    private String user_id, user_name;
    private int userunit_id, total_fees, total_paid, outstanding_fees;
    private boolean error = false;
    private String error_message = "";
    
    CalculateFees(String user_id) {
        this.user_id = user_id.toUpperCase();
        totalFees();
        totalPaid();
    }
    
    // Calculate total fees
    private void totalFees() {
        PreparedStatement resident_sql, price_sql, month_sql;
        ResultSet resident_result, price_result, month_result;
        int total_months, total_days;
        String unit_id;
        Date register_date, first_month, second_month;
        
        total_fees = total_months = total_days = 0;
        second_month = null;
        
        if (openConnection()) {
            try {
                // Get resident's information
                resident_sql = conn.prepareStatement("SELECT User_Name, UserUnit_ID, Total_Months, UserUnit_RegisterDate, Unit_ID, DATEDIFF(CURRENT_DATE(), DATE_ADD(UserUnit_RegisterDate, INTERVAL Total_Months MONTH)) AS Total_Days FROM (SELECT User_Name, UserUnit_ID, UserUnit_RegisterDate, Unit_ID, TIMESTAMPDIFF(MONTH, UserUnit_RegisterDate, CURRENT_DATE()) AS Total_Months FROM users_units JOIN users ON users_units.User_ID = users.User_ID WHERE users.User_ID = ? AND UserUnit_TerminateDate IS NULL) AS a");
                resident_sql.setString(1, user_id.toUpperCase());
                resident_result = resident_sql.executeQuery();
                if (resident_result.next()) {
                    user_name = resident_result.getString("User_Name");
                    userunit_id = resident_result.getInt("UserUnit_ID");
                    total_months = resident_result.getInt("Total_Months");
                    total_days = resident_result.getInt("Total_Days");
                    register_date = resident_result.getDate("UserUnit_RegisterDate");
                    unit_id = resident_result.getString("Unit_ID");

                    // Get unit price (including previous prices)
                    price_sql = conn.prepareStatement("SELECT Price_Amount, Price_Date FROM price WHERE Unit_ID = ?");
                    price_sql.setString(1, unit_id);
                    price_result = price_sql.executeQuery();
                    
                    int count = 0;
                    // Default price for each unit is RM 200
                    int oldprice = 200;
                    int newprice = 0;

                    // Calculate unit price (if there's price change)
                    while (price_result.next()) {
                        // If first iteration, register date is the first date
                        if (count == 0) {
                            first_month = register_date;
                        } else {
                            first_month = second_month;
                        }
                        second_month = price_result.getDate("Price_Date");

                        // Get and save new price
                        newprice = price_result.getInt("Price_Amount");

                        // Calculate month difference
                        month_sql = conn.prepareStatement("SELECT TIMESTAMPDIFF(MONTH, ?, ?) AS Month");
                        month_sql.setDate(1, first_month);
                        month_sql.setDate(2, second_month);
                        month_result = month_sql.executeQuery();

                        // Calculate total fees for this period
                        if (month_result.next()) {
                            int months = month_result.getInt("Month");
                            total_fees += months * oldprice;
                            total_months -= months;
                        } else {
                            error = true;
                            error_message = "Error getting total fees.";
                        }
                        oldprice = newprice;
                        count++;
                    }
                    // If user is stays past three days, count as a month
                    if (count != 0) {
                        total_fees += total_months * newprice;
                        if (total_days > 3) {
                            total_fees += newprice;
                        }
                    // If no price change
                    } else {
                        total_fees += total_months * 200;
                        if (total_days > 3) {
                            total_fees += 200;
                        }
                    }
                } else {
                    error = true;
                    error_message = "User ID does not exist or is currently not renting";
                }
            } catch (SQLException ex) {
                error = true;
                error_message = ex.getMessage();
            } finally {
                closeConnection();
            }
        } else {
            error = true;
            error_message = "Error connecting to database.";
        }
    }
    
    // Calculate total amount paid
    private void totalPaid() {
        PreparedStatement amount_sql;
        ResultSet amount_result;
        total_paid = 0;
        
        if (openConnection()) {
            try {
                // Calculate total amount paid by the user thus far
                amount_sql = conn.prepareStatement("SELECT SUM(Payment_Amount) AS Total_Paid FROM payment WHERE UserUnit_ID = ?");
                amount_sql.setInt(1, userunit_id);
                amount_result = amount_sql.executeQuery();

                if (amount_result.next()) {
                    total_paid = amount_result.getInt("Total_Paid");
                } else {
                    error = true;
                    error_message = "Error getting total amount paid.";
                }
            } catch (SQLException ex) {
                error = true;
                error_message = ex.getMessage();
            } finally {
                closeConnection();
            }
        } else {
            error = true;
            error_message = "Error connecting to database.";
        }
    }
    
    public int getTotalFees() {
        return total_fees;
    }
    
    public int getTotalPaid() {
        return total_paid;
    }
    
    public int getOutstandingFees() {
        outstanding_fees = total_fees - total_paid;
        return outstanding_fees;
    }
    
    public boolean getError() {
        return error;
    }
    
    public String getErrorMessage() {
        return error_message;
    }
    
    public String getUserName() {
        return user_name;
    }
    
    public int getUserUnitId() {
        return userunit_id;
    }
}
