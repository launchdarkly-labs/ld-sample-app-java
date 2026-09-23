package dev.swing;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JButton;
import javax.swing.border.EmptyBorder;
import javax.swing.Timer;

public class ButtonMenu extends JButton {

    public Color getEffectColor() {
        return effectColor;
    }

    public void setEffectColor(Color effectColor) {
        this.effectColor = effectColor;
    }

    private Timer timer;
    private int targetSize;
    private float animatSize;
    private Point pressedPoint;
    private float alpha;
    private long startTime;
    private static final int ANIMATION_DURATION = 400; // 400ms total duration
    private Color effectColor = new Color(173, 173, 173);

    public ButtonMenu() {
        setContentAreaFilled(false);
        setBorder(new EmptyBorder(8, 10, 8, 10));
        setHorizontalAlignment(JButton.LEFT);
        setBackground(new Color(43, 44, 75));
        setForeground(new Color(250, 250, 250));
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent me) {
                targetSize = Math.max(getWidth(), getHeight()) * 2;
                animatSize = 0;
                pressedPoint = me.getPoint();
                alpha = 0.5f;
                if (timer != null && timer.isRunning()) {
                    timer.stop();
                }
                startTime = System.currentTimeMillis();
                timer = new Timer(16, e -> { // ~60fps
                    long elapsed = System.currentTimeMillis() - startTime;
                    float progress = Math.min(1f, (float) elapsed / ANIMATION_DURATION);
                    
                    // Smooth easing for size
                    animatSize = targetSize * easeOutQuad(progress);
                    
                    // Smooth fade out after halfway point
                    if (progress > 0.5f) {
                        alpha = 0.5f * (1f - easeInQuad((progress - 0.5f) * 2f));
                    }
                    
                    if (progress >= 1f) {
                        alpha = 0;
                        ((Timer)e.getSource()).stop();
                    }
                    repaint();
                });
                timer.start();
            }
        });
    }

    // Smooth easing functions for animation
    private float easeOutQuad(float t) {
        return t * (2 - t);
    }
    
    private float easeInQuad(float t) {
        return t * t;
    }

    @Override
    protected void paintComponent(Graphics grphcs) {
        int width = getWidth();
        int height = getHeight();
        Graphics2D g2 = (Graphics2D) grphcs;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        if (pressedPoint != null) {
            Area area = new Area(new RoundRectangle2D.Double(0, 0, width, height, 10, 10));
            g2.setColor(effectColor);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha));
            area.intersect(new Area(new Ellipse2D.Double((pressedPoint.x - animatSize / 2), (pressedPoint.y - animatSize / 2), animatSize, animatSize)));
            g2.fill(area);
        }
        g2.setComposite(AlphaComposite.SrcOver);
        super.paintComponent(grphcs);
    }

    @Override
    public void paint(Graphics grphcs) {
        if (isSelected()) {
            int width = getWidth();
            int height = getHeight();
            Graphics2D g2 = (Graphics2D) grphcs.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(25, 25, 25));
            g2.fillRoundRect(0, 0, width - 1, height - 1, 10, 10);
        }
        super.paint(grphcs);
    }

}
