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
import java.util.ArrayList;
import javax.swing.JOptionPane;
import javax.swing.RowFilter;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/**
 *
 * @author Jozhua Ten
 */
public class Residents extends javax.swing.JFrame {
    public static String resident_id = "TP";
    private DefaultTableModel residents_table;
    private boolean error = false;

    /**
     * Creates new form SearchStudent
     */
    public Residents() {
        initComponents();
        loadResidents();
    }

    // Load all the residents in an array list from the database
    private ArrayList<ResidentsList> storeResidentsList() {
        error = false;

        lblError.setText("");

        PreparedStatement residents_sql;
        ResultSet residents_result;

        ArrayList<ResidentsList> residents_list = new ArrayList<>();
        if (!openConnection()) {
            lblError.setText("Error connecting to database.");
            error = true;
        }

        if (!error) {
            try {
                residents_sql = conn.prepareStatement("SELECT users.User_ID, User_Name, User_ContactNumber, User_EmailAddress, IF(UserUnit_TerminateDate IS NULL, Unit_ID, \"-\") AS Unit_ID, IF(UserUnit_TerminateDate IS NULL, UserUnit_RegisterDate, \"-\") AS UserUnit_RegisterDate FROM users JOIN users_units ON users.User_ID = users_units.User_ID WHERE UserUnit_ID IN (SELECT MAX(UserUnit_ID) FROM users_units GROUP BY User_ID) AND User_Deactivated = 0 ORDER BY User_ID ASC");
                residents_result = residents_sql.executeQuery();

                while (residents_result.next()) {
                    ResidentsList resident_list = new ResidentsList();
                    resident_list.setUserId(residents_result.getString("User_ID"));
                    resident_list.setName(residents_result.getString("User_Name"));
                    resident_list.setContactNumber(residents_result.getString("User_ContactNumber"));
                    resident_list.setEmailAddress(residents_result.getString("User_EmailAddress"));
                    resident_list.setUnitId(residents_result.getString("Unit_ID"));
                    resident_list.setRegisterDate(residents_result.getString("UserUnit_RegisterDate"));

                    residents_list.add(resident_list);
                }
            } catch (SQLException ex) {
                lblError.setText(ex.getMessage());                
            } finally {
                if (!closeConnection()) {
                    lblError.setText("Error closing database connection.");
                }
            }
        }
        return residents_list;
    }

    // Populates the table with the residents' information
    private void loadResidents() {
        ArrayList<ResidentsList> residents_list = storeResidentsList();

        residents_table = (DefaultTableModel) tblResidents.getModel();
        residents_table.setRowCount(0);

        Object[] residents_tablerow = new Object[6];

        for (int i = 0; i < residents_list.size(); i++) {
            residents_tablerow[0] = residents_list.get(i).getUserId();
            residents_tablerow[1] = residents_list.get(i).getName();
            residents_tablerow[2] = residents_list.get(i).getContactNumber();
            residents_tablerow[3] = residents_list.get(i).getEmailAddress();
            residents_tablerow[4] = residents_list.get(i).getUnitId();
            residents_tablerow[5] = residents_list.get(i).getRegisterDate();

            residents_table.addRow(residents_tablerow);
        }
    }

    // Searches the residents
    private void searchResident(String search) {
        TableRowSorter<DefaultTableModel> row_sorter = new TableRowSorter<>(residents_table);
        tblResidents.setRowSorter(row_sorter);
        row_sorter.setRowFilter(RowFilter.regexFilter(search));
    }

    // Validates that a row is selected
    private boolean validateSelection() {
        residents_table = (DefaultTableModel) tblResidents.getModel();

        if (tblResidents.getSelectedRow() == -1) {
            lblError.setText("Please select a resident.");
            return false;
        }
        resident_id = tblResidents.getValueAt(tblResidents.getSelectedRow(), 0).toString();
        return true;
    }

