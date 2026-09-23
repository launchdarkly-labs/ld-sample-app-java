package dev.swing;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import javax.swing.JButton;
import javax.swing.border.EmptyBorder;
import javax.swing.Timer;

public class ButtonBadges extends JButton {

    public int getBadges() {
        return badges;
    }

    public void setBadges(int badges) {
        this.badges = badges;
    }

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
    private int badges;

    public ButtonBadges() {
        setContentAreaFilled(false);
        setBorder(new EmptyBorder(5, 5, 5, 5));
        setBackground(Color.WHITE);
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
        int size = Math.min(width, height) - 8;
        int x = (width - size) / 2;
        int y = (height - size) / 2;
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillOval(0, 0, size, size);
        if (pressedPoint != null) {
            g2.setColor(effectColor);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha));
            g2.fillOval((int) (pressedPoint.x - animatSize / 2), (int) (pressedPoint.y - animatSize / 2), (int) animatSize, (int) animatSize);
        }
        g2.dispose();
        grphcs.drawImage(img, x, y, null);
        super.paintComponent(grphcs);
    }

    @Override
    public void paint(Graphics grphcs) {
        super.paint(grphcs);
        if (badges > 0) {
            String value = badges > 9 ? "+9" : badges + "";
            int width = getWidth();
            Graphics2D g2 = (Graphics2D) grphcs;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            FontMetrics ft = g2.getFontMetrics();
            Rectangle2D r2 = ft.getStringBounds(value, g2);
            int fw = (int) r2.getWidth();
            int fh = (int) r2.getHeight();
            g2.setColor(getForeground());
            int size = Math.max(fw, fh) + 4;
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.9f));
            g2.fillOval(width - size, 0, size, size);
            int x = (size - fw) / 2;
            g2.setColor(Color.WHITE);
            g2.setComposite(AlphaComposite.SrcOver);
            g2.drawString(value, width - size + x, ft.getAscent() + 1);
            g2.dispose();
        }
    }
}
