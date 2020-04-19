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
import java.awt.event.KeyEvent;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.SwingUtilities;

/**
 *
 * @author Jozhua Ten
 */
public class StaffUpdateResident extends javax.swing.JFrame {
    private String user_id = Residents.resident_id.toUpperCase();
    private String oldname, oldnumber, oldemail, oldunit_id;
    private boolean error = false;

    /**
     * Creates new form StaffUpdateResident
     */
    public StaffUpdateResident() {
        initComponents();
        loadResident();
        loadUnits();
        
        // Set default button
        SwingUtilities.getRootPane(btnUpdate).setDefaultButton(btnUpdate);
    }
    
    private boolean multiValidate() {
        if (!validateName(txtName.getText())) {
            lblNameError.setText(getNameErrorMessage());
        }
        if (!validateNumber(txtNumber.getText())) {
            lblNumberError.setText(getNumberErrorMessage());
        }
        if (!validateEmail(txtEmail.getText())) {
            lblEmailError.setText(getEmailErrorMessage());
        }
        return (validateName(txtName.getText()) && validateNumber(txtNumber.getText()) && validateEmail(txtEmail.getText()));
    }
    
    private void loadResident() {
        error = false;

        lblError.setText("");

        PreparedStatement resident_sql;
        ResultSet resident_result;

        if (!openConnection()) {
            lblError.setText("Error connecting to database.");
            error = true;
        }

        if (!error) {
            try {
                resident_sql = conn.prepareStatement("SELECT User_Name, User_ContactNumber, User_EmailAddress, IF(UserUnit_TerminateDate IS NULL, Unit_ID, \"-\") AS Unit_ID FROM users JOIN users_units ON users.User_ID = users_units.User_ID WHERE UserUnit_ID = (SELECT MAX(UserUnit_ID) FROM users_units WHERE User_ID = ?)");
                resident_sql.setString(1, user_id);
                resident_result = resident_sql.executeQuery();

                if (resident_result.next()) {
                    txtUserId.setText(user_id);

                    oldname = resident_result.getString("User_Name");
                    oldnumber = resident_result.getString("User_ContactNumber");
                    oldemail = resident_result.getString("User_EmailAddress");
                    oldunit_id = resident_result.getString("Unit_ID");

                    txtName.setText(oldname);
                    txtNumber.setText(oldnumber);
                    txtEmail.setText(oldemail);
                } else {
                    txtName.setEnabled(false);
                    txtNumber.setEnabled(false);
                    txtEmail.setEnabled(false);
                    cmbUnit.setEnabled(false);

                    lblError.setText("User ID not found.");
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
    
    private void loadUnits() {
        error = false;

        lblError.setText("");

        PreparedStatement units_sql;
        ResultSet units_result;
        boolean unit_available = false;

        if (!openConnection()) {
            lblError.setText("Error connecting to database.");
            error = true;
        }

        if (!error) {
            try {
                units_sql = conn.prepareStatement("SELECT Unit_ID FROM units WHERE Unit_ID NOT IN (SELECT Unit_ID FROM users_units WHERE UserUnit_TerminateDate IS NULL) OR Unit_ID = ?");
                units_sql.setString(1, oldunit_id);
                units_result = units_sql.executeQuery();
                
                while (units_result.next()) {
                    cmbUnit.addItem(units_result.getString("Unit_ID"));
                    unit_available = true;
                }
                if (!oldunit_id.equals("-")) {
                    cmbUnit.setSelectedItem(oldunit_id);
                } else if (!unit_available) {
                    cmbUnit.setEnabled(false);
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

    private void updateResident(String name, String number, String email, String unit_id) {
        boolean newname, newnumber, newemail, newunit_id, insertunitonly;
        newname = newnumber = newemail = newunit_id = insertunitonly = false;
        error = false;

        lblError.setText("");

        PreparedStatement update_sql, updateunit_sql, insertunit_sql;
        
        if (!oldname.equals(name)) {
            newname = true;
        }

        if (!oldnumber.equals(number)) {
            newnumber = true;
        }

        if (!oldemail.equals(email)) {
            newemail = true;
        }

        if (!oldunit_id.equals(unit_id)) {
            newunit_id = true;
        }

        if (!newname && !newnumber && !newemail && !newunit_id) {
            lblError.setText("No new details.");
            error = true;
        }

        if (!error) {
            try {
                if (newunit_id) {
                    CalculateFees calculate_fees = new CalculateFees(user_id.toUpperCase());
                    if (calculate_fees.getError()) {
                        String error_message = calculate_fees.getErrorMessage();
                        if (error_message.equals("User ID does not exist or is not currently renting")) {
                            if (validateUserIdExist(user_id.toUpperCase())) {
                                insertunitonly = true;
                            } else {
                                lblError.setText(error_message);
                                error = true;
                            }
                        } else {
                            lblError.setText(error_message);
                            error = true;
                        }
                    } else if (calculate_fees.getOutstandingFees() > 0) {
                        lblError.setText("Resident has outstanding fees.");
                        error = true;
                    }
                    
                    if (!error && !openConnection()) {
                        lblError.setText("Error connecting to database.");
                        error = true;
                    }

                    if (!error) {
                        insertunit_sql = conn.prepareStatement("INSERT INTO users_units VALUES(NULL, ?, ?, CURRENT_DATE(), NULL)");
                        insertunit_sql.setString(1, user_id);
                        insertunit_sql.setString(2, unit_id);
                        
                        if (insertunitonly) {
                            if (insertunit_sql.executeUpdate() == 1) {
                                this.dispose();

                                Residents staffresident_form = new Residents();
                                staffresident_form.setVisible(true);
                                staffresident_form.setLocationRelativeTo(null);
                            } else {
                                lblError.setText("Error registering resident into new unit.");
                            }
                        } else {
                            updateunit_sql = conn.prepareStatement("UPDATE users_units SET UserUnit_TerminateDate = CURRENT_DATE() WHERE User_ID = ? AND UserUnit_TerminateDate IS NULL");
                            updateunit_sql.setString(1, user_id);
                            if (updateunit_sql.executeUpdate() == 1) {
                                if (insertunit_sql.executeUpdate() != 1) {
                                    lblError.setText("Error registering resident into new unit.");
                                }
                            } else {
                                lblError.setText("Error deregistering resident from old unit.");
                            }
                        }
                    }
                }
                
                if (!error && (newname || newnumber || newemail)) {
                    int count = 0;
                    String sql = "UPDATE users SET";

                    if (newname) {
                        sql = sql.concat(" User_Name = ?");
                        count++;
                    }
                    if (newnumber) {
                        if (count >= 1) {
                            sql = sql.concat(",");
                        }
                        sql = sql.concat(" User_ContactNumber = ?");
                        count++;
                    }
                    if (newemail) {
                        if (count >= 1) {
                            sql = sql.concat(",");
                        }
                        sql = sql.concat(" User_EmailAddress = ?");
                        count++;
                    }
                    sql = sql.concat(" WHERE User_ID = ?");

                    update_sql = conn.prepareStatement(sql);
                    if (newname) {
                        update_sql.setString(1, name);
                    }
                    if (newnumber) {
                        int i;
                        if (newname) {
                            i = 2;
                        } else {
                            i = 1;
                        }
                        update_sql.setString(i, number);
                    }
                    if (newemail) {
                        update_sql.setString(count, email);
                    }
                    update_sql.setString(++count, user_id);
                    if (update_sql.executeUpdate() != 1) {
                        lblError.setText("Error updating account details.");
                        error = true;
                    }
                }
                if (!error) {
                    this.dispose();

                    Residents staffresident_form = new Residents();
                    staffresident_form.setVisible(true);
                    staffresident_form.setLocationRelativeTo(null);
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
        lblNameError = new javax.swing.JLabel();
        lblNumberError = new javax.swing.JLabel();
        lblEmailError = new javax.swing.JLabel();
        btnUpdate = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        lblError = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Update");

        jPanel1.setBackground(new java.awt.Color(51, 153, 255));
        jPanel1.setMinimumSize(new java.awt.Dimension(600, 75));

        jLabel1.setFont(new java.awt.Font("Segoe UI Semibold", 0, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("UPDATE");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(jLabel1)
                .addContainerGap(486, Short.MAX_VALUE))
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

        txtUserId.setEditable(false);
        txtUserId.setBackground(new java.awt.Color(55, 71, 79));
        txtUserId.setFont(new java.awt.Font("Segoe UI Semilight", 0, 14)); // NOI18N
        txtUserId.setForeground(new java.awt.Color(255, 255, 255));
        txtUserId.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtUserId.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "USER ID", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI Semilight", 0, 12), new java.awt.Color(255, 255, 255))); // NOI18N
        txtUserId.setCaretColor(new java.awt.Color(255, 255, 255));
        txtUserId.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        txtUserId.setPreferredSize(new java.awt.Dimension(250, 50));

        txtName.setBackground(new java.awt.Color(55, 71, 79));
        txtName.setFont(new java.awt.Font("Segoe UI Semilight", 0, 14)); // NOI18N
        txtName.setForeground(new java.awt.Color(255, 255, 255));
        txtName.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtName.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "NAME", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI Semilight", 0, 12), new java.awt.Color(255, 255, 255))); // NOI18N
        txtName.setCaretColor(new java.awt.Color(255, 255, 255));
        txtName.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        txtName.setMinimumSize(new java.awt.Dimension(250, 30));
        txtName.setPreferredSize(new java.awt.Dimension(250, 50));
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
        txtNumber.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
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
        txtEmail.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
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
        cmbUnit.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        cmbUnit.setMinimumSize(new java.awt.Dimension(250, 30));
        cmbUnit.setPreferredSize(new java.awt.Dimension(250, 30));

        lblNameError.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        lblNameError.setForeground(new java.awt.Color(255, 51, 51));
        lblNameError.setPreferredSize(new java.awt.Dimension(250, 20));

        lblNumberError.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        lblNumberError.setForeground(new java.awt.Color(255, 51, 51));
        lblNumberError.setPreferredSize(new java.awt.Dimension(250, 20));

        lblEmailError.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        lblEmailError.setForeground(new java.awt.Color(255, 51, 51));
        lblEmailError.setPreferredSize(new java.awt.Dimension(250, 20));

        btnUpdate.setBackground(new java.awt.Color(51, 153, 255));
        btnUpdate.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        btnUpdate.setForeground(new java.awt.Color(255, 255, 255));
        btnUpdate.setText("UPDATE");
        btnUpdate.setBorder(null);
        btnUpdate.setBorderPainted(false);
        btnUpdate.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnUpdate.setFocusPainted(false);
        btnUpdate.setPreferredSize(new java.awt.Dimension(250, 50));
        btnUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateActionPerformed(evt);
            }
        });

        btnCancel.setBackground(new java.awt.Color(84, 110, 122));
        btnCancel.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        btnCancel.setForeground(new java.awt.Color(255, 255, 255));
        btnCancel.setText("CANCEL");
        btnCancel.setBorder(null);
        btnCancel.setBorderPainted(false);
        btnCancel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnCancel.setFocusPainted(false);
        btnCancel.setPreferredSize(new java.awt.Dimension(250, 50));
        btnCancel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnCancelMouseClicked(evt);
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
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblError, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(300, 300, 300)
                        .addComponent(btnUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(cmbUnit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblNumberError, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtUserId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(50, 50, 50)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblEmailError, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jLabel2))
                    .addComponent(txtName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblNameError, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(25, 25, 25))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(txtUserId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(40, 40, 40)
                .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(lblNameError, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(15, 15, 15)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                    .addComponent(btnUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(5, 5, 5)
                .addComponent(lblError, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCancelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCancelMouseClicked
        this.dispose();
        
        Residents staffresident_form = new Residents();
        staffresident_form.setVisible(true);
        staffresident_form.setLocationRelativeTo(null);
    }//GEN-LAST:event_btnCancelMouseClicked

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

    private void txtNameKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtNameKeyReleased
        if (evt.getKeyCode() != KeyEvent.VK_ENTER) {
            lblNameError.setText("");
        }
    }//GEN-LAST:event_txtNameKeyReleased

    private void txtNumberKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtNumberKeyReleased
        if (evt.getKeyCode() != KeyEvent.VK_ENTER) {
            lblNumberError.setText("");
        }
    }//GEN-LAST:event_txtNumberKeyReleased

    private void txtEmailKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtEmailKeyReleased
        if (evt.getKeyCode() != KeyEvent.VK_ENTER) {
            lblEmailError.setText("");
        }
    }//GEN-LAST:event_txtEmailKeyReleased

    private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateActionPerformed
        if (multiValidate()) {
            updateResident(txtName.getText(), txtNumber.getText(), txtEmail.getText(), cmbUnit.getItemAt(cmbUnit.getSelectedIndex()));
        }
    }//GEN-LAST:event_btnUpdateActionPerformed

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
                new StaffUpdateResident().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnUpdate;
    private javax.swing.JComboBox<String> cmbUnit;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel lblEmailError;
    private javax.swing.JLabel lblError;
    private javax.swing.JLabel lblNameError;
    private javax.swing.JLabel lblNumberError;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtName;
    private javax.swing.JTextField txtNumber;
    private javax.swing.JTextField txtUserId;
    // End of variables declaration//GEN-END:variables
}
