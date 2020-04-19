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
public class TransactionsList {
    // Variables
    private int payment_id, amount;
    private String date, unit_id;

    public void setPaymentId(int payment_id) {
        this.payment_id = payment_id;
    }

    public void setPaymentAmount(int amount) {
        this.amount = amount;
    }

    public void setPaymentDate(String date) {
        this.date = date;
    }

    public void setUnitId(String unit_id) {
        this.unit_id = unit_id;
    }

    public int getPaymentId() {
        return payment_id;
    }

    public int getPaymentAmount() {
        return amount;
    }

    public String getPaymentDate() {
        return date;
    }

    public String getUnitId() {
        return unit_id;
    }
}
