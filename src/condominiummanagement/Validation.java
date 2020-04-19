/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package condominiummanagement;

import static condominiummanagement.Database.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Jozhua Ten
 */
public class Validation {
    private static String iderror_message, idexisterror_message, idactivatederror_message, passworderror_message, nameerror_message, numbererror_message, emailerror_message, amountpaiderror_message, unitiderror_message, dateerror_message, priceerror_message;
    private static String error_message = "";
    
    // Validate User ID format
    public static boolean validateUserId(String user_id) {
        if (user_id.trim().isEmpty()) {
            iderror_message = "User ID cannot be empty.";
            return false;
        } else if (!user_id.trim().matches("[tT][pP][0-9]{6}")) {
            iderror_message = "User ID is invalid.";
            return false;
        }
        iderror_message = "";
        return true;
    }
    
    public static String getUserIdErrorMessage() {
        return iderror_message;
    }
    
    // Validate whether User ID exists
    public static boolean validateUserIdExist(String user_id) {
        if (!validateUserId(user_id)) {
            idexisterror_message = iderror_message;
            return false;
        }
        error_message = "";
        PreparedStatement registered_sql;
        ResultSet registered_result;

        if (openConnection()) {
            try {
                registered_sql = conn.prepareStatement("SELECT User_ID FROM users WHERE User_ID = ?");
                registered_sql.setString(1, user_id.trim().toUpperCase());
                registered_result = registered_sql.executeQuery();

                if (registered_result.next()) {
                    idexisterror_message = "";
                    return true;
                }
            } catch (SQLException ex) {
                error_message = ex.getMessage();
                return false;
            } finally {
                if (!closeConnection()) {
                    error_message = "Error closing database connection.";
                }
            }
        } else {
            error_message = "Error connecting to database.";
            return false;
        }
        idexisterror_message = "User ID does not exists.";
        error_message = "";
        return false;
    }
    
    public static String getUserIdExistErrorMessage() {
        return idexisterror_message;
    }
    
    // Validate whether User is active
    public static boolean validateUserActivated(String user_id) {
        if (!validateUserId(user_id)) {
            idactivatederror_message = iderror_message;
            return false;
        }
        if (!validateUserIdExist(user_id)) {
            idactivatederror_message = idexisterror_message;
            return false;
        }
        error_message = "";
        PreparedStatement activated_sql;
        ResultSet activated_result;

        if (openConnection()) {
            try {
                activated_sql = conn.prepareStatement("SELECT User_ID FROM users WHERE User_ID = ? AND User_Deactivated = 1");
                activated_sql.setString(1, user_id.trim().toUpperCase());
                activated_result = activated_sql.executeQuery();

                if (activated_result.next()) {
                    idactivatederror_message = "User is deactivated.";
                    return false;
                }
            } catch (SQLException ex) {
                error_message = ex.getMessage();
                return false;
            } finally {
                if (!closeConnection()) {
                    error_message = "Error closing database connection.";
                }
            }
        } else {
            error_message = "Error connecting to database.";
            return false;
        }
        idactivatederror_message = "";
        error_message = "";
        return true;
    }
    
    public static String getUserActivatedErrorMessage() {
        return idactivatederror_message;
    }
    
    // Validate User Password format
    public static boolean validatePassword(String password) {
        if (password.trim().isEmpty()) {
            passworderror_message = "Password cannot be empty.";
            return false;
        } else if (password.trim().length() < 6) {
            passworderror_message = "Password must be at least 6 characters.";
            return false;
        } else if (password.trim().length() > 18) {
            passworderror_message = "Password cannot be longer than 18 characters";
            return false;
        }
        passworderror_message = "";
        return true;
    }
    
    public static String getPasswordErrorMessage() {
        return passworderror_message;
    }

    // Validate User Name format
    public static boolean validateName(String name) {
        if (name.trim().isEmpty()) {
            nameerror_message = "Name cannot be empty.";
            return false;
        } else if (name.trim().length() < 3) {
            nameerror_message = "Name cannot be shorter than 3 characters.";
            return false;
        } else if (name.trim().length() > 255) {
            nameerror_message = "Name cannot be longer than 255 characters.";
            return false;
        } else if (!name.trim().matches("(-?([A-Z].\\s)?([A-Z][a-z]+)\\s?)+([A-Z]'([A-Z][a-z]+))?")) {
            nameerror_message = "Name can only be in alphabets and initials must be capitalised.";
            return false;
        }
        nameerror_message = "";
        return true;
    }
    
