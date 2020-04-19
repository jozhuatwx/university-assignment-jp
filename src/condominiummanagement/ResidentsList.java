/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package condominiummanagement;

/**
 *
 * @author Jozhua Ten
 */
public class ResidentsList {
    // Variables
    private String user_id, name, number, email, unit_id, register_date;

    public void setUserId(String user_id) {
        this.user_id = user_id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setContactNumber(String number) {
        this.number = number;
    }

    public void setEmailAddress(String email) {
        this.email = email;
    }

    public void setUnitId(String unit_id) {
        this.unit_id = unit_id;
    }

    public void setRegisterDate(String register_date) {
        this.register_date = register_date;
    }

    public String getUserId() {
        return user_id;
    }

    public String getName() {
        return name;
    }

    public String getContactNumber() {
        return number;
    }

    public String getEmailAddress() {
        return email;
    }

    public String getUnitId() {
        return unit_id;
    }

    public String getRegisterDate() {
        return register_date;
    }
}
