/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package condominiummanagement;

import static condominiummanagement.Database.closeConnection;
import static condominiummanagement.Database.conn;
import static condominiummanagement.Database.openConnection;
import static condominiummanagement.Validation.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author Jozhua Ten
 */
public class RegisterResident extends javax.swing.JFrame {
    private boolean error = false;

    /**
     * Creates new form RegisterStudent
     */
    public RegisterResident() {
        initComponents();
        loadUnits();
    }
    
    private boolean multiValidate() {
        if (!validateUserId(txtUserId.getText()) && validateUserIdExist(txtUserId.getText())) {
            if (!getUserIdErrorMessage().equals("")) {
                lblUserIdError.setText(getUserIdErrorMessage());
            } else if (!getErrorMessage().equals("")) {
                lblError.setText(getErrorMessage());
            } else {
                lblUserIdError.setText("User ID is registered.");
            }
        }
        if (!validateName(txtName.getText())) {
            lblNameError.setText(getNameErrorMessage());
        }
        if (!validateNumber(txtNumber.getText())) {
            lblNumberError.setText(getNumberErrorMessage());
        }
        if (!validateEmail(txtEmail.getText())) {
            lblEmailError.setText(getEmailErrorMessage());
        }
        return (!validateUserIdExist(txtUserId.getText()) && validateName(txtName.getText()) && validateNumber(txtNumber.getText()) && validateEmail(txtEmail.getText()));
    }
    
    private void loadUnits() {
        error = false;

        PreparedStatement units_sql;
        ResultSet units_result;
        boolean unit_available = false;

        if (!openConnection()) {
            lblError.setText("Error connecting to database.");
            error = true;
        }

        if (!error) {
            try {
                units_sql = conn.prepareStatement("SELECT Unit_ID FROM units WHERE Unit_ID NOT IN (SELECT Unit_ID FROM users_units WHERE UserUnit_TerminateDate IS NULL)");
                units_result = units_sql.executeQuery();

                while (units_result.next()) {
                    cmbUnit.addItem(units_result.getString("Unit_ID"));
                    unit_available = true;
                }

                if (!unit_available) {
                    txtUserId.setEnabled(false);
                    txtName.setEnabled(false);
                    txtNumber.setEnabled(false);
                    txtEmail.setEnabled(false);
                    cmbUnit.setEnabled(false);

                    lblError.setText("No units available.");
                }
            } catch (SQLException ex) {
                lblError.setText(ex.getMessage());
            } finally {
                if (!closeConnection()) {
                    lblError.setText("Error closing database connection.");
                }
            }
        }
    }