    public static String getNameErrorMessage() {
        return nameerror_message;
    }
    
    // Validate User Contact Number format
    public static boolean validateNumber(String number) {
        if (number.trim().isEmpty() || number.trim().equals("000-00000000")) {
            numbererror_message = "Contact number cannot be empty.";
            return false;
        } else if (!number.trim().matches("^(01[2-9]-)?(\\d{7})$") && !number.trim().matches("^(011-)?(\\d{8})$")) {
            numbererror_message = "Contact number not in correct format.";
            return false;
        }
        numbererror_message = "";
        return true;
    }
    
    public static String getNumberErrorMessage() {
        return numbererror_message;
    }

    // Validate User Email Address format
    public static boolean validateEmail(String email) {
        if (email.trim().isEmpty()) {
            emailerror_message = "Email address cannot be empty.";
            return false;
        } else if (email.trim().length() > 255) {
            emailerror_message = "Email cannot be longer than 255 characters.";
            return false;
        } else if (!email.trim().matches("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$")) {
            emailerror_message = "Email not in correct format.";
            return false;
        }
        emailerror_message = "";
        return true;
    }
    
    public static String getEmailErrorMessage() {
        return emailerror_message;
    }
    
    // Validate Amount Paid
    public static boolean validateAmountPaid(String fees, String paid) {
        try {
            Double outstanding_fees = Double.parseDouble(fees.trim());
            Double amount_paid = Double.parseDouble(paid.trim());
            if (amount_paid < outstanding_fees) {
                amountpaiderror_message = "Cannot pay less than outstanding fees."; 
                return false;
            } else if (amount_paid < 0) {
                amountpaiderror_message = "Cannot pay less than zero.";
                return false;
            }
        } catch (NumberFormatException ex) {
            amountpaiderror_message = "Please enter a number.";
            return false;
        }
        return true;
    }
    
    public static String getAmountPaidErrorMessage() {
        return amountpaiderror_message;
    }
    
    // Validate Unit ID format
    public static boolean validateUnitId(String unit_id) {
        error_message = "";
        PreparedStatement unitid_sql;
        ResultSet unitid_result;

        if (openConnection()) {
            try {
                unitid_sql = conn.prepareStatement("SELECT Unit_ID FROM units WHERE Unit_ID = ?");
                unitid_sql.setString(1, unit_id.trim().toUpperCase());
                unitid_result = unitid_sql.executeQuery();

                if (unitid_result.next()) {
                    error_message = "";
                    unitiderror_message = "";
                    return true;
                } else {
                    unitiderror_message = "Unit ID is invalid.";
                }
            } catch (SQLException ex) {
                error_message = ex.getMessage();
                return false;
            } finally {
                if (!closeConnection()) {
                    error_message = "Error closing database connection.";
                }
            }
        } else {
            error_message = "Error connecting to database.";
            return false;
        }
        return false;
    }
    
    public static String getUnitIdErrorMessage() {
        return unitiderror_message;
    }
    
    // Validate Price Date format
    public static boolean validateDate(String date) {
        if (date.trim().equals("") || date.trim().equals("YYYY-MM")) {
            dateerror_message = "Date cannot be empty.";
            return false;
        } else if (!date.trim().matches("([1-2][0-9]{3})-(0[1-9]|1[012])")) {
            dateerror_message = "Date not in correct format.";
            return false;
        } else {
            try {
                SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM");
                Date date1 = dateformat.parse(date);
                Date date2 = new Date();
                
                if (date1.before(date2) || date1.equals(date2)) {
                    dateerror_message = "Date cannot be this month or before.";
                    return false;
                }
            } catch (ParseException ex) {
                dateerror_message = "Date not in correct format.";
                return false;
            }
        }
        return true;
    }
    
    public static String getDateErrorMessage() {
        return dateerror_message;
    }
    
    // Validate Unit Price
    public static boolean validatePrice(String unit_price) {
        try {
            double price = Double.parseDouble(unit_price.trim());
            if (price == 0 || price < 0) {
                priceerror_message = "Price cannot be zero or less.";
                return false;
            }
        } catch (NumberFormatException ex) {
            priceerror_message = "Please enter a number.";
            return false;
        }
        return true;
    }
    
    public static String getPriceErrorMessage() {
        return priceerror_message;
    }
    
    public static String getErrorMessage() {
        return error_message;
    }
}
