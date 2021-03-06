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
import java.util.ArrayList;
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/**
 *
 * @author Jozhua Ten
 */
public class Units extends javax.swing.JFrame {
    public static String unit_id;
    private DefaultTableModel units_table;
    private boolean error = false;
    
    /**
     * Creates new form Units
     */
    public Units() {
        initComponents();
        loadUnits();
    }
    
    private ArrayList<UnitsList> storeUnitsList() {
        error = false;

        lblError.setText("");

        PreparedStatement units_sql;
        ResultSet units_result;

        ArrayList<UnitsList> units_list = new ArrayList<>();
        if (!openConnection()) {
            lblError.setText("Error connecting to database.");
            error = true;
        }

        if (!error) {
            try {
                units_sql = conn.prepareStatement("SELECT units.Unit_ID, IFNULL(Price_Amount, 200) AS Price_Amount, IFNULL(Price_Date, \"-\") AS Price_Date, IFNULL(User_ID, \"-\") AS User_ID FROM units LEFT JOIN price ON units.Unit_ID = price.Unit_ID LEFT JOIN users_units ON units.Unit_ID = users_units.Unit_ID WHERE EXISTS (SELECT UserUnit_ID FROM users_units WHERE UserUnit_TerminateDate IS NULL GROUP BY Unit_ID) ORDER BY Unit_ID");
                units_result = units_sql.executeQuery();

                while (units_result.next()) {
                    UnitsList unit_list = new UnitsList();
                    unit_list.setUnitId(units_result.getString("Unit_ID"));
                    unit_list.setPriceAmount(units_result.getInt("Price_Amount"));
                    unit_list.setPriceDate(units_result.getString("Price_Date"));
                    unit_list.setUserId(units_result.getString("User_ID"));

                    units_list.add(unit_list);
                }
            } catch (SQLException ex) {
                lblError.setText(ex.getMessage());                
            } finally {
                if (!closeConnection()) {
                    lblError.setText("Error closing database connection.");
                }
            }
        }
        return units_list;
    }
    
    private void loadUnits() {
        ArrayList<UnitsList> units_list = storeUnitsList();

        units_table = (DefaultTableModel) tblUnits.getModel();
        units_table.setRowCount(0);

        Object[] units_tablerow = new Object[4];

        for (int i = 0; i < units_list.size(); i++) {
            units_tablerow[0] = units_list.get(i).getUnitId();
            units_tablerow[1] = units_list.get(i).getPriceAmount();
            units_tablerow[2] = units_list.get(i).getPriceDate();
            units_tablerow[3] = units_list.get(i).getUserId();

            units_table.addRow(units_tablerow);
        }
    }
    
    private void searchUnits(String search) {
        TableRowSorter<DefaultTableModel> row_sorter = new TableRowSorter<>(units_table);
        tblUnits.setRowSorter(row_sorter);
        row_sorter.setRowFilter(RowFilter.regexFilter(search));
    }
    
