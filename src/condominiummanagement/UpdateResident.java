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
public class UpdateResident extends javax.swing.JFrame {
    private String user_id = Login.user_id;
    private String oldname, oldnumber, oldemail;
    private boolean error = false;
    private boolean newpassword = false;
    
    /**
     * Creates new form ModifyStudent
     */
    public UpdateResident() {
        initComponents();
        loadResident();
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
                resident_sql = conn.prepareStatement("SELECT User_Name, User_ContactNumber, User_EmailAddress, Unit_ID FROM users JOIN users_units ON users.User_ID = users_units.User_ID WHERE UserUnit_ID = (SELECT MAX(UserUnit_ID) FROM users_units WHERE User_ID = ?)");
                resident_sql.setString(1, user_id);
                resident_result = resident_sql.executeQuery();

                if (resident_result.next()) {
                    txtUserId.setText(user_id);

                    oldname = resident_result.getString("User_Name");
                    oldnumber = resident_result.getString("User_ContactNumber");
                    oldemail = resident_result.getString("User_EmailAddress");

                    txtName.setText(oldname);
                    txtNumber.setText(oldnumber);
                    txtEmail.setText(oldemail);
                }
            } catch (SQLException ex) {
                lblError.setText(ex.getMessage());
            } finally {
                if (!closeConnection()) {
                    lblError.setText("Error closing database connection.");
                }
            }
        }
        lblError.setText("");
    }
    
    private void updateResident(String name, String number, String email, String password) {
        boolean newname, newnumber, newemail;
        newname = false;
        newnumber = false;
        newemail = false;
        error = false;

        lblError.setText("");

        PreparedStatement update_sql, check_sql;
        ResultSet check_result;
        
        if (newpassword) {
            if (!validatePassword(txtNewPassword.getText())) {
                lblNewPasswordError.setText(getPasswordErrorMessage());
                error = true;
            } 
        }
        
        if (!oldname.equals(name)) {
            newname = true;
        }

        if (!oldnumber.equals(number)) {
            newnumber = true;
        }

        if (!oldemail.equals(email)) {
            newemail = true;
        }

        if (!newname && !newnumber && !newemail && !newpassword) {
            lblError.setText("No new details.");
            error = true;
        }
        
        if (!error && !openConnection()) {
            lblError.setText("Error connecting to database. ");
            error = true;
        }

        if (!error) {
            try {
                check_sql = conn.prepareStatement("SELECT * FROM users WHERE User_ID = ?");
                check_sql.setString(1, user_id);
                check_result = check_sql.executeQuery();

                if (check_result.next()) {
                    if (!BCrypt.checkpw(password, check_result.getString("User_Password"))) {
                        lblError.setText("Wrong password.");
                        error = true;
                    }
                } else {
                    lblError.setText("Wrong password.");
                    error = true;
                }
                
                if (!error) {
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
                    if (newpassword) {
                        if (count >= 1) {
                            sql = sql.concat(",");
                        }
                        sql = sql.concat(" User_Password = ?");
                        count++;
                    }
                    sql = sql.concat(" WHERE User_ID = ?");
                    
                    int number_index = 0;
                    update_sql = Database.conn.prepareStatement(sql);
                    if (newname) {
                        update_sql.setString(1, name);
                    }
                    if (newnumber) {
                        if (newname) {
                            number_index = 2;
                        } else {
                            number_index = 1;
                        }
                        update_sql.setString(number_index, number);
                    }
                    if (newemail) {
                        update_sql.setString(++number_index, email);
                    }
                    if (newpassword) {
                        update_sql.setString(count, BCrypt.hashpw(txtNewPassword.getText(), BCrypt.gensalt()));
                    }
                    update_sql.setString(++count, user_id);
                    if (update_sql.executeUpdate() == 1) {
                        Login.user_name = name;
                        this.dispose();

                        ResidentMenu resident_form = new ResidentMenu();
                        resident_form.setVisible(true);
                        resident_form.setLocationRelativeTo(null);
                    } else {
                        lblError.setText("Error updating account details. ");
                    }
                }
            } catch (SQLException ex) {
                lblError.setText(ex.getMessage());
            } finally {
                if (!closeConnection()) {
                    lblError.setText("Error closing database connection. ");
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
        txtPassword = new javax.swing.JPasswordField();
        ckbUpdatePassword = new javax.swing.JCheckBox();
        txtNewPassword = new javax.swing.JPasswordField();
        lblNameError = new javax.swing.JLabel();
        lblNumberError = new javax.swing.JLabel();
        lblEmailError = new javax.swing.JLabel();
        lblPasswordError = new javax.swing.JLabel();
        lblNewPasswordError = new javax.swing.JLabel();
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
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(31, 31, 31)
                .addComponent(jLabel1)
                .addGap(12, 12, 12))
        );

        jPanel2.setBackground(new java.awt.Color(55, 71, 79));
        jPanel2.setMinimumSize(new java.awt.Dimension(600, 325));

        txtUserId.setEditable(false);
        txtUserId.setBackground(new java.awt.Color(55, 71, 79));
        txtUserId.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtUserId.setForeground(new java.awt.Color(255, 255, 255));
        txtUserId.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtUserId.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "USER ID", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI Semilight", 0, 12), new java.awt.Color(255, 255, 255))); // NOI18N
        txtUserId.setCaretColor(new java.awt.Color(255, 255, 255));
        txtUserId.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        txtUserId.setDisabledTextColor(new java.awt.Color(255, 255, 255));
        txtUserId.setPreferredSize(new java.awt.Dimension(250, 50));

        txtName.setBackground(new java.awt.Color(55, 71, 79));
        txtName.setFont(new java.awt.Font("Segoe UI Semilight", 0, 14)); // NOI18N
        txtName.setForeground(new java.awt.Color(255, 255, 255));
        txtName.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtName.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "NAME", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI Semilight", 0, 12), new java.awt.Color(255, 255, 255))); // NOI18N
        txtName.setCaretColor(new java.awt.Color(255, 255, 255));
        txtName.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        txtName.setMinimumSize(new java.awt.Dimension(250, 30));
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

        txtPassword.setBackground(new java.awt.Color(55, 71, 79));
        txtPassword.setFont(new java.awt.Font("Segoe UI Semilight", 0, 14)); // NOI18N
        txtPassword.setForeground(new java.awt.Color(255, 255, 255));
        txtPassword.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtPassword.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "PASSWORD", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI Semilight", 0, 12), new java.awt.Color(255, 255, 255))); // NOI18N
        txtPassword.setCaretColor(new java.awt.Color(255, 255, 255));
        txtPassword.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        txtPassword.setMinimumSize(new java.awt.Dimension(250, 30));
        txtPassword.setPreferredSize(new java.awt.Dimension(250, 50));
        txtPassword.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtPasswordFocusLost(evt);
            }
        });
        txtPassword.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtPasswordKeyReleased(evt);
            }
        });

        ckbUpdatePassword.setBackground(new java.awt.Color(55, 71, 79));
        ckbUpdatePassword.setFont(new java.awt.Font("Segoe UI Semilight", 0, 14)); // NOI18N
        ckbUpdatePassword.setForeground(new java.awt.Color(255, 255, 255));
        ckbUpdatePassword.setText("Update Password");
        ckbUpdatePassword.setBorder(null);
        ckbUpdatePassword.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        ckbUpdatePassword.setPreferredSize(new java.awt.Dimension(250, 30));
        ckbUpdatePassword.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                ckbUpdatePasswordMouseClicked(evt);
            }
        });

        txtNewPassword.setBackground(new java.awt.Color(55, 71, 79));
        txtNewPassword.setFont(new java.awt.Font("Segoe UI Semilight", 0, 14)); // NOI18N
        txtNewPassword.setForeground(new java.awt.Color(255, 255, 255));
        txtNewPassword.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtNewPassword.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "NEW PASSWORD", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI Semilight", 0, 12), new java.awt.Color(255, 255, 255))); // NOI18N
        txtNewPassword.setCaretColor(new java.awt.Color(255, 255, 255));
        txtNewPassword.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        txtNewPassword.setEnabled(false);
        txtNewPassword.setMinimumSize(new java.awt.Dimension(250, 30));
        txtNewPassword.setPreferredSize(new java.awt.Dimension(250, 50));
        txtNewPassword.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtNewPasswordFocusLost(evt);
            }
        });
        txtNewPassword.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtNewPasswordKeyReleased(evt);
            }
        });

        lblNameError.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        lblNameError.setForeground(new java.awt.Color(255, 51, 51));
        lblNameError.setPreferredSize(new java.awt.Dimension(250, 20));

        lblNumberError.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        lblNumberError.setForeground(new java.awt.Color(255, 51, 51));
        lblNumberError.setPreferredSize(new java.awt.Dimension(250, 20));

        lblEmailError.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        lblEmailError.setForeground(new java.awt.Color(255, 51, 51));
        lblEmailError.setPreferredSize(new java.awt.Dimension(250, 20));

        lblPasswordError.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        lblPasswordError.setForeground(new java.awt.Color(255, 51, 51));
        lblPasswordError.setPreferredSize(new java.awt.Dimension(250, 20));

        lblNewPasswordError.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        lblNewPasswordError.setForeground(new java.awt.Color(255, 51, 51));
        lblNewPasswordError.setPreferredSize(new java.awt.Dimension(250, 20));

        btnUpdate.setBackground(new java.awt.Color(51, 153, 255));
        btnUpdate.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        btnUpdate.setForeground(new java.awt.Color(255, 255, 255));
        btnUpdate.setText("UPDATE");
        btnUpdate.setBorder(null);
        btnUpdate.setBorderPainted(false);
        btnUpdate.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnUpdate.setFocusPainted(false);
        btnUpdate.setPreferredSize(new java.awt.Dimension(250, 50));
        btnUpdate.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnUpdateMouseClicked(evt);
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
        lblError.setText("Error");
        lblError.setPreferredSize(new java.awt.Dimension(250, 20));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblPasswordError, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(txtNumber, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblNumberError, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtNewPassword, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(ckbUpdatePassword, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtPassword, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(50, 50, 50)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblEmailError, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(lblNewPasswordError, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtUserId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblNameError, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblError, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                .addComponent(txtPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(lblPasswordError, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(15, 15, 15)
                .addComponent(ckbUpdatePassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(txtNewPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(lblNewPasswordError, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20)
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
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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

    private void txtPasswordFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtPasswordFocusLost
        if (!validatePassword(txtPassword.getText())) {
            lblPasswordError.setText(getPasswordErrorMessage());
        }
    }//GEN-LAST:event_txtPasswordFocusLost

    private void txtNewPasswordFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtNewPasswordFocusLost
        if (!validatePassword(txtNewPassword.getText())) {
            lblPasswordError.setText(getPasswordErrorMessage());
        }
    }//GEN-LAST:event_txtNewPasswordFocusLost

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

    private void btnCancelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCancelMouseClicked
        this.dispose();

        ResidentMenu resident_form = new ResidentMenu();
        resident_form.setVisible(true);
        resident_form.setLocationRelativeTo(null);
    }//GEN-LAST:event_btnCancelMouseClicked

    private void btnUpdateMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnUpdateMouseClicked
        if (validateName(txtName.getText()) && validateNumber(txtNumber.getText()) && validateEmail(txtEmail.getText()) && validatePassword(txtPassword.getText())) {
            updateResident(txtName.getText(), txtNumber.getText(), txtEmail.getText(), txtPassword.getText());
        } else {
            lblNameError.setText(getNameErrorMessage());
            lblNumberError.setText(getNumberErrorMessage());
            lblEmailError.setText(getEmailErrorMessage());
            lblPasswordError.setText(getPasswordErrorMessage());
        }
    }//GEN-LAST:event_btnUpdateMouseClicked

    private void ckbUpdatePasswordMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_ckbUpdatePasswordMouseClicked
        if (ckbUpdatePassword.isSelected()) {
            txtNewPassword.setEnabled(true);
            newpassword = true;
        } else {
            txtNewPassword.setEnabled(false);
            newpassword = false;
        }
    }//GEN-LAST:event_ckbUpdatePasswordMouseClicked

    private void txtNameKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtNameKeyReleased
        lblNameError.setText("");
    }//GEN-LAST:event_txtNameKeyReleased

    private void txtNumberKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtNumberKeyReleased
        lblNumberError.setText("");
    }//GEN-LAST:event_txtNumberKeyReleased

    private void txtEmailKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtEmailKeyReleased
        lblEmailError.setText("");
    }//GEN-LAST:event_txtEmailKeyReleased

    private void txtPasswordKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtPasswordKeyReleased
        lblPasswordError.setText("");
    }//GEN-LAST:event_txtPasswordKeyReleased

    private void txtNewPasswordKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtNewPasswordKeyReleased
        lblNewPasswordError.setText("");
    }//GEN-LAST:event_txtNewPasswordKeyReleased

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
                new UpdateResident().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnUpdate;
    private javax.swing.JCheckBox ckbUpdatePassword;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel lblEmailError;
    private javax.swing.JLabel lblError;
    private javax.swing.JLabel lblNameError;
    private javax.swing.JLabel lblNewPasswordError;
    private javax.swing.JLabel lblNumberError;
    private javax.swing.JLabel lblPasswordError;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtName;
    private javax.swing.JPasswordField txtNewPassword;
    private javax.swing.JTextField txtNumber;
    private javax.swing.JPasswordField txtPassword;
    private javax.swing.JTextField txtUserId;
    // End of variables declaration//GEN-END:variables
}
