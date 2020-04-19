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
public class UnitsList {
    private String unit_id, date, user_id;
    private int price;
    
    public void setUnitId(String unit_id) {
        this.unit_id = unit_id;
    }
    
    public void setPriceAmount(int price) {
        this.price = price;
    }
    
    public void setPriceDate(String date) {
        this.date = date;
    }
    
    public void setUserId(String user_id) {
        this.user_id = user_id;
    }
    
    public String getUnitId() {
        return unit_id;
    }
    
    public int getPriceAmount() {
        return price;
    }
    
    public String getPriceDate() {
        return date;
    }
    
    public String getUserId() {
        return user_id;
    }
}
