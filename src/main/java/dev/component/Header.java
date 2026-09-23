package dev.component;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.SwingUtilities;

public class Header extends javax.swing.JPanel {
    private Point initialClick;

    public Header() {
        initComponents();
        setOpaque(false);
        setBackground(new Color(51, 51, 51));
        
        // Add mouse listeners for window dragging
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                initialClick = e.getPoint();
                getComponentAt(initialClick);
            }
        });

        this.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                // Get window and current location
                java.awt.Window window = SwingUtilities.getWindowAncestor(Header.this);
                if (window != null) {  // Make sure we have a window
                    Point currentScreen = e.getLocationOnScreen();
                    
                    // Move window
                    window.setLocation(
                        currentScreen.x - initialClick.x,
                        currentScreen.y - initialClick.y
                    );
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonBadges1 = new dev.swing.ButtonBadges();
        buttonBadges2 = new dev.swing.ButtonBadges();

        buttonBadges1.setBackground(new java.awt.Color(25, 25, 25));
        buttonBadges1.setForeground(new java.awt.Color(9, 129, 233));
        buttonBadges1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/dev/icon/noti.png"))); // NOI18N
        buttonBadges1.setBadges(5);

        buttonBadges2.setBackground(new java.awt.Color(25, 25, 25));
        buttonBadges2.setForeground(new java.awt.Color(247, 58, 58));
        buttonBadges2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/dev/icon/message.png"))); // NOI18N
        buttonBadges2.setBadges(15);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(539, Short.MAX_VALUE)
                .addComponent(buttonBadges2, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(3, 3, 3)
                .addComponent(buttonBadges1, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(buttonBadges2, javax.swing.GroupLayout.DEFAULT_SIZE, 38, Short.MAX_VALUE)
                    .addComponent(buttonBadges1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    @Override
    public void paint(Graphics grphcs) {
        Graphics2D g2 = (Graphics2D) grphcs.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        Area area = new Area(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 15, 15));
        area.add(new Area(new Rectangle2D.Double(0, 20, getWidth(), getHeight())));
        g2.fill(area);
        g2.dispose();
        super.paint(grphcs);
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private dev.swing.ButtonBadges buttonBadges1;
    private dev.swing.ButtonBadges buttonBadges2;
    // End of variables declaration//GEN-END:variables
}