    // Deactivates the selected resident
    private void deactivateResident() {
        boolean deactivateonly = false;
        error = false;

        PreparedStatement termination_sql, deactivate_sql;

        if (!error) {
            try {
                // Ensures that the resident does not have outstanding fees
                CalculateFees calculate_fees = new CalculateFees(resident_id.toUpperCase());
                if (calculate_fees.getError()) {
                    String error_message = calculate_fees.getErrorMessage();
                    // For users who may not be renting but is activated
                    if (error_message.equals("User ID does not exist or is not currently renting")) {
                        if (validateUserIdExist(resident_id)) {
                            deactivateonly = true;
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
                    // Deactivates the user
                    deactivate_sql = conn.prepareStatement("UPDATE users SET User_Deactivated = 1 WHERE User_ID = ?");
                    deactivate_sql.setString(1, resident_id.toUpperCase());
                    
                    if (deactivateonly) {
                        if (deactivate_sql.executeUpdate() == 1) {
                            loadResidents();
                        } else {
                            lblError.setText("Error deactivating account.");
                        }
                    } else {
                        // Ends the unit residency
                        termination_sql = conn.prepareStatement("UPDATE users_units SET UserUnit_TerminateDate = CURRENT_DATE() WHERE UserUnit_ID = ?");
                        termination_sql.setInt(1, calculate_fees.getUserUnitId());
                        if (termination_sql.executeUpdate() == 1) {
                            if (deactivate_sql.executeUpdate() == 1) {
                                loadResidents();
                            } else {
                                lblError.setText("Error deactivating account.");
                            }
                        } else {
                            lblError.setText("Error terminating residency.");                            
                        }
                    }
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

    // Delete the selected resident
    private void deleteResident() {
        error = false;

        PreparedStatement delete_sql;

        if (!openConnection()) {
            lblError.setText("Error connecting to database.");
            error = true;
        }

        if (!error) {
            try {
                delete_sql = conn.prepareStatement("DELETE FROM users WHERE User_ID = ?");
                delete_sql.setString(1, resident_id);
                if (delete_sql.executeUpdate() == 1) {
                    loadResidents();
                } else {
                    lblError.setText("Error deleting account.");
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
        txtSearch = new javax.swing.JTextField();
        btnRefresh = new javax.swing.JButton();
        btnRegister = new javax.swing.JButton();
        btnUpdate = new javax.swing.JButton();
        btnReactivate = new javax.swing.JButton();
        btnDeactivate = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        lblError = new javax.swing.JLabel();
        btnBack = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblResidents = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Residents");

        jPanel1.setBackground(new java.awt.Color(51, 153, 255));
        jPanel1.setPreferredSize(new java.awt.Dimension(900, 75));

        jLabel1.setFont(new java.awt.Font("Segoe UI Semibold", 0, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("RESIDENTS");

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
        jPanel2.setPreferredSize(new java.awt.Dimension(900, 525));

        txtSearch.setBackground(new java.awt.Color(55, 71, 79));
        txtSearch.setFont(new java.awt.Font("Segoe UI Semilight", 0, 14)); // NOI18N
        txtSearch.setForeground(new java.awt.Color(255, 255, 255));
        txtSearch.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtSearch.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "SEARCH", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI Semilight", 0, 12), new java.awt.Color(255, 255, 255))); // NOI18N
        txtSearch.setCaretColor(new java.awt.Color(255, 255, 255));
        txtSearch.setPreferredSize(new java.awt.Dimension(300, 50));
        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtSearchKeyReleased(evt);
            }
        });

        btnRefresh.setBackground(new java.awt.Color(84, 110, 122));
        btnRefresh.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        btnRefresh.setForeground(new java.awt.Color(255, 255, 255));
        btnRefresh.setText("REFRESH");
        btnRefresh.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        btnRefresh.setBorderPainted(false);
        btnRefresh.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnRefresh.setFocusPainted(false);
        btnRefresh.setPreferredSize(new java.awt.Dimension(250, 50));
        btnRefresh.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnRefreshMouseClicked(evt);
            }
        });

        btnRegister.setBackground(new java.awt.Color(84, 110, 122));
        btnRegister.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        btnRegister.setForeground(new java.awt.Color(255, 255, 255));
        btnRegister.setText("REGISTER");
        btnRegister.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        btnRegister.setBorderPainted(false);
        btnRegister.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnRegister.setFocusPainted(false);
        btnRegister.setPreferredSize(new java.awt.Dimension(250, 50));
        btnRegister.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegisterActionPerformed(evt);
            }
        });

        btnUpdate.setBackground(new java.awt.Color(84, 110, 122));
        btnUpdate.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        btnUpdate.setForeground(new java.awt.Color(255, 255, 255));
        btnUpdate.setText("UPDATE");
        btnUpdate.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        btnUpdate.setBorderPainted(false);
        btnUpdate.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnUpdate.setFocusPainted(false);
        btnUpdate.setPreferredSize(new java.awt.Dimension(250, 50));
        btnUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateActionPerformed(evt);
            }
        });