    private void register(String user_id, String name, String number, String email, String unit_id) {
        error = false;

        PreparedStatement register_sql, occupyunit_sql;

        if (!openConnection()) {
            lblError.setText("Error connecting to database.");
            error = true;
        }

        if (!error) {
            try {
                register_sql = conn.prepareStatement("INSERT INTO users VALUES(?, ?, ?, ?, ?, 1, 0)");
                register_sql.setString(1, user_id.toUpperCase());
                register_sql.setString(2, name);
                register_sql.setString(3, BCrypt.hashpw(user_id.toUpperCase(), BCrypt.gensalt()));
                register_sql.setString(4, number);
                register_sql.setString(5, email);
                if (register_sql.executeUpdate() == 1) {
                    occupyunit_sql = conn.prepareStatement("INSERT INTO users_units VALUES(NULL, ?, ?, CURRENT_DATE(), NULL)");
                    occupyunit_sql.setString(1, user_id.toUpperCase());
                    occupyunit_sql.setString(2, unit_id);
                    if(occupyunit_sql.executeUpdate() == 1) {
                        this.dispose();

                        Residents searchresident_form = new Residents();
                        searchresident_form.setVisible(true);
                        searchresident_form.setLocationRelativeTo(null);
                    } else {
                        lblError.setText("Error occupying unit.");
                    }
                } else {
                    lblError.setText("Registering account.");
                }
            } catch (SQLException ex) {
                lblError.setText(ex.getMessage());
            } finally {
                if (!closeConnection()) {
                    lblError.setText("Error closing database connection.");
                }
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        txtUserId = new javax.swing.JTextField();
        txtName = new javax.swing.JTextField();
        txtNumber = new javax.swing.JTextField();
        txtEmail = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        cmbUnit = new javax.swing.JComboBox<>();
        lblUserIdError = new javax.swing.JLabel();
        lblNameError = new javax.swing.JLabel();
        lblNumberError = new javax.swing.JLabel();
        lblEmailError = new javax.swing.JLabel();
        btnRegister = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        lblError = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Register");

        jPanel1.setBackground(new java.awt.Color(51, 153, 255));
        jPanel1.setMinimumSize(new java.awt.Dimension(600, 75));

        jLabel1.setFont(new java.awt.Font("Segoe UI Semibold", 0, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("REGISTER");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(jLabel1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(31, 31, 31)
                .addComponent(jLabel1)
                .addContainerGap())
        );

        jPanel2.setBackground(new java.awt.Color(55, 71, 79));
        jPanel2.setMinimumSize(new java.awt.Dimension(600, 325));

        txtUserId.setBackground(new java.awt.Color(55, 71, 79));
        txtUserId.setFont(new java.awt.Font("Segoe UI Semilight", 0, 14)); // NOI18N
        txtUserId.setForeground(new java.awt.Color(255, 255, 255));
        txtUserId.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtUserId.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "USER ID", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI Semilight", 0, 12), new java.awt.Color(255, 255, 255))); // NOI18N
        txtUserId.setCaretColor(new java.awt.Color(255, 255, 255));
        txtUserId.setMinimumSize(new java.awt.Dimension(250, 30));
        txtUserId.setPreferredSize(new java.awt.Dimension(250, 50));
        txtUserId.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtUserIdFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtUserIdFocusLost(evt);
            }
        });
        txtUserId.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtUserIdKeyReleased(evt);
            }
        });

        txtName.setBackground(new java.awt.Color(55, 71, 79));
        txtName.setFont(new java.awt.Font("Segoe UI Semilight", 0, 14)); // NOI18N
        txtName.setForeground(new java.awt.Color(255, 255, 255));
        txtName.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtName.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "NAME", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI Semilight", 0, 12), new java.awt.Color(255, 255, 255))); // NOI18N
        txtName.setCaretColor(new java.awt.Color(255, 255, 255));
        txtName.setPreferredSize(new java.awt.Dimension(550, 50));
        txtName.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtNameFocusLost(evt);
            }
        });
        txtName.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtNameKeyReleased(evt);
            }
        });

        txtNumber.setBackground(new java.awt.Color(55, 71, 79));
        txtNumber.setFont(new java.awt.Font("Segoe UI Semilight", 0, 14)); // NOI18N
        txtNumber.setForeground(new java.awt.Color(255, 255, 255));
        txtNumber.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtNumber.setText("000-00000000");
        txtNumber.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "CONTACT NUMBER", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI Semilight", 0, 12), new java.awt.Color(255, 255, 255))); // NOI18N
        txtNumber.setCaretColor(new java.awt.Color(255, 255, 255));
        txtNumber.setMinimumSize(new java.awt.Dimension(250, 30));
        txtNumber.setPreferredSize(new java.awt.Dimension(250, 50));
        txtNumber.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtNumberFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtNumberFocusLost(evt);
            }
        });
        txtNumber.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtNumberKeyReleased(evt);
            }
        });

        txtEmail.setBackground(new java.awt.Color(55, 71, 79));
        txtEmail.setFont(new java.awt.Font("Segoe UI Semilight", 0, 14)); // NOI18N
        txtEmail.setForeground(new java.awt.Color(255, 255, 255));
        txtEmail.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtEmail.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "EMAIL ADDRESS", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI Semilight", 0, 12), new java.awt.Color(255, 255, 255))); // NOI18N
        txtEmail.setCaretColor(new java.awt.Color(255, 255, 255));
        txtEmail.setMinimumSize(new java.awt.Dimension(250, 30));
        txtEmail.setPreferredSize(new java.awt.Dimension(250, 50));
        txtEmail.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtEmailFocusLost(evt);
            }
        });
        txtEmail.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtEmailKeyReleased(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Segoe UI Semilight", 0, 12)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("UNIT");

        cmbUnit.setBackground(new java.awt.Color(55, 71, 79));
        cmbUnit.setFont(new java.awt.Font("Segoe UI Semilight", 0, 14)); // NOI18N
        cmbUnit.setForeground(new java.awt.Color(255, 255, 255));
        cmbUnit.setPreferredSize(new java.awt.Dimension(250, 30));

        lblUserIdError.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        lblUserIdError.setForeground(new java.awt.Color(255, 51, 51));
        lblUserIdError.setPreferredSize(new java.awt.Dimension(250, 20));

        lblNameError.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        lblNameError.setForeground(new java.awt.Color(255, 51, 51));
        lblNameError.setPreferredSize(new java.awt.Dimension(550, 20));

        lblNumberError.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        lblNumberError.setForeground(new java.awt.Color(255, 51, 51));
        lblNumberError.setPreferredSize(new java.awt.Dimension(250, 20));

        lblEmailError.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        lblEmailError.setForeground(new java.awt.Color(255, 51, 51));
        lblEmailError.setPreferredSize(new java.awt.Dimension(250, 20));

        btnRegister.setBackground(new java.awt.Color(51, 153, 255));
        btnRegister.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        btnRegister.setForeground(new java.awt.Color(255, 255, 255));
        btnRegister.setText("REGISTER");
        btnRegister.setBorderPainted(false);
        btnRegister.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnRegister.setFocusPainted(false);
        btnRegister.setPreferredSize(new java.awt.Dimension(250, 50));
        btnRegister.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegisterActionPerformed(evt);
            }
        });

        btnCancel.setBackground(new java.awt.Color(84, 110, 122));
        btnCancel.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        btnCancel.setForeground(new java.awt.Color(255, 255, 255));
        btnCancel.setText("CANCEL");
        btnCancel.setBorderPainted(false);
        btnCancel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnCancel.setFocusPainted(false);
        btnCancel.setPreferredSize(new java.awt.Dimension(250, 50));
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });

        lblError.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        lblError.setForeground(new java.awt.Color(255, 51, 51));
        lblError.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblError.setPreferredSize(new java.awt.Dimension(250, 20));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(txtName, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(lblNumberError, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(txtNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGap(50, 50, 50)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(lblEmailError, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addComponent(lblNameError, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(lblError, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(300, 300, 300)
                                .addComponent(btnRegister, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(cmbUnit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblUserIdError, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtUserId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addComponent(jLabel2)))))
                .addGap(25, 25, 25))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(txtUserId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(lblUserIdError, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(15, 15, 15)
                .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(lblNameError, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(15, 15, 15)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(5, 5, 5)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblNumberError, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblEmailError, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(15, 15, 15)
                .addComponent(jLabel2)
                .addGap(5, 5, 5)
                .addComponent(cmbUnit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(32, 32, 32)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnRegister, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(5, 5, 5)
                .addComponent(lblError, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        this.dispose();
        
        Residents searchresident_form = new Residents();
        searchresident_form.setVisible(true);
        searchresident_form.setLocationRelativeTo(null);
    }//GEN-LAST:event_btnCancelActionPerformed

    private void btnRegisterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegisterActionPerformed
        if (multiValidate()) {
            register(txtUserId.getText(), txtName.getText(), txtNumber.getText(), txtEmail.getText(), cmbUnit.getItemAt(cmbUnit.getSelectedIndex()));
        }
    }//GEN-LAST:event_btnRegisterActionPerformed

    private void txtNameFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtNameFocusLost
        if (!validateName(txtName.getText())) {
            lblNameError.setText(getNameErrorMessage());
        }
    }//GEN-LAST:event_txtNameFocusLost

    private void txtEmailFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtEmailFocusLost
        if (!validateEmail(txtEmail.getText())) {
            lblEmailError.setText(getEmailErrorMessage());
        }
    }//GEN-LAST:event_txtEmailFocusLost

    private void txtUserIdFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtUserIdFocusLost
        if (txtUserId.getText().trim().equals("TP")) {
            txtUserId.setText("");
        }
        if (!validateUserId(txtUserId.getText()) && validateUserIdExist(txtUserId.getText())) {
            if (!getUserIdErrorMessage().equals("")) {
                lblUserIdError.setText(getUserIdErrorMessage());
            } else if (!getErrorMessage().equals("")) {
                lblError.setText(getErrorMessage());
            } else {
                lblUserIdError.setText("User ID is registered.");
            }
        }
    }//GEN-LAST:event_txtUserIdFocusLost

    private void txtNumberFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtNumberFocusGained
        if (txtNumber.getText().equals("000-00000000")) {
            txtNumber.setText("");
        }
    }//GEN-LAST:event_txtNumberFocusGained

    private void txtNumberFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtNumberFocusLost
        if (txtNumber.getText().trim().equals("")) {
            txtNumber.setText("000-00000000");
        }
        if (!validateNumber(txtNumber.getText())) {
            lblNumberError.setText(getNumberErrorMessage());
        }
    }//GEN-LAST:event_txtNumberFocusLost

    private void txtUserIdKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtUserIdKeyReleased
        lblUserIdError.setText("");
    }//GEN-LAST:event_txtUserIdKeyReleased

    private void txtNameKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtNameKeyReleased
        lblNameError.setText("");
    }//GEN-LAST:event_txtNameKeyReleased

    private void txtNumberKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtNumberKeyReleased
        lblNumberError.setText("");
    }//GEN-LAST:event_txtNumberKeyReleased

    private void txtEmailKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtEmailKeyReleased
        lblEmailError.setText("");
    }//GEN-LAST:event_txtEmailKeyReleased

    private void txtUserIdFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtUserIdFocusGained
        if (txtUserId.getText().trim().isEmpty()) {
            txtUserId.setText("TP");
        }
    }//GEN-LAST:event_txtUserIdFocusGained

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("NimbusLookAndFeel".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(StaffMenu.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new RegisterResident().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnRegister;
    private javax.swing.JComboBox<String> cmbUnit;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel lblEmailError;
    private javax.swing.JLabel lblError;
    private javax.swing.JLabel lblNameError;
    private javax.swing.JLabel lblNumberError;
    private javax.swing.JLabel lblUserIdError;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtName;
    private javax.swing.JTextField txtNumber;
    private javax.swing.JTextField txtUserId;
    // End of variables declaration//GEN-END:variables
}