    private boolean validateSelection() {
        if (tblUnits.getSelectedRow() == -1) {
            lblError.setText("Please select a unit.");
            return false;
        }
        unit_id = tblUnits.getValueAt(tblUnits.getSelectedRow(), 0).toString();
        return true;
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
        jScrollPane1 = new javax.swing.JScrollPane();
        tblUnits = new javax.swing.JTable();
        txtSearch = new javax.swing.JTextField();
        lblError = new javax.swing.JLabel();
        btnBack = new javax.swing.JButton();
        btnRefresh = new javax.swing.JButton();
        btnUpdatePrice = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Units");

        jPanel1.setBackground(new java.awt.Color(51, 153, 255));

        jLabel1.setFont(new java.awt.Font("Segoe UI Semibold", 0, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("UNITS");

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

        tblUnits.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Unit ID", "Price", "Effective Month", "User ID"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Integer.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblUnits.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tblUnits.setShowVerticalLines(false);
        jScrollPane1.setViewportView(tblUnits);
        tblUnits.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        txtSearch.setBackground(new java.awt.Color(55, 71, 79));
        txtSearch.setFont(new java.awt.Font("Segoe UI Semilight", 0, 14)); // NOI18N
        txtSearch.setForeground(new java.awt.Color(255, 255, 255));
        txtSearch.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtSearch.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "SEARCH", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI Semilight", 0, 12), new java.awt.Color(255, 255, 255))); // NOI18N
        txtSearch.setCaretColor(new java.awt.Color(255, 255, 255));
        txtSearch.setPreferredSize(new java.awt.Dimension(300, 50));
        txtSearch.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtSearchFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtSearchFocusLost(evt);
            }
        });
        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtSearchKeyReleased(evt);
            }
        });

        lblError.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        lblError.setForeground(new java.awt.Color(255, 51, 51));
        lblError.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblError.setText("Error");
        lblError.setPreferredSize(new java.awt.Dimension(250, 20));

        btnBack.setBackground(new java.awt.Color(84, 110, 122));
        btnBack.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        btnBack.setForeground(new java.awt.Color(255, 255, 255));
        btnBack.setText("BACK");
        btnBack.setBorder(null);
        btnBack.setBorderPainted(false);
        btnBack.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnBack.setFocusPainted(false);
        btnBack.setPreferredSize(new java.awt.Dimension(100, 50));
        btnBack.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnBackMouseClicked(evt);
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

        btnUpdatePrice.setBackground(new java.awt.Color(84, 110, 122));
        btnUpdatePrice.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        btnUpdatePrice.setForeground(new java.awt.Color(255, 255, 255));
        btnUpdatePrice.setText("UPDATE PRICE");
        btnUpdatePrice.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        btnUpdatePrice.setBorderPainted(false);
        btnUpdatePrice.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnUpdatePrice.setFocusPainted(false);
        btnUpdatePrice.setPreferredSize(new java.awt.Dimension(250, 50));
        btnUpdatePrice.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnUpdatePriceMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblError, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 573, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(btnBack, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(20, 20, 20)
                                .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 453, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(30, 30, 30)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btnRefresh, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnUpdatePrice, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addGap(22, 22, 22))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnRefresh, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnBack, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 20, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 310, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnUpdatePrice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(15, 15, 15)
                .addComponent(lblError, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 0, Short.MAX_VALUE))
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

    private void txtSearchFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtSearchFocusGained
        if (txtSearch.getText().equals("Search")) {
            txtSearch.setText("");
        }
    }//GEN-LAST:event_txtSearchFocusGained

    private void txtSearchFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtSearchFocusLost
        if (txtSearch.getText().trim().isEmpty()) {
            txtSearch.setText("Search");
        } else {
            searchUnits(txtSearch.getText().trim());
        }
    }//GEN-LAST:event_txtSearchFocusLost

    private void txtSearchKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtSearchKeyReleased
        searchUnits(txtSearch.getText().trim());
    }//GEN-LAST:event_txtSearchKeyReleased

    private void btnBackMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnBackMouseClicked
        this.dispose();

        StaffMenu staff_form = new StaffMenu();
        staff_form.setVisible(true);
        staff_form.setLocationRelativeTo(null);
    }//GEN-LAST:event_btnBackMouseClicked

    private void btnRefreshMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnRefreshMouseClicked
        loadUnits();
    }//GEN-LAST:event_btnRefreshMouseClicked

    private void btnUpdatePriceMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnUpdatePriceMouseClicked
        if (validateSelection()) {
            this.dispose();

            UpdatePrice updateprice_form = new UpdatePrice();
            updateprice_form.setVisible(true);
            updateprice_form.setLocationRelativeTo(null);
        }
    }//GEN-LAST:event_btnUpdatePriceMouseClicked

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
                new Units().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBack;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnUpdatePrice;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblError;
    private javax.swing.JTable tblUnits;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables
}
