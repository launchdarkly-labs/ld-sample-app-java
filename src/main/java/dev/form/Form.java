package dev.form;

import java.awt.Color;

public class Form extends javax.swing.JPanel {

    public Form(int index) {
        // Set dark background immediately
        setBackground(new Color(25, 25, 25));
        setOpaque(true);
        setDoubleBuffered(true);
        
        initComponents();
        
        // Keep dark background
        setBackground(new Color(25, 25, 25));
        setOpaque(true);
        jLabel1.setText("Form " + index);
        jLabel1.setBackground(new Color(25, 25, 25));
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setBackground(new Color(25, 25, 25));
        setOpaque(true);

        jLabel1 = new javax.swing.JLabel();
        jLabel1.setBackground(new Color(25, 25, 25));
        jLabel1.setOpaque(true);
        jLabel1.setFont(new java.awt.Font("sansserif", 1, 48)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(137, 137, 137));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Form");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 388, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 288, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables
}