        btnReactivate.setBackground(new java.awt.Color(84, 110, 122));
        btnReactivate.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        btnReactivate.setForeground(new java.awt.Color(255, 255, 255));
        btnReactivate.setText("REACTIVATE");
        btnReactivate.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        btnReactivate.setBorderPainted(false);
        btnReactivate.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnReactivate.setFocusPainted(false);
        btnReactivate.setPreferredSize(new java.awt.Dimension(250, 50));
        btnReactivate.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnReactivateMouseClicked(evt);
            }
        });

        btnDeactivate.setBackground(new java.awt.Color(84, 110, 122));
        btnDeactivate.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        btnDeactivate.setForeground(new java.awt.Color(255, 255, 255));
        btnDeactivate.setText("DEACTIVATE");
        btnDeactivate.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        btnDeactivate.setBorderPainted(false);
        btnDeactivate.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnDeactivate.setFocusPainted(false);
        btnDeactivate.setPreferredSize(new java.awt.Dimension(250, 50));
        btnDeactivate.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnDeactivateMouseClicked(evt);
            }
        });

        btnDelete.setBackground(new java.awt.Color(84, 110, 122));
        btnDelete.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        btnDelete.setForeground(new java.awt.Color(255, 255, 255));
        btnDelete.setText("DELETE");
        btnDelete.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        btnDelete.setBorderPainted(false);
        btnDelete.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnDelete.setFocusPainted(false);
        btnDelete.setPreferredSize(new java.awt.Dimension(250, 50));
        btnDelete.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnDeleteMouseClicked(evt);
            }
        });

        lblError.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        lblError.setForeground(new java.awt.Color(255, 51, 51));
        lblError.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblError.setPreferredSize(new java.awt.Dimension(250, 20));

        btnBack.setBackground(new java.awt.Color(84, 110, 122));
        btnBack.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        btnBack.setForeground(new java.awt.Color(255, 255, 255));
        btnBack.setText("BACK");
        btnBack.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        btnBack.setBorderPainted(false);
        btnBack.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnBack.setFocusPainted(false);
        btnBack.setPreferredSize(new java.awt.Dimension(100, 50));
        btnBack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBackActionPerformed(evt);
            }
        });

        tblResidents.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "User ID", "Name", "Number", "Email", "Unit", "Register Date"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblResidents.setCellSelectionEnabled(false);
        tblResidents.setRowSelectionAllowed(true);
        tblResidents.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tblResidents.setShowVerticalLines(false);
        jScrollPane2.setViewportView(tblResidents);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblError, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(btnBack, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(20, 20, 20)
                                .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 453, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jScrollPane2))
                        .addGap(30, 30, 30)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnRefresh, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnRegister, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnUpdate, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnReactivate, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnDelete, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnDeactivate, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(487, 487, 487))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnRefresh, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnBack, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(btnRegister, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(btnUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(20, 20, 20)
                        .addComponent(btnReactivate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(btnDeactivate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(20, 20, 20)
                        .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addGap(15, 15, 15)
                .addComponent(lblError, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(20, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 461, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackActionPerformed
        this.dispose();
        
        StaffMenu staffmenu_form = new StaffMenu();
        staffmenu_form.setVisible(true);
        staffmenu_form.setLocationRelativeTo(null);
    }//GEN-LAST:event_btnBackActionPerformed

    private void btnRegisterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegisterActionPerformed
        this.dispose();
        
        RegisterResident registerresident_form = new RegisterResident();
        registerresident_form.setVisible(true);
        registerresident_form.setLocationRelativeTo(null);
    }//GEN-LAST:event_btnRegisterActionPerformed

    private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateActionPerformed
        if (validateSelection()) {
            this.dispose();
        
            StaffUpdateResident staffupdateresident_form = new StaffUpdateResident();
            staffupdateresident_form.setVisible(true);
            staffupdateresident_form.setLocationRelativeTo(null);
        }
    }//GEN-LAST:event_btnUpdateActionPerformed

    private void btnDeleteMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnDeleteMouseClicked
        if (validateSelection()) {
            Object[] options = { "Yes", "No", "Deactivate instead" };
            int choice = JOptionPane.showOptionDialog(null, "This includes deleting payment records (without checking for outstanding fees), please consider deactivating instead. Do you want to proceed?", "Confirm Deletion", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[2]);
            if (choice == 0) {
                deleteResident();
            } else if (choice == 2) {
                deactivateResident();
            }
        }
    }//GEN-LAST:event_btnDeleteMouseClicked

    private void btnDeactivateMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnDeactivateMouseClicked
        if (validateSelection()) {
            int choice = JOptionPane.showConfirmDialog(null, "Are you sure you want to deactive this account?", "Confirm Deactivation", JOptionPane.YES_NO_OPTION);
            if (choice == 0) {
                deactivateResident();
            }
        }
    }//GEN-LAST:event_btnDeactivateMouseClicked

    private void btnRefreshMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnRefreshMouseClicked
        loadResidents();
    }//GEN-LAST:event_btnRefreshMouseClicked

    private void btnReactivateMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnReactivateMouseClicked
        this.dispose();

        Reactivate activate_form = new Reactivate();
        activate_form.setVisible(true);
        activate_form.setLocationRelativeTo(null);
    }//GEN-LAST:event_btnReactivateMouseClicked

    private void txtSearchKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtSearchKeyReleased
        searchResident(txtSearch.getText().trim());
    }//GEN-LAST:event_txtSearchKeyReleased

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
                new Residents().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBack;
    private javax.swing.JButton btnDeactivate;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnReactivate;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnRegister;
    private javax.swing.JButton btnUpdate;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblError;
    private javax.swing.JTable tblResidents;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables
}
